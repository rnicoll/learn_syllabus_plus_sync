package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import blackboard.base.FormattedText;
import blackboard.data.ValidationException;
import blackboard.data.course.Course;
import blackboard.data.course.CourseCourse;
import blackboard.data.course.CourseMembership;
import blackboard.data.course.Group;
import blackboard.data.course.GroupMembership;
import blackboard.data.user.User;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseCourseDbLoader;
import blackboard.persist.course.CourseDbLoader;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.persist.course.GroupDbLoader;
import blackboard.persist.course.GroupDbPersister;
import blackboard.persist.course.GroupMembershipDbLoader;
import blackboard.persist.course.GroupMembershipDbPersister;
import blackboard.persist.user.UserDbLoader;

import org.springframework.stereotype.Service;

import uk.ac.ed.learn9.bb.timetabling.data.EnrolmentChange;
import uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun;

/**
 * Service for interacting with Blackboard Learn.
 */
@Service
public class BlackboardService {
    /**
     * Identifier of role students have within a group, for creating new
     * group memberships.
     */
    public static final String GROUP_ROLE_IDENTIFIER_STUDENT = "student";

    /**
     * Applies pending changes to Learn. Normally this would be called by
     * {@link SynchronisationService#applyEnrolmentChanges(uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun)}.
     *
     * @param connection a connection to the local database.
     * @param run the synchronisation run to take changes from.
     * @throws SQLException if there was a problem with the local database.
     * @throws PersistenceException if there was a problem loading or saving
     * data from/to Learn.
     * @throws ValidationException if a newly generated group enrolment was
     * invalid.
     */
    public void applyEnrolmentChanges(final Connection connection, final SynchronisationRun run)
            throws SQLException, PersistenceException, ValidationException {
        final ChangeOutcomeUpdateStatement outcome = new ChangeOutcomeUpdateStatement(connection);

        try {
            final PreparedStatement queryStatement = connection.prepareStatement(
                    "SELECT c.change_id, m.learn_course_id, a.learn_group_id, s.learn_user_id, c.change_type "
                    + "FROM enrolment_change c "
                    + "JOIN activity a on a.tt_activity_id=c.tt_activity_id "
                    + "JOIN module m ON m.tt_module_id=a.tt_module_id "
                    + "JOIN student_set s ON s.tt_student_set_id=c.tt_student_set_id "
                    + "WHERE c.run_id=? "
                    + "AND c.update_completed IS NULL "
                    + "ORDER BY m.learn_course_id, c.change_id");
            try {
                queryStatement.setInt(1, run.getRunId());
                applyEnrolmentChanges(queryStatement, outcome);
            } finally {
                queryStatement.close();
            }
        } finally {
            outcome.close();
        }
    }

    /**
     * Applies any changes that have been generated, but not yet applied to
     * Learn. Normally this would be called by
     * {@link SynchronisationService#applyEnrolmentChanges(uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun)}.
     *
     * @param connection a connection to the local database.
     * @throws SQLException if there was a problem with the local database.
     * @throws PersistenceException if there was a problem loading or saving
     * data from/to Learn.
     * @throws ValidationException if a newly generated group enrolment was
     * invalid.
     */
    public void applyPreviouslyFailedEnrolmentChanges(final Connection connection)
            throws SQLException, PersistenceException, ValidationException {
        final ChangeOutcomeUpdateStatement outcome = new ChangeOutcomeUpdateStatement(connection);

        try {
            final PreparedStatement queryStatement = connection.prepareStatement(
                    "SELECT c.change_id, m.learn_course_id, a.learn_group_id, s.learn_user_id, c.change_type "
                    + "FROM enrolment_change c "
                    + "JOIN activity a on a.tt_activity_id=c.tt_activity_id "
                    + "JOIN module m ON m.tt_module_id=a.tt_module_id "
                    + "JOIN student_set s ON s.tt_student_set_id=c.tt_student_set_id "
                    + "JOIN change_result r ON r.result_code=c.result_code "
                    + "WHERE c.update_completed IS NULL "
                    + "AND r.retry='1' "
                    + "ORDER BY m.learn_course_id, c.change_id");
            try {
                applyEnrolmentChanges(queryStatement, outcome);
            } finally {
                queryStatement.close();
            }
        } finally {
            outcome.close();
        }
    }

    /**
     * Applies a set of enrolment changes to Learn.
     *
     * @param queryStatement a prepared statement ready to be executed. Results
     * must be ordered by course ID first, then by change ID.
     * @param outcome a prepared outcome persistence object.
     * @throws PersistenceException
     * @throws SQLException
     * @throws ValidationException
     */
    private void applyEnrolmentChanges(final PreparedStatement queryStatement, final ChangeOutcomeUpdateStatement outcome)
            throws PersistenceException, SQLException, ValidationException {
        final CourseMembershipDbLoader courseMembershipDbLoader = this.getCourseMembershipDbLoader();
        final GroupMembershipDbLoader groupMembershipDbLoader = this.getGroupMembershipDbLoader();
        final GroupMembershipDbPersister groupMembershipDbPersister = this.getGroupMembershipDbPersister();

        Id currentCourseId = null;
        Map<Id, CourseMembership> studentCourseMemberships = null;

        final ResultSet rs = queryStatement.executeQuery();
        try {
            while (rs.next()) {
                final EnrolmentChange.Type changeType = EnrolmentChange.Type.valueOf(rs.getString("change_type"));
                final int changeId = rs.getInt("change_id");
                String temp = rs.getString("learn_course_id");

                if (null == temp) {
                    outcome.markCourseMissing(changeId);
                    continue;
                }

                final Id courseId = Id.generateId(Course.DATA_TYPE, temp);

                temp = rs.getString("learn_group_id");
                if (null == temp) {
                    outcome.markGroupMissing(changeId);
                    continue;
                }

                final Id groupId = Id.generateId(Group.DATA_TYPE, temp);

                if (null == currentCourseId
                        || !currentCourseId.equals(courseId)) {
                    // Load student memberships on the current course
                    currentCourseId = courseId;
                    studentCourseMemberships = getStudentCourseMemberships(courseMembershipDbLoader, courseId);
                }

                temp = rs.getString("learn_student_id");
                if (null == temp) {
                    outcome.markStudentMissing(changeId);
                    continue;
                }

                final Id studentId = Id.generateId(User.DATA_TYPE, temp);
                switch (changeType) {
                    case ADD:
                        final CourseMembership courseMembership = studentCourseMemberships.get(studentId);

                        if (null == courseMembership) {
                            // Student is not on this course - probably a delay
                            // bringing in data from Learn, but we can ignore
                            outcome.markNotOnCourse(changeId);
                            continue;
                        }

                        groupMembershipDbPersister.persist(buildGroupMembership(courseMembership, groupId));

                        outcome.markSuccess(changeId);
                        break;
                    case REMOVE:
                        try {
                            final GroupMembership groupMembership = groupMembershipDbLoader.loadByGroupAndUserId(groupId, studentId);

                            groupMembershipDbPersister.deleteById(groupMembership.getId());
                            outcome.markSuccess(changeId);
                        } catch (KeyNotFoundException e) {
                            outcome.markAlreadyRemoved(changeId);
                        }
                        break;
                    default:
                        throw new RuntimeException("Unexpected change type \""
                                + changeType + "\".");
                }
            }
        } finally {
            rs.close();
        }
    }

    /**
     * Generates groups for activities in timetabling that can be mapped to
     * courses in Learn.
     *
     * @param stagingDatabase a connection to the staging database.
     * @throws PersistenceException if there was a problem loading or saving
     * data in Learn.
     * @throws SQLException if there was a problem accessing one of the
     * databases.
     * @throws ValidationException if a newly generated group fails validation
     * by Learn prior to persistence.
     */
    public void generateGroupsForActivities(final Connection stagingDatabase)
            throws PersistenceException, SQLException, ValidationException {
        final GroupDbPersister groupDbPersister = this.getGroupDbPersister();

        final ManagedLearnGroupIDStatement updateStatement = new ManagedLearnGroupIDStatement(stagingDatabase);
        try {
            PreparedStatement queryStatement = stagingDatabase.prepareStatement(
                    "(SELECT tt_activity_id, tt_activity_name, learn_group_id, learn_group_name, "
                    + "learn_course_id, description "
                    + "FROM non_jta_sync_activity_vw "
                        + "WHERE learn_course_id IS NOT NULL AND learn_group_id IS NOT NULL"
                    + ")"
                    + " UNION "
                    + "(SELECT tt_activity_id, tt_activity_name, learn_group_id, learn_group_name, "
                    + "learn_course_id, description "
                    + "FROM jta_sync_activity_vw "
                        + "WHERE learn_course_id IS NOT NULL AND learn_group_id IS NULL"
                    + ")");
            try {
                final ResultSet rs = queryStatement.executeQuery();
                try {
                    while (rs.next()) {
                        final Id courseId = Id.generateId(Course.DATA_TYPE, rs.getString("learn_course_id"));
                        final String descriptionText = rs.getString("description");
                        final FormattedText description;

                        if (null == descriptionText) {
                            description = null;
                        } else {
                            description = new FormattedText(descriptionText, FormattedText.Type.PLAIN_TEXT);
                        }

                        final Group group = buildCourseGroup(courseId, rs.getString("learn_group_name"), description);

                        groupDbPersister.persist(group);

                        final Timestamp now = new Timestamp(System.currentTimeMillis());
                        final String activityId = rs.getString("tt_activity_id");

                        updateStatement.recordGroupId(now, activityId, group.getId());
                    }
                } finally {
                    rs.close();
                }
            } finally {
                queryStatement.close();
            }
        } finally {
            updateStatement.close();
        }
    }

    /**
     * Resolves modules from the timetabling system into the relevant course in
     * Learn, where applicable.
     *
     * @param stagingDatabase a connection to the staging database.
     * @throws KeyNotFoundException if there was an inconsistency in the Learn
     * database.
     * @throws PersistenceException if there was a problem loading a course from
     * the Learn database.
     * @throws SQLException if there was a problem accessing the cache database.
     */
    public void mapModulesToCourses(final Connection stagingDatabase)
            throws KeyNotFoundException, PersistenceException, SQLException {
        final CourseDbLoader courseDbLoader = this.getCourseDbLoader();
        final CourseCourseDbLoader courseCourseDbLoader = this.getCourseCourseDbLoader();

        final PreparedStatement updateStatement = stagingDatabase.prepareStatement("UPDATE module "
                + "SET learn_course_id=? "
                + "WHERE tt_module_id=?");
        try {
            final PreparedStatement queryStatement = stagingDatabase.prepareStatement(
                    "SELECT tt_module_id, effective_course_code, learn_course_id "
                    + "FROM sync_module_vw "
                    + "WHERE learn_course_code IS NOT NULL "
                    + "AND learn_course_id IS NULL");
            try {
                final ResultSet rs = queryStatement.executeQuery();
                while (rs.next()) {
                    final String courseCode = rs.getString("effective_course_code");

                    if (!courseDbLoader.doesCourseIdExist(courseCode)) {
                        continue;
                    }

                    Course course = courseDbLoader.loadByCourseId(courseCode);
                    Id courseId;

                    // If the course has a parent-child relationship with another
                    // course, use the parent
                    try {
                        final CourseCourse courseCourse = courseCourseDbLoader.loadParent(course.getId());

                        // Successfully found a parent course, replace the child with it.
                        courseId = courseCourse.getParentCourseId();
                    } catch (KeyNotFoundException e) {
                        // No parent course, ignore
                        courseId = course.getId();
                    }

                    updateStatement.setString(1, courseId.getExternalString());
                    updateStatement.setString(2, rs.getString("tt_module_id"));
                    updateStatement.executeUpdate();
                }
            } finally {
                queryStatement.close();
            }
        } finally {
            updateStatement.close();
        }
    }

    /**
     * Identifies students sets with group enrolments to be copied to Learn, and
     * maps them to their IDs in Learn.
     *
     * @param stagingDatabase a connection to the staging database.
     *
     * @throws PersistenceException if there was a problem loading or saving
     * data in Learn.
     * @throws SQLException if there was a problem accessing one of the
     * databases.
     */
    public void mapStudentSetsToUsers(final Connection stagingDatabase)
            throws PersistenceException, SQLException {
        final UserDbLoader userDbLoader = getUserDbLoader();

        final PreparedStatement updateStatement = stagingDatabase.prepareStatement("UPDATE student_set "
                + "SET learn_user_id=? "
                + "WHERE tt_student_set_id=?");
        try {
            final PreparedStatement queryStatement = stagingDatabase.prepareStatement(
                    "SELECT s.tt_student_set_id, s.username, s.learn_user_id "
                    + "FROM sync_student_set_vw s "
                    + "WHERE s.learn_user_id IS NULL");
            try {
                final ResultSet rs = queryStatement.executeQuery();
                try {
                    while (rs.next()) {
                        final String username = rs.getString("username");
                        final User user;

                        try {
                            user = userDbLoader.loadByUserName(username);
                        } catch (KeyNotFoundException e) {
                            // User isn't in Learn, ignore
                            continue;
                        }

                        updateStatement.setString(1, user.getId().getExternalString());
                        updateStatement.setString(2, rs.getString("tt_student_set_id"));
                        updateStatement.executeUpdate();
                    }
                } finally {
                    rs.close();
                }
            } finally {
                queryStatement.close();
            }
        } finally {
            updateStatement.close();
        }
    }

    /**
     * Constructs a new course group and returns it.
     *
     * @param courseId the course that the group will belong to.
     * @param groupName the name of the group.
     * @return the new group.
     */
    private Group buildCourseGroup(final Id courseId, final String groupName, final FormattedText description) {
        // Create the new group
        final Group group = new Group();
        group.setCourseId(courseId);
        group.setTitle(groupName);
        group.setIsAvailable(false);
        group.setSelfEnrolledAllowed(false);
        group.setDescription(description);
        return group;
    }

    /**
     * Constructs a user's membership on a group and returns it.
     *
     * @param courseMembership the membership of the student onto the course the
     * group belongs to.
     * @param groupId the ID of the group to enrol the student onto.
     * @return the group membership.
     */
    public GroupMembership buildGroupMembership(final CourseMembership courseMembership,
            final Id groupId) {
        final GroupMembership groupMembership = new GroupMembership();

        groupMembership.setCourseMembershipId(courseMembership.getId());
        groupMembership.setGroupId(groupId);
        groupMembership.setGroupRoleIdentifier(GROUP_ROLE_IDENTIFIER_STUDENT);

        return groupMembership;
    }

    /**
     * Get a mapping from user IDs to course memberships for students on a
     * single course in Learn.
     *
     * @param courseMembershipLoader the course membership loader to use.
     * @param courseId the course to load course memberships from.
     * @return a mapping from user IDs to course memberships for students on a
     * single course in Learn.
     * @throws PersistenceException if there was a problem loading the course
     * memberships.
     */
    private Map<Id, CourseMembership> getStudentCourseMemberships(final CourseMembershipDbLoader courseMembershipLoader, final Id courseId)
            throws PersistenceException {
        Map<Id, CourseMembership> studentCourseMemberships = new HashMap<Id, CourseMembership>();

        for (CourseMembership membership : courseMembershipLoader.loadByCourseIdAndRole(courseId, CourseMembership.Role.STUDENT)) {
            studentCourseMemberships.put(membership.getUserId(), membership);
        }

        return studentCourseMemberships;
    }

    /**
     * Loads a group from the Blackboard database, changes its description then
     * writes it back out again.
     *
     * @param groupId the Blackboard ID for the group.
     * @param description a plain-text description for the group.
     * @throws KeyNotFoundException if the group could not be found.
     * @throws PersistenceException if there was a problem loading/saving the
     * group.
     * @throws ValidationException if the change was invalid.
     */
    public void updateGroupDescription(final Id groupId, final String description)
            throws KeyNotFoundException, PersistenceException, ValidationException {
        final GroupDbLoader groupLoader = this.getGroupDbLoader();
        final GroupDbPersister groupPersister = this.getGroupDbPersister();
        final Group group = groupLoader.loadById(groupId);

        group.setDescription(new FormattedText(description, FormattedText.Type.PLAIN_TEXT));

        groupPersister.persist(group);
    }

    /**
     * Gets an instance of a {@link CourseMembershipDbLoader}. This method
     * exists to allow overriding for test-cases.
     *
     * @return a course membership loader.
     * @throws PersistenceException if there was a problem constructing the
     * loader.
     */
    protected CourseMembershipDbLoader getCourseMembershipDbLoader() throws PersistenceException {
        return CourseMembershipDbLoader.Default.getInstance();
    }

    /**
     * Gets an instance of a {@link CourseDbLoader}. This method exists to allow
     * overriding for test-cases.
     *
     * @return a course loader.
     * @throws PersistenceException if there was a problem constructing the
     * loader.
     */
    protected CourseDbLoader getCourseDbLoader() throws PersistenceException {
        return CourseDbLoader.Default.getInstance();
    }

    /**
     * Gets an instance of a {@link CourseCourseDbLoader}. This method exists to allow
     * overriding for test-cases.
     *
     * @return a course-course relationship loader.
     * @throws PersistenceException if there was a problem constructing the
     * loader.
     */
    protected CourseCourseDbLoader getCourseCourseDbLoader() throws PersistenceException {
        return CourseCourseDbLoader.Default.getInstance();
    }

    /**
     * Gets an instance of a {@link GroupMembershipDbLoader}. This method exists
     * to allow overriding for test-cases.
     *
     * @return a group membership loader.
     * @throws PersistenceException if there was a problem constructing the
     * loader.
     */
    protected GroupMembershipDbLoader getGroupMembershipDbLoader() throws PersistenceException {
        return GroupMembershipDbLoader.Default.getInstance();
    }

    protected GroupMembershipDbPersister getGroupMembershipDbPersister() throws PersistenceException {
        return GroupMembershipDbPersister.Default.getInstance();
    }

    /**
     * Gets an instance of a {@link GroupDbLoader}. This method exists to allow
     * overriding for test-cases.
     *
     * @return a group loader.
     * @throws PersistenceException if there was a problem constructing the
     * loader.
     */
    protected GroupDbLoader getGroupDbLoader() throws PersistenceException {
        return GroupDbLoader.Default.getInstance();
    }

    protected GroupDbPersister getGroupDbPersister() throws PersistenceException {
        return GroupDbPersister.Default.getInstance();
    }

    /**
     * Gets an instance of a {@link UserDbLoader}. This method exists to allow
     * overriding for test-cases.
     *
     * @return a user loader.
     * @throws PersistenceException if there was a problem constructing the
     * loader.
     */
    protected UserDbLoader getUserDbLoader() throws PersistenceException {
        return UserDbLoader.Default.getInstance();
    }

    /**
     * Wrapper around a prepared statement for updating the Learn group ID
     * associated with an activity in the database.
     */
    private class ManagedLearnGroupIDStatement extends Object {

        private final PreparedStatement statement;

        private ManagedLearnGroupIDStatement(final Connection connection)
                throws SQLException {
            this.statement = connection.prepareStatement("UPDATE activity a "
                    + "SET a.learn_group_id=?, a.learn_group_created=? "
                    + "WHERE a.tt_activity_id=?");
        }

        /**
         * Closes the prepared statement underlying this object.
         *
         * @throws SQLException
         */
        public void close() throws SQLException {
            this.statement.close();
        }

        public int recordGroupId(final Timestamp now,
                final String activityId, final Id groupId)
                throws SQLException {
            this.statement.setString(1, groupId.toExternalString());
            this.statement.setTimestamp(2, now);
            this.statement.setString(3, activityId);
            return this.statement.executeUpdate();
        }
    }
}
