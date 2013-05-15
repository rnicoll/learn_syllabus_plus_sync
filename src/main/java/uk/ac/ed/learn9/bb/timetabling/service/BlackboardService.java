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
        final GroupMembershipDbLoader groupMembershipLoader = getGroupMembershipDbLoader();
        final GroupMembershipDbPersister groupMembershipDbPersister = getGroupMembershipDbPersister();
        
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
        final CourseMembershipDbLoader courseMembershipLoader = getCourseMembershipDbLoader();
        final GroupMembershipDbPersister groupMembershipDbPersister = getGroupMembershipDbPersister();
        
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
        final GroupDbPersister groupDbPersister = getGroupDbPersister();
        
        final PreparedStatement updateStatement = connection.prepareStatement(
            "UPDATE activity a "
                + "SET a.learn_group_id=? "
                + "WHERE a.tt_activity_id=?");
        try {
            // First resolve all activities that have a Learn course, but no group,
            // and are not JTA child activities.
            PreparedStatement queryStatement = connection.prepareStatement(
                "SELECT a.tt_activity_id, a.tt_activity_name, a.learn_group_id, a.learn_group_name, m.learn_course_id "
                    + "FROM sync_activities a "
                        + "JOIN module m ON m.tt_module_id=a.tt_module_id "
                        + "JOIN activity_type t ON t.tt_type_id=a.tt_type_id "
                    + "WHERE a.learn_group_id IS NULL "
                        + "AND a.tt_jta_activity_id IS NULL "
                        + "AND a.learn_group_name IS NOT NULL "
                        + "AND m.learn_course_id IS NOT NULL "
                        // Only work on activites to be added
                        + "AND a.tt_activity_id IN (SELECT DISTINCT tt_activity_id FROM enrolment_add WHERE run_id=?);"
            );
            try {
                queryStatement.setInt(1, run.getRunId());
                final ResultSet rs = queryStatement.executeQuery();
                try {
                    while (rs.next()) {
                        doCreateCourseGroupAndStoreId(rs, groupDbPersister, updateStatement);
                    }
                } finally {
                    rs.close();
                }
            } finally {
                queryStatement.close();
            }
            
            // Create groups for all activities that are part of a JTA
            queryStatement = connection.prepareStatement(
                "SELECT p.tt_activity_id, p.tt_activity_name, p.learn_group_id, p.learn_group_name, m.learn_course_id "
                    + "FROM activity a "
                        + "JOIN sync_activities p ON p.tt_activity_id=a.tt_jta_activity_id "
                        + "JOIN module m ON m.tt_module_id=p.tt_module_id "
                        + "JOIN activity_type t ON t.tt_type_id=p.tt_type_id "
                    + "WHERE p.learn_group_id IS NULL "
                        + "AND p.learn_group_name IS NOT NULL "
                        + "AND m.learn_course_id IS NOT NULL "
                        + "AND a.tt_activity_id IN (SELECT DISTINCT tt_activity_id FROM enrolment_add WHERE run_id=?)"
            );
            try {
                queryStatement.setInt(1, run.getRunId());
                final ResultSet rs = queryStatement.executeQuery();
                try {
                    while (rs.next()) {
                        doCreateCourseGroupAndStoreId(rs, groupDbPersister, updateStatement);
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
        final CourseDbLoader courseDbLoader = getCourseDbLoader();
        final CourseCourseDbLoader courseCourseDbLoader = getCourseCourseDbLoader();
        
        final PreparedStatement statement = connection.prepareStatement(
                "SELECT tt_module_id, learn_course_code, learn_course_id "
                    + "FROM module "
                    + "WHERE learn_course_code IS NOT NULL "
                        + "AND learn_course_id IS NULL",
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE
        );
        try {
            final ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                final String courseCode = rs.getString("learn_course_code");
                
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
                
                rs.updateString("learn_course_id", course.getId().getExternalString());
                rs.updateRow();
            }
        } finally {
            statement.close();
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
        
        // First resolve all activities that have a Learn course, but no group,
        // and are not JTA child activities.
        PreparedStatement queryStatement = connection.prepareStatement(
            "SELECT s.tt_student_set_id, s.tt_host_key username, s.learn_user_id "
                + "FROM student_set s "
                + "WHERE s.learn_user_id IS NULL "
                    + "AND s.tt_host_key IS NOT NULL "
                    + "AND s.tt_student_set_id IN (SELECT DISTINCT tt_student_set_id FROM enrolment_add WHERE run_id=?)",
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE
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
                    
                    rs.updateString("learn_user_id", user.getId().getExternalString());
                    rs.updateRow();
                }
            } finally {
                rs.close();
            }
        } finally {
            queryStatement.close();
        }
    }

    /**
     * Constructs a new course group and returns it.
     * 
     * @param courseId the course that the group will belong to.
     * @param groupName the name of the group.
     * @return the new group.
     */
    public Group buildCourseGroup(final Id courseId, final String groupName, final FormattedText description)
         {
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
        final GroupDbLoader groupLoader = GroupDbLoader.Default.getInstance();
        final Group group = groupLoader.loadById(groupId);
        
        final GroupDbPersister groupPersister = GroupDbPersister.Default.getInstance();
        
        group.setDescription(new FormattedText(description, FormattedText.Type.PLAIN_TEXT));
        
        groupPersister.persist(group);
    }

    private CourseMembershipDbLoader getCourseMembershipDbLoader() throws PersistenceException {
        return CourseMembershipDbLoader.Default.getInstance();
    }

    private CourseDbLoader getCourseDbLoader() throws PersistenceException {
        return CourseDbLoader.Default.getInstance();
    }

    private CourseCourseDbLoader getCourseCourseDbLoader() throws PersistenceException {
        return CourseCourseDbLoader.Default.getInstance();
    }

    private GroupMembershipDbLoader getGroupMembershipDbLoader() throws PersistenceException {
        return GroupMembershipDbLoader.Default.getInstance();
    }

    private GroupMembershipDbPersister getGroupMembershipDbPersister() throws PersistenceException {
        return GroupMembershipDbPersister.Default.getInstance();
    }

    private GroupDbPersister getGroupDbPersister() throws PersistenceException {
        return GroupDbPersister.Default.getInstance();
    }

    private UserDbLoader getUserDbLoader() throws PersistenceException {
        return UserDbLoader.Default.getInstance();
    }

    /**
     * Internal method that fetches Learn course ID and group name from a
     * result set, creates a suitable group and persists it, then writes the
     * group ID back into the database. This is only intended for use by
     * the {@link #generateGroupsForActivities(SynchronisationRun, Connection)}
     * method.
     * 
     * @param rs the result set to extract data from. Must have "tt_activity_id",
     * "learn_course_id" and "learn_group_name" fields.
     * @param groupDbPersister the group persister for Learn.
     * @param updateStatement the update statement to use to write changes back.
     * First parameter must be group ID (to set), the second the activity ID
     * (as primary key).
     * @throws PersistenceException
     * @throws ValidationException
     * @throws SQLException 
     */
    private void doCreateCourseGroupAndStoreId(final ResultSet rs, final GroupDbPersister groupDbPersister,
            final PreparedStatement updateStatement)
            throws PersistenceException, ValidationException, SQLException {
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
        
        final String activityId = rs.getString("tt_activity_id");
        
        updateStatement.setString(1, group.getId().toExternalString());
        updateStatement.setString(2, activityId);
        updateStatement.executeUpdate();
    }
}
