package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.ac.ed.learn9.bb.timetabling.data.BlackboardCourseCode;

/**
 * Service for communicating with the "merged courses" database.
 */
@Service
public class MergedCoursesService {
    /**
     * Fetches details of all courses that are merged together to form a
     * course. Where the given parent course doesn't have any courses
     * merged to create it, an empty list is returned.
     *
     * @param parentCourseCode
     * @return 
     */
    public List<BlackboardCourseCode> getMergedCourses(final BlackboardCourseCode parentCourseCode) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Returns the course that this course is 
     * @param childCourseCode
     * @return 
     */
    public BlackboardCourseCode getParentCourse(final BlackboardCourseCode childCourseCode) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Resolves the courses in Learn that activities map to, and stores the
     * corrected associations in the database.
     * 
     * @param source 
     */
    public void resolveMergedCourses(final Connection source) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
