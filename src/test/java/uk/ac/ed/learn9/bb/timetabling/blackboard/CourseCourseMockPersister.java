package uk.ac.ed.learn9.bb.timetabling.blackboard;

import java.sql.Connection;

import blackboard.base.AppVersion;
import blackboard.data.ValidationException;
import blackboard.data.course.CourseCourse;
import blackboard.persist.BbPersistenceManager;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseCourseDbPersister;

/**
 *
 * @author jnicoll2
 */
public class CourseCourseMockPersister implements CourseCourseDbPersister {
    private AppVersion appVersion;
    private MockPersistenceManager persistenceManager;
    
                CourseCourseMockPersister(final MockPersistenceManager manager) {
        this.persistenceManager = manager;
    }

    @Override
    public void persist(final CourseCourse course) throws ValidationException, PersistenceException {
        this.persistenceManager.getCourseCourseLoader().addCourseCourse(course);
    }

    @Override
    public void persist(final CourseCourse course, Connection cnctn) throws ValidationException, PersistenceException {
        this.persist(course);
    }

    @Override
    public void deleteById(Id id) throws KeyNotFoundException, PersistenceException {
        this.persistenceManager.getCourseCourseLoader().removeCourseCourseById(id);
    }

    @Override
    public void deleteById(Id id, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        this.deleteById(id);
    }

    @Override
    public void init(BbPersistenceManager bpm, AppVersion av) {
        this.appVersion = av;
    }

    @Override
    public AppVersion getAppVersion() {
        return this.appVersion;
    }
    
}
