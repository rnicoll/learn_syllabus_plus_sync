package uk.ac.ed.learn9.bb.timetabling.service;

import blackboard.base.FormattedText;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import blackboard.data.course.Course;
import blackboard.data.course.CourseCourse;
import blackboard.data.course.Group;
import blackboard.data.ValidationException;
import blackboard.data.course.CourseMembership;
import blackboard.data.course.GroupMembership;
import blackboard.data.user.User;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseDbLoader;
import blackboard.persist.course.CourseCourseDbLoader;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.persist.course.GroupDbLoader;
import blackboard.persist.course.GroupDbPersister;
import blackboard.persist.course.GroupMembershipDbLoader;
import blackboard.persist.course.GroupMembershipDbPersister;
import blackboard.persist.user.UserDbLoader;
import java.sql.Timestamp;
import org.springframework.stereotype.Service;
import uk.ac.ed.learn9.bb.timetabling.data.EnrolmentChange;
import uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun;


/**
 * Service for interacting with Blackboard Learn.
 */
@Service
public class BlackboardService {    
    public static final String GROUP_ROLE_IDENTIFIER_STUDENT = "student";

    /**
     * Applies all changes that remove an enrolment of a student on a course
     * group.
     * 
     * @param connection a connection to the cache database.
     * @param run the synchronisation run to pull changes to be actioned, from.
     * @throws SQLException
     * @throws PersistenceException
     * @throws ValidationException 
     */
    public void applyRemoveEnrolmentChanges(final Connection connection, final SynchronisationRun run)
        throws SQLException, PersistenceException, ValidationException {
        final GroupMembershipDbLoader groupMembershipLoader = this.getGroupMembershipDbLoader();
        final GroupMembershipDbPersister groupMembershipDbPersister = this.getGroupMembershipDbPersister();
        
        final PreparedStatement updateStatement = connection.prepareStatement(
            "UPDATE enrolment_change "
                + "SET update_completed=NOW() "
                + "WHERE change_id=? "
                    + "AND update_completed IS NULL"
        );
        
        try {
            final PreparedStatement queryStatement = connection.prepareStatement(
                "SELECT c.change_id, m.learn_course_id, a.learn_group_id, s.learn_user_id "
                    + "FROM enrolment_change c "
                        + "JOIN activity a on a.tt_activity_id=c.tt_activity_id "
                        + "JOIN module m ON m.tt_module_id=a.tt_module_id "
                        + "JOIN student_set s ON s.tt_student_set_id=c.tt_student_set_id "
                    + "WHERE c.run_id=? "
                        + "AND c.change_type=? "
                        + "AND m.learn_course_id IS NOT NULL "
                        + "AND a.learn_group_id IS NOT NULL "
                        + "AND s.learn_user_id IS NOT NULL "
                        + "AND c.update_completed IS NULL"
            );
            try {
                queryStatement.setInt(1, run.getRunId());
                queryStatement.setString(2, EnrolmentChange.CHANGE_TYPE_REMOVE);
                
                final ResultSet rs = queryStatement.executeQuery();
                try {
                    while (rs.next()) {
                        final int changeId = rs.getInt("change_id");
                        final Id groupId = Id.generateId(Course.DATA_TYPE, rs.getString("learn_group_id"));
                        final Id studentId = Id.generateId(User.DATA_TYPE, rs.getString("learn_student_id"));
                        
                        try {
                            removeUserFromGroup(groupId, studentId, groupMembershipLoader, groupMembershipDbPersister);
                        } catch(KeyNotFoundException e) {
                            continue;
                        }
                        
                        updateStatement.setInt(1, changeId);
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
     * Applies pending 'add' changes to Learn.
     * 
     * @param connection a connection to the local database.
     * @param run the synchronisation run to take changes from.
     * @throws SQLException if there was a problem with the local database.
     * @throws PersistenceException
     * @throws ValidationException 
     */
    public void applyAddEnrolmentChanges(final Connection connection, final SynchronisationRun run)
        throws SQLException, PersistenceException, ValidationException {
        final CourseMembershipDbLoader courseMembershipLoader = this.getCourseMembershipDbLoader();
        final GroupMembershipDbPersister groupMembershipDbPersister = this.getGroupMembershipDbPersister();
        
        final PreparedStatement updateStatement = connection.prepareStatement(
            "UPDATE enrolment_change "
                + "SET update_completed=NOW() "
                + "WHERE change_id=? "
                    + "AND update_completed IS NULL"
        );
        
        try {
            final PreparedStatement queryStatement = connection.prepareStatement(
                "SELECT c.change_id, m.learn_course_id, a.learn_group_id, s.learn_user_id "
                    + "FROM enrolment_change c "
                        + "JOIN activity a on a.tt_activity_id=c.tt_activity_id "
                        + "JOIN module m ON m.tt_module_id=a.tt_module_id "
                        + "JOIN student_set s ON s.tt_student_set_id=c.tt_student_set_id "
                    + "WHERE c.run_id=? "
                        + "AND c.change_type=? "
                        + "AND m.learn_course_id IS NOT NULL "
                        + "AND a.learn_group_id IS NOT NULL "
                        + "AND s.learn_user_id IS NOT NULL "
                        + "AND c.update_completed IS NULL "
                    + "ORDER BY m.learn_course_id"
            );
            try {
                queryStatement.setInt(1, run.getRunId());
                queryStatement.setString(2, EnrolmentChange.CHANGE_TYPE_REMOVE);
                
                Id currentCourseId = null;
                Map<Id, CourseMembership> studentCourseMemberships = null;
                
                final ResultSet rs = queryStatement.executeQuery();
                try {
                    while (rs.next()) {
                        final int changeId = rs.getInt("change_id");
                        final Id courseId = Id.generateId(Course.DATA_TYPE, rs.getString("learn_course_id"));
                        final Id groupId = Id.generateId(Course.DATA_TYPE, rs.getString("learn_group_id"));
                        
                        if (null == currentCourseId
                                || !currentCourseId.equals(courseId)) {
                            // Load student memberships on the current course
                            currentCourseId = courseId;
                            studentCourseMemberships = getStudentCourseMemberships(courseMembershipLoader, courseId);
                        }
                        
                        final Id studentId = Id.generateId(User.DATA_TYPE, rs.getString("learn_student_id"));
                        final CourseMembership courseMembership = studentCourseMemberships.get(studentId);
                        
                        if (null == courseMembership) {
                            // Student is not on this course - probably a delay
                            // bringing in data from Learn, but we can ignore
                            continue;
                        }
                        
                        groupMembershipDbPersister.persist(buildGroupMembership(courseMembership, groupId));
                        
                        updateStatement.setInt(1, changeId);
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
     * Generates groups for activities in timetabling that can be mapped to courses
     * in Learn.
     */
    public void generateGroupsForActivities(final SynchronisationRun run, final Connection connection)
            throws KeyNotFoundException, PersistenceException, SQLException, ValidationException {
        final GroupDbPersister groupDbPersister = this.getGroupDbPersister();
        
        final ManagedLearnGroupIDStatement updateStatement
            = new ManagedLearnGroupIDStatement(connection);
        try {
            PreparedStatement queryStatement = connection.prepareStatement(
                "(SELECT tt_activity_id, tt_activity_name, learn_group_id, learn_group_name, "
                    + "learn_course_id, description "
                    + "FROM non_jta_sync_activities_vw WHERE learn_group_id IS NULL)"
                + " UNION "
                + "(SELECT tt_activity_id, tt_activity_name, learn_group_id, learn_group_name, "
                    + "learn_course_id, description "
                    + "FROM jta_sync_activities_vw WHERE learn_group_id IS NULL)"
            );
            try {
                queryStatement.setInt(1, run.getRunId());
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
     * @param connection a connection to the cache database.
     * @throws KeyNotFoundException if there was an inconsistency in the Learn
     * database.
     * @throws PersistenceException if there was a problem loading a course
     * from the Learn database.
     * @throws SQLException if there was a problem accessing the cache database.
     */
    public void mapModulesToCourses(final Connection connection)
            throws KeyNotFoundException, PersistenceException, SQLException {
        final CourseDbLoader courseDbLoader = this.getCourseDbLoader();
        final CourseCourseDbLoader courseCourseDbLoader = this.getCourseCourseDbLoader();
        
        final PreparedStatement updateStatement = connection.prepareStatement("UPDATE module "
            + "SET learn_course_id=? "
                + "WHERE tt_module_id=?");
        try {
            final PreparedStatement queryStatement = connection.prepareStatement(
                    "SELECT tt_module_id, effective_course_code, learn_course_id "
                        + "FROM sync_modules_vw "
                        + "WHERE learn_course_code IS NOT NULL "
                            + "AND learn_course_id IS NULL"
            );
            try {
                final ResultSet rs = queryStatement.executeQuery();
                while (rs.next()) {
                    final String courseCode = rs.getString("effective_course_code");

                    if (!courseDbLoader.doesCourseIdExist(courseCode)) {
                        continue;
                    }

                    Course course = courseDbLoader.loadByCourseId(courseCode);

                    // If the course has a parent-child relationship with another
                    // course, use the parent
                    try {
                        final CourseCourse courseCourse = courseCourseDbLoader.loadParent(course.getId());
                        final Course parentCourse = courseDbLoader.loadById(courseCourse.getParentCourseId());

                        // Successfully found a parent course, replace the child with it.
                        course = parentCourse;
                    } catch(KeyNotFoundException e) {
                        // No parent course, ignore
                    }

                    updateStatement.setString(1, course.getId().getExternalString());
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
     * Identifies students sets with group enrolments to be copied to Learn,
     * and maps them to their IDs in Learn.
     * @param run 
     */
    public void mapStudentSetsToUsers(final Connection connection, final SynchronisationRun run)
        throws PersistenceException, SQLException {
        final UserDbLoader userDbLoader = getUserDbLoader();
        
        final PreparedStatement updateStatement = connection.prepareStatement("UPDATE student_set "
            + "SET learn_user_id=? "
            + "WHERE tt_student_set_id=?");
        try {
            final PreparedStatement queryStatement = connection.prepareStatement(
                "SELECT s.tt_student_set_id, s.username, s.learn_user_id "
                    + "FROM sync_student_set_vw s "
                    + "WHERE s.learn_user_id IS NULL "
                        + "AND s.tt_student_set_id IN (SELECT DISTINCT tt_student_set_id FROM enrolment_add WHERE run_id=?)"
            );
            try {
                queryStatement.setInt(1, run.getRunId());
                final ResultSet rs = queryStatement.executeQuery();
                try {
                    while (rs.next()) {
                        final String username = rs.getString("username");
                        final User user;

                        try {
                            user = userDbLoader.loadByUserName(username);
                        } catch(KeyNotFoundException e) {
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
    public Group buildCourseGroup(final Id courseId, final String groupName, final FormattedText description) {
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
     * @param courseMembership
     * @param groupId
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
     * Get a mapping from user IDs to course memberships for students on a single
     * course in Learn.
     * 
     * @param courseMembershipLoader the course membership loader to use.
     * @param courseId the course to load course memberships from.
     * @return a mapping from user IDs to course memberships for students on a single
     * course in Learn.
     * @throws PersistenceException if there was a problem loading the
     * course memberships.
     */
    private Map<Id, CourseMembership> getStudentCourseMemberships(final CourseMembershipDbLoader courseMembershipLoader, final Id courseId)
            throws PersistenceException {
        Map<Id, CourseMembership> studentCourseMemberships = new HashMap<Id, CourseMembership>();
        
        for (CourseMembership membership: courseMembershipLoader.loadByCourseIdAndRole(courseId, CourseMembership.Role.STUDENT)) {
            studentCourseMemberships.put(membership.getUserId(), membership);
        }
        
        return studentCourseMemberships;
    }

    /**
     * Removes a user from a group in Learn.
     * 
     * @param groupId
     * @param studentId
     * @param groupMembershipLoader
     * @param groupMembershipDbPersister
     * @throws PersistenceException 
     */
    public void removeUserFromGroup(final Id groupId, final Id studentId, final GroupMembershipDbLoader groupMembershipLoader, final GroupMembershipDbPersister groupMembershipDbPersister)
            throws KeyNotFoundException, PersistenceException {
        final GroupMembership groupMembership = groupMembershipLoader.loadByGroupAndUserId(groupId, studentId);

        groupMembershipDbPersister.deleteById(groupMembership.getId());
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

    protected CourseMembershipDbLoader getCourseMembershipDbLoader() throws PersistenceException {
        return CourseMembershipDbLoader.Default.getInstance();
    }

    protected CourseDbLoader getCourseDbLoader() throws PersistenceException {
        return CourseDbLoader.Default.getInstance();
    }

    protected CourseCourseDbLoader getCourseCourseDbLoader() throws PersistenceException {
        return CourseCourseDbLoader.Default.getInstance();
    }

    protected GroupMembershipDbLoader getGroupMembershipDbLoader() throws PersistenceException {
        return GroupMembershipDbLoader.Default.getInstance();
    }

    protected GroupMembershipDbPersister getGroupMembershipDbPersister() throws PersistenceException {
        return GroupMembershipDbPersister.Default.getInstance();
    }

    protected GroupDbLoader getGroupDbLoader() throws PersistenceException {
        return getGroupDbLoader();
    }

    protected GroupDbPersister getGroupDbPersister() throws PersistenceException {
        return getGroupDbPersister();
    }

    protected UserDbLoader getUserDbLoader() throws PersistenceException {
        return UserDbLoader.Default.getInstance();
    }
    
    /**
     * Wrapper around a prepared statement for updating the Learn group ID
     * associated with an activity in the database.
     */
    private class ManagedLearnGroupIDStatement extends Object {
        private final PreparedStatement statement;
        
        private             ManagedLearnGroupIDStatement(final Connection connection)
            throws SQLException {
            this.statement = connection.prepareStatement("UPDATE activity a "
                + "SET a.learn_group_id=?, a.learn_group_created=? "
                + "WHERE a.tt_activity_id=?");
        }
        
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
