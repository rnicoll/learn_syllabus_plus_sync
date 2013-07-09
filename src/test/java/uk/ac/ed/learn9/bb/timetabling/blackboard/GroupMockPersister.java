package uk.ac.ed.learn9.bb.timetabling.blackboard;

import java.sql.Connection;

import blackboard.base.AppVersion;
import blackboard.data.ValidationException;
import blackboard.data.course.Group;
import blackboard.persist.course.GroupDbPersister;
import blackboard.persist.BbPersistenceManager;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;

/**
 *
 * @author jnicoll2
 */
public class GroupMockPersister implements GroupDbPersister {
    private AppVersion appVersion;
    private MockPersistenceManager persistenceManager;
    
                GroupMockPersister(final MockPersistenceManager manager) {
        this.persistenceManager = manager;
    }

    @Override
    public void persist(Group group) throws ValidationException, PersistenceException {
        this.persistenceManager.getGroupLoader().addGroup(group);
    }

    @Override
    public void persist(Group group, Connection cnctn) throws ValidationException, PersistenceException {
        this.persist(group);
    }

    @Override
    public void deleteById(Id id) throws KeyNotFoundException, PersistenceException {
        this.persistenceManager.getGroupLoader().removeGroupById(id);
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
