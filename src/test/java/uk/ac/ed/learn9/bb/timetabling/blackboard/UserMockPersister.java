package uk.ac.ed.learn9.bb.timetabling.blackboard;

import java.sql.Connection;

import blackboard.base.AppVersion;
import blackboard.data.ValidationException;
import blackboard.data.user.User;
import blackboard.persist.user.UserDbPersister;
import blackboard.persist.BbPersistenceManager;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;

/**
 *
 * @author jnicoll2
 */
public class UserMockPersister implements UserDbPersister {
    private AppVersion appVersion;
    private MockPersistenceManager persistenceManager;
    
                UserMockPersister(final MockPersistenceManager manager) {
        this.persistenceManager = manager;
    }

    @Override
    public void persist(User course) throws ValidationException, PersistenceException {
        this.persistenceManager.getUserLoader().addUser(course);
    }

    @Override
    public void persist(User course, Connection cnctn) throws ValidationException, PersistenceException {
        this.persist(course);
    }

    @Override
    public void deleteById(Id id) throws KeyNotFoundException, PersistenceException {
        this.persistenceManager.getUserLoader().removeUserById(id);
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
