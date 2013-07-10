package uk.ac.ed.learn9.bb.timetabling.blackboard;

import java.sql.Connection;

import blackboard.base.AppVersion;
import blackboard.data.ValidationException;
import blackboard.data.course.GroupMembership;
import blackboard.persist.BbPersistenceManager;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.GroupMembershipDbPersister;

/**
 *
 * @author jnicoll2
 */
public class GroupMembershipMockPersister implements GroupMembershipDbPersister {
    private AppVersion appVersion;
    private MockPersistenceManager persistenceManager;
    
                GroupMembershipMockPersister(final MockPersistenceManager manager) {
        this.persistenceManager = manager;
    }

    @Override
    public void persist(final GroupMembership groupMembership) throws ValidationException, PersistenceException {
        this.persistenceManager.getGroupMembershipLoader().addGroupMembership(groupMembership);
    }

    @Override
    public void persist(final GroupMembership groupMembership, Connection cnctn) throws ValidationException, PersistenceException {
        this.persist(groupMembership);
    }

    @Override
    public void deleteById(Id id) throws KeyNotFoundException, PersistenceException {
        this.persistenceManager.getGroupMembershipLoader().removeGroupMembershipById(id);
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
