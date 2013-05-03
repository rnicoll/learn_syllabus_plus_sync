package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import blackboard.data.course.Course;
import blackboard.data.course.CourseCourse;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseDbLoader;
import blackboard.persist.course.CourseCourseDbLoader;
import blackboard.persist.course.GroupDbPersister;
import org.springframework.stereotype.Service;


/**
 * Service for interacting with Blackboard Learn.
 */
@Service
public class BlackboardService {
    /**
     * Generates groups for activities in timetabling that can be mapped to courses
     * in Learn.
     */
    public void generateGroupsForActivities(final Connection connection)
            throws KeyNotFoundException, PersistenceException, SQLException {
        //final CourseDbLoader courseDbLoader = CourseDbLoader.Default.getInstance();
        //final GroupDbPersister groupDbPersister = GroupDbPersister.Default.getInstance();
        
        // First resolve all activities that have a Learn course, but no group,
        // and are not JTA child activities.
        final PreparedStatement statement = connection.prepareStatement(
                "SELECT a.tt_activity_id, a.tt_activity_name, a.learn_group_id, a.learn_group_name, m.tt_module_name, m.learn_course_id, t.tt_type_name "
                    + "FROM activity a "
                        + "JOIN module m ON m.tt_module_id=a.tt_module_id "
                        + "JOIN activity_type t ON t.tt_type_id=a.tt_type_id "
                    + "WHERE a.learn_group_id IS NULL "
                        + "AND a.tt_jta_activity_id IS NULL "
                        + "AND m.learn_course_id IS NOT NULL",
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE
        );
        try {
            final ResultSet rs = statement.executeQuery();
            try {
                while (rs.next()) {
                    final String activityName = rs.getString("tt_activity_name");
                    final String moduleName = rs.getString("tt_module_name");
                    final String activityType = rs.getString("tt_type_name");
                    final String groupName = buildGroupName(activityName, moduleName, activityType);
                    
                    if (null == groupName) {
                        continue;
                    }
                    
                    rs.updateString("learn_group_name", groupName);
                    
                    /* final Id courseId = Id.generateId(Course.DATA_TYPE, rs.getString("learn_course_id"));
                    final Course course;
                    
                    try {
                        course = courseDbLoader.loadById(courseId);
                    } catch(KeyNotFoundException e) {
                        // Course has been removed since this record was written.
                        // XXX: Should notify the user in some manner
                    } */                   
                    
                    rs.updateRow();
                }
            } finally {
                rs.close();
            }
        } finally {
            statement.close();
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

    /**
     * Builds a group name based the name of the activity and module, and the
     * activity type.
     * 
     * @param activityName the name of the activity in timetabling.
     * @param moduleName the name of the module the activity belongs to, in timetabling.
     * @param activityType the name of the type of activity (lecture, tutorial, etc).
     * @return a group name, or null if no name could be generated (for example
     * missing data).
     */
    private String buildGroupName(String activityName, String moduleName, String activityType) {
        if (null == activityName) {
            return null;
        }
        
        final StringBuilder groupName = new StringBuilder("TT_");
        
        // The ID of a group, for example a number to identify it within its
        // set.
        final String groupId;
        
        // In many cases, the activity name will have the module name at the
        // start. In whic case, we strip the module name to give us a group
        // ID.
        //
        // Failing that, we look for a sensible break point and hope!
        if (null != moduleName
            && activityName.startsWith(moduleName)) {
            final String temp = activityName.substring(moduleName.length());
            if (temp.startsWith("/")) {
                groupId = temp.substring(1);
            } else {
                groupId = temp;
            }
        } else {
            // If the activity name has a '/' in, split on the last '/'
            // character.
            if (activityName.lastIndexOf("/") >= 0) {
                groupId = activityName.substring(activityName.lastIndexOf("/") + 1);
            } else if (activityName.lastIndexOf(" ") >= 0) {
                groupId = activityName.substring(activityName.lastIndexOf(" ") + 1);
            } else {
                groupId = activityName;
            }
        }
        
        if (null != activityType) {
            groupName.append(activityType.trim())
                .append("_");
        }
        
        groupName.append(groupId.trim());
        
        return groupName.toString();
    }
}
