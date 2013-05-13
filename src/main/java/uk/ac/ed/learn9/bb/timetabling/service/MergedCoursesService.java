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
    public List<BlackboardCourseCode> getMergedCourses(final BlackboardCourseCode courseCode) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    public void resolveMergedCourses(final Connection source) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
