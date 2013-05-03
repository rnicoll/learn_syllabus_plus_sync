package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import blackboard.data.course.Course;
import blackboard.data.course.CourseCourse;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseDbLoader;
import blackboard.persist.course.CourseCourseDbLoader;
import org.springframework.stereotype.Service;


/**
 * Service for interacting with Blackboard Learn.
 */
@Service
public class BlackboardService {
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
}
