package uk.ac.ed.learn9.bb.timetabling.blackboard;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blackboard.base.AppVersion;
import blackboard.base.BbList;
import blackboard.data.course.Course.ServiceLevel;
import blackboard.data.course.CourseMembership;
import blackboard.data.course.CourseMembership.Role;
import blackboard.data.user.User;
import blackboard.persist.BbPersistenceManager;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseMembershipDbLoader;

/**
 *
 * @author jnicoll2
 */
public class CourseMembershipMockLoader implements CourseMembershipDbLoader {
    private AppVersion appVersion;
    private final MockPersistenceManager persistenceManager;
    private final Map<Id, CourseMembership> courseMembershipsById = new HashMap<Id, CourseMembership>();
    private final Map<Id, List<CourseMembership>> courseMembershipsByCourseId = new HashMap<Id, List<CourseMembership>>();
    
                            CourseMembershipMockLoader(final MockPersistenceManager manager) {
        this.persistenceManager = manager;
    }
                            
    protected void addCourseMembership(final CourseMembership membership) {
        this.courseMembershipsById.put(membership.getId(), membership);
        
        List<CourseMembership> courseMemberships = this.courseMembershipsByCourseId.get(membership.getCourseId());
        if (null == courseMemberships) {
            courseMemberships = new ArrayList<CourseMembership>();
            this.courseMembershipsByCourseId.put(membership.getCourseId(), courseMemberships);
        }
        
        courseMemberships.add(membership);
    }

    protected void removeCourseMembershipById(Id id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    protected void removeCourseMembershipByBatchUid(String batchUid) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    protected void removeCourseMembershipByCourseId(Id id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public CourseMembership loadById(Id id) throws KeyNotFoundException, PersistenceException {
        final CourseMembership membership = this.courseMembershipsById.get(id);
        
        if (null == membership) {
            throw new KeyNotFoundException("Could not find course membership \""
                + id.getExternalString() + "\".");
        } else {
            return membership;
        }
    }

    @Override
    public CourseMembership loadById(Id id, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadById(id);
    }

    @Override
    public CourseMembership loadById(Id id, Connection cnctn, boolean bHeavy) throws KeyNotFoundException, PersistenceException {
        return this.loadById(id);
    }

    @Override
    public List<CourseMembership> loadByIdsAndCourseIdsAndUserIdsAndRoleIds(final List<Id> ids, final List<Id> courseIds, final List<Id> userIds, final List<Role> roles, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadByIdsAndCourseIdsAndUserIdsAndRoleIds(ids, courseIds, userIds, roles);
    }

    @Override
    public List<CourseMembership> loadByIdsAndCourseIdsAndUserIdsAndRoleIds(final List<Id> ids, final List<Id> courseIds, final List<Id> userIds, final List<Role> roles) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<CourseMembership> loadByUserId(final Id userId) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<CourseMembership> loadByUserId(final Id userId, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadByUserId(userId);
    }

    @Override
    public BbList<CourseMembership> loadByUserId(final Id userId, Connection cnctn, boolean bHeavy, boolean bHeavy1) throws KeyNotFoundException, PersistenceException {
        return this.loadByUserId(userId);
    }

    @Override
    public BbList<CourseMembership> loadByUserIdAndRoles(Id id, ServiceLevel sl, List<Role> roles) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CourseMembership loadByBatchUID(final String batchUid, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadByBatchUID(batchUid);
    }

    @Override
    public CourseMembership loadByBatchUID(final String batchUid) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<CourseMembership> loadByCourseId(final Id courseId) throws KeyNotFoundException, PersistenceException {
        final List<CourseMembership> memberships = this.courseMembershipsByCourseId.get(courseId);
        
        if (null == memberships) {
            // This call throws an exception if the course ID is invalid
            this.persistenceManager.getCourseLoader().loadById(courseId);
            return new BbList<CourseMembership>();
        } else {
            return new BbList<CourseMembership>(memberships);
        }
    }

    @Override
    public BbList<CourseMembership> loadByCourseId(final Id courseId, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadByCourseId(courseId);
    }

    @Override
    public BbList<CourseMembership> loadByCourseId(final Id courseId, Connection cnctn, boolean bHeavy) throws KeyNotFoundException, PersistenceException {
        return this.loadByCourseId(courseId);
    }

    @Override
    public BbList<CourseMembership> loadByCourseIdWithUserInfo(final Id courseId) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<CourseMembership> loadByCourseIdWithUserInfo(final Id courseId, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadByCourseIdWithUserInfo(courseId);
    }

    @Override
    public BbList<CourseMembership> loadByCourseIdAndRole(final Id courseId, Role role) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<CourseMembership> loadByCourseIdAndRole(final Id courseId, Role role, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadByCourseIdAndRole(courseId, role);
    }

    @Override
    public BbList<CourseMembership> loadByCourseIdAndRole(final Id courseId, Role role, Connection cnctn, boolean bHeavy) throws KeyNotFoundException, PersistenceException {
        return this.loadByCourseIdAndRole(courseId, role);
    }

    @Override
    public BbList<CourseMembership> loadByCourseIdAndInstructorFlag(final Id courseId) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<CourseMembership> loadByCourseIdAndInstructorFlag(final Id courseId, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadByCourseIdAndInstructorFlag(courseId);
    }

    @Override
    public BbList<CourseMembership> loadByCourseIdAndInstructorFlag(final Id courseId, Connection cnctn, boolean bHeavy) throws KeyNotFoundException, PersistenceException {
        return this.loadByCourseIdAndInstructorFlag(courseId);
    }

    @Override
    public CourseMembership loadByCourseAndUserId(final Id courseId, final Id userId) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isCourseUserEntrollmentAvailable(Id id, Id id1) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isCourseUserExplicitlyDisabled(Id id, Id id1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CourseMembership loadByCourseAndUserId(final Id courseId, final Id userId, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<CourseMembership> loadByCourseAndGroupId(final Id courseId, final Id groupId) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<CourseMembership> loadByCourseAndGroupId(final Id courseId, final Id groupId, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CourseMembership loadByCourseAndUserId(final Id courseId, final Id userId, Connection cnctn, boolean bHeavy) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CourseMembership loadByCourseAndUserId(final Id courseId, final Id userId, Connection cnctn, boolean bHeavy, boolean bHeavy1) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<Id, List<User>> loadInstructorsByUser(final Id userId) throws PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<Id, List<User>> loadInstructorsByUser(final Id userId, Connection cnctn) throws PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<CourseMembership> loadByCourseIdAndRoles(Id id, List<Role> roles) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<CourseMembership> loadByCourseIdAndRoles(Id id, List<Role> roles, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadByCourseIdAndRoles(id, roles);
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
