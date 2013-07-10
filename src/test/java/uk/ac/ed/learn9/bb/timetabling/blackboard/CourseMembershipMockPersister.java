package uk.ac.ed.learn9.bb.timetabling.blackboard;

import java.sql.Connection;

import blackboard.base.AppVersion;
import blackboard.data.ValidationException;
import blackboard.data.course.CourseMembership;
import blackboard.persist.BbPersistenceManager;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseMembershipDbPersister;

/**
 *
 * @author jnicoll2
 */
public class CourseMembershipMockPersister implements CourseMembershipDbPersister {
    private AppVersion appVersion;
    private MockPersistenceManager persistenceManager;
    
                CourseMembershipMockPersister(final MockPersistenceManager manager) {
        this.persistenceManager = manager;
    }

    @Override
    public void persist(final CourseMembership courseMembership) throws ValidationException, PersistenceException {
        this.persistenceManager.getCourseMembershipLoader().addCourseMembership(courseMembership);
    }

    @Override
    public void persist(final CourseMembership courseMembership, Connection cnctn) throws ValidationException, PersistenceException {
        this.persist(courseMembership);
    }

    @Override
    public void deleteById(Id id) throws KeyNotFoundException, PersistenceException {
        this.persistenceManager.getCourseMembershipLoader().removeCourseMembershipById(id);
    }

    @Override
    public void deleteById(Id id, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        this.deleteById(id);
    }

    @Override
    public void deleteByCourseId(Id id) throws KeyNotFoundException, PersistenceException {
        this.persistenceManager.getCourseMembershipLoader().removeCourseMembershipByCourseId(id);
    }

    @Override
    public void deleteByCourseId(Id id, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        this.deleteByCourseId(id);
    }

    @Override
    public void deleteByBatchUID(final String batchUid) throws KeyNotFoundException, PersistenceException {
        this.persistenceManager.getCourseMembershipLoader().removeCourseMembershipByBatchUid(batchUid);
    }

    @Override
    public void deleteByBatchUID(final String batchUid, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        this.deleteByBatchUID(batchUid);
    }

    @Override
    public void init(BbPersistenceManager bpm, AppVersion av) {
        this.appVersion = av;
    }

    @Override
    public AppVersion getAppVersion() {
        return this.appVersion;
    }

    @Override
    public void persistLastAccess(CourseMembership cm) throws ValidationException, PersistenceException {
        // This happens automatically in this implementation
    }
    
}
