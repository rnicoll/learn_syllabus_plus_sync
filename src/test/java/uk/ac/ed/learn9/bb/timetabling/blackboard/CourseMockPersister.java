package uk.ac.ed.learn9.bb.timetabling.blackboard;

import java.sql.Connection;

import blackboard.base.AppVersion;
import blackboard.data.ValidationException;
import blackboard.data.course.Course;
import blackboard.persist.course.CourseDbPersister;
import blackboard.persist.BbPersistenceManager;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;

/**
 *
 * @author jnicoll2
 */
public class CourseMockPersister implements CourseDbPersister {
    private AppVersion appVersion;
    private MockPersistenceManager persistenceManager;
    
                CourseMockPersister(final MockPersistenceManager manager) {
        this.persistenceManager = manager;
    }

    @Override
    public void persist(Course course) throws ValidationException, PersistenceException {
        this.persistenceManager.getCourseLoader().addCourse(course);
    }

    @Override
    public void persist(Course course, Connection cnctn) throws ValidationException, PersistenceException {
        this.persist(course);
    }

    @Override
    public void deleteById(Id id) throws KeyNotFoundException, PersistenceException {
        this.persistenceManager.getCourseLoader().removeCourseById(id);
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
