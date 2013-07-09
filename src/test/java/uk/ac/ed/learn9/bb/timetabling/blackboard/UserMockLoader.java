package uk.ac.ed.learn9.bb.timetabling.blackboard;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blackboard.base.AppVersion;
import blackboard.base.BbList;
import blackboard.data.user.User;
import blackboard.persist.BbPersistenceManager;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.impl.mapping.DbObjectMap;
import blackboard.persist.user.UserDbLoader;
import blackboard.persist.user.UserSearch;
import blackboard.persist.user.UserSearch.SearchParameter;

/**
 * Mock user loading class for use in automated testing.
 */
public class UserMockLoader implements UserDbLoader {
    private AppVersion appVersion;
    private final Map<Id, User> userById = new HashMap<Id, User>();
    private final Map<String, User> userByUsername = new HashMap<String, User>();
    private User guestUser;
    private MockPersistenceManager persistenceManager;
    
                            UserMockLoader(final MockPersistenceManager setPersistenceManager) {
        this.persistenceManager = setPersistenceManager;
    }
    
    /**
     * Add a user to the mock loader.
     * 
     * @param user user to be stored ready for loading.
     */
    protected void addUser(final User user) {
        assert null != user;
        assert null != user.getId();
        assert null != user.getUserName();
        
        this.userById.put(user.getId(), user);
        this.userByUsername.put(user.getUserName(), user);
    }
    
    public void setGuestUser(final User user) {
        this.guestUser = user;
    }

    @Override
    public User loadById(Id id) throws KeyNotFoundException, PersistenceException {
        final User user = this.userById.get(id);
        
        if (null == user) {
            throw new KeyNotFoundException("Could not find user \""
                + id.getExternalString() + "\".");
        } else {
            return user;
        }
    }

    @Override
    public User loadById(Id id, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadById(id);
    }

    @Override
    public User loadById(Id id, Connection cnctn, boolean bHeavy) throws KeyNotFoundException, PersistenceException {
        return this.loadById(id);
    }

    @Override
    public User loadById(Id id, Connection cnctn, boolean bln, DbObjectMap dom) throws KeyNotFoundException, PersistenceException {
        return this.loadById(id);
    }

    @Override
    public BbList<User> loadByEmailAddressFamilyNameGivenName(final String emailAddress, final String familyName, final String givenName) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<User> loadByEmailAddressFamilyNameGivenName(final String emailAddress, final String familyName, final String givenName, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadByEmailAddressFamilyNameGivenName(emailAddress, familyName, givenName);
    }

    @Override
    public BbList<User> loadByStudentIdFamilyNameGivenName(final String studentId, final String familyName, final String givenName, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<User> loadByCardNumberFamilyNameGivenName(final String cardNumber, final String familyName, final String givenName, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<User> searchByUserName(final String username) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public User loadByUserName(final String username) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public User loadByUserName(final String username, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        final User user = this.userByUsername.get(username);
        
        if (null == user) {
            throw new KeyNotFoundException("Could not find user \""
                + username + "\".");
        } else {
            return user;
        }
    }

    @Override
    public User loadByUserName(final String username, Connection cnctn, boolean bln) throws KeyNotFoundException, PersistenceException {
        return this.loadByUserName(username);
    }

    @Override
    public BbList<User> loadByCourseId(Id id) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<User> loadByCourseId(Id id, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadByCourseId(id);
    }

    @Override
    public BbList<User> loadByCourseId(Id id, Connection cnctn, boolean bln) throws KeyNotFoundException, PersistenceException {
        return this.loadByCourseId(id);
    }

    @Override
    public BbList<User> loadByGroupId(Id id) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<User> loadByGroupId(Id id, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadByGroupId(id);
    }

    @Override
    public BbList<User> loadByGroupId(Id id, Connection cnctn, boolean bln) throws KeyNotFoundException, PersistenceException {
        return this.loadByGroupId(id);
    }

    @Override
    public User loadByBatchUid(final String batchUid) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public User loadByBatchUid(final String batchUid, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadByBatchUid(batchUid);
    }

    @Override
    public User loadByBatchUid(final String batchUid, Connection cnctn, boolean bln) throws KeyNotFoundException, PersistenceException {
        return this.loadByBatchUid(batchUid);
    }

    @Override
    public BbList<User> loadObservedByObserverId(final Id observerId) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<User> loadObservedByObserverId(final Id observerId, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadObservedByObserverId(observerId);
    }

    @Override
    public BbList<User> loadObservedByObserverId(final Id observerId, Connection cnctn, boolean bHeavy) throws KeyNotFoundException, PersistenceException {
        return this.loadObservedByObserverId(observerId);
    }

    @Override
    public BbList<User> loadByPrimaryPortalRoleId(Id id) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<User> loadByPrimaryPortalRoleId(Id id, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadByPrimaryPortalRoleId(id);
    }

    @Override
    public BbList<User> loadByPrimaryPortalRoleId(Id id, Connection cnctn, boolean bln) throws KeyNotFoundException, PersistenceException {
        return this.loadByPrimaryPortalRoleId(id);
    }

    @Override
    public List<User> loadByPortalRoleId(Id id) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public User loadUserByUserRole(Id id) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public User loadUserByCourseMembership(Id id) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<User> loadByUserSearch(UserSearch us) throws PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public User loadGuestUser() throws KeyNotFoundException, PersistenceException {
        return this.guestUser;
    }

    @Override
    public List<User> loadUsersForUserDirectory(DbObjectMap dom, SearchParameter sp) throws PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<User> loadUsersForUserDirectory(DbObjectMap dom, SearchParameter sp, Connection cnctn) throws PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
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
