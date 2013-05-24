package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.util.Collection;
import org.springframework.stereotype.Service;
import uk.ac.ed.learn9.bb.timetabling.data.LearnCourseCode;

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
     * @param parentCourseCode the course code of the parent course to check
     * for courses merged into.
     * @return a collection of courses merged into the given course. Empty (not null)
     * if there are no merged courses under the given course.
     */
    public Collection<LearnCourseCode> getMergedCourses(final LearnCourseCode parentCourseCode) {
        assert null != parentCourseCode;
        
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Returns the course that a child course is merged into, where applicable.
     * 
     * @param childCourseCode the course code of the child course to check for.
     * @return the course code of the parent course, or null if the course
     * is not merged.
     */
    public LearnCourseCode getParentCourse(final LearnCourseCode childCourseCode) {
        assert null != childCourseCode;
        
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Resolves the courses in Learn that activities map to, and stores the
     * corrected associations in the database.
     * 
     * @param stagingDatabase a connection to the staging database.
     */
    public void resolveMergedCourses(final Connection stagingDatabase) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
