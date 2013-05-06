package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import blackboard.data.course.Course;
import blackboard.data.course.CourseCourse;
import blackboard.data.course.Group;
import blackboard.data.ValidationException;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseDbLoader;
import blackboard.persist.course.CourseCourseDbLoader;
import blackboard.persist.course.GroupDbPersister;
import org.springframework.stereotype.Service;
import uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun;


/**
 * Service for interacting with Blackboard Learn.
 */
@Service
public class BlackboardService {
    /**
     * Generates groups for activities in timetabling that can be mapped to courses
     * in Learn.
     */
    public void generateGroupsForActivities(final SynchronisationRun run, final Connection connection)
            throws KeyNotFoundException, PersistenceException, SQLException, ValidationException {
        final CourseDbLoader courseDbLoader = CourseDbLoader.Default.getInstance();
        final GroupDbPersister groupDbPersister = GroupDbPersister.Default.getInstance();
        
        final PreparedStatement updateStatement = connection.prepareStatement(
            "UPDATE activity a "
                + "SET a.learn_group_id=? "
                + "WHERE a.tt_activity_id=?");
        try {
            // First resolve all activities that have a Learn course, but no group,
            // and are not JTA child activities.
            PreparedStatement queryStatement = connection.prepareStatement(
                "SELECT a.tt_activity_id, a.tt_activity_name, a.learn_group_id, a.learn_group_name, m.learn_course_id "
                    + "FROM activity a "
                        + "JOIN module m ON m.tt_module_id=a.tt_module_id "
                        + "JOIN activity_type t ON t.tt_type_id=a.tt_type_id "
                    + "WHERE a.learn_group_id IS NULL "
                        + "AND a.tt_jta_activity_id IS NULL "
                        + "AND a.learn_group_name IS NOT NULL "
                        + "AND m.learn_course_id IS NOT NULL "
                        + "AND a.tt_activity_id IN (SELECT DISTINCT tt_activity_id FROM enrolment_add WHERE run_id=?)"
            );
            try {
                queryStatement.setInt(1, run.getRunId());
                final ResultSet rs = queryStatement.executeQuery();
                try {
                    while (rs.next()) {
                        final Id courseId = Id.generateId(Course.DATA_TYPE, rs.getString("learn_course_id"));
                        final Group group = persistGroup(courseId, rs.getString("learn_group_name"), groupDbPersister);
                        
                        final String activityId = rs.getString("tt_activity_id");
                        
                        updateStatement.setString(1, group.getId().toExternalString());
                        updateStatement.setString(2, activityId);
                        updateStatement.executeUpdate();
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
                        + "JOIN activity p ON p.tt_activity_id=a.tt_jta_activity_id "
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
                        final Id courseId = Id.generateId(Course.DATA_TYPE, rs.getString("learn_course_id"));
                        final Group group = persistGroup(courseId, rs.getString("learn_group_name"), groupDbPersister);
                        
                        final String activityId = rs.getString("tt_activity_id");
                        
                        updateStatement.setString(1, group.getId().toExternalString());
                        updateStatement.setString(2, activityId);
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
        final CourseDbLoader courseDbLoader = CourseDbLoader.Default.getInstance();
        final CourseCourseDbLoader courseCourseDbLoader = CourseCourseDbLoader.Default.getInstance();
        
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

    private Group persistGroup(final Id courseId, final String groupName, final GroupDbPersister groupDbPersister) throws ValidationException, PersistenceException {
        // Create the new group
        final Group group = new Group();
        group.setCourseId(courseId);
        group.setTitle(groupName);
        group.setIsAvailable(false);
        group.setSelfEnrolledAllowed(false);
        // FIXME: group.setDescription(null);
        groupDbPersister.persist(group);
        return group;
    }
}
