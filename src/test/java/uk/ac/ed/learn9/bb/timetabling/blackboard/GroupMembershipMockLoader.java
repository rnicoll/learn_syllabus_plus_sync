package uk.ac.ed.learn9.bb.timetabling.blackboard;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blackboard.base.AppVersion;
import blackboard.base.BbList;
import blackboard.data.course.CourseMembership;
import blackboard.data.course.CourseMembership.Role;
import blackboard.data.course.GroupMembership;
import blackboard.persist.BbPersistenceManager;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.GroupMembershipDbLoader;

/**
 *
 * @author jnicoll2
 */
public class GroupMembershipMockLoader implements GroupMembershipDbLoader {
    private AppVersion appVersion;
    private final MockPersistenceManager persistenceManager;
    private final Map<Id, GroupMembership> groupMembershipsById = new HashMap<Id, GroupMembership>();
    private final Map<Id, List<GroupMembership>> groupMembershipsByCourseId = new HashMap<Id, List<GroupMembership>>();
    private final Map<Id, List<GroupMembership>> groupMembershipsByGroupId = new HashMap<Id, List<GroupMembership>>();
    
                            GroupMembershipMockLoader(final MockPersistenceManager manager) {
        this.persistenceManager = manager;
    }
                            
    protected void addGroupMembership(final GroupMembership membership) throws PersistenceException {
        this.groupMembershipsById.put(membership.getId(), membership);
        
        final CourseMembership courseMembership = this.persistenceManager
                .getCourseMembershipLoader()
                .loadById(membership.getCourseMembershipId());
        
        List<GroupMembership> groupMemberships = this.groupMembershipsByCourseId.get(courseMembership.getCourseId());
        if (null == groupMemberships) {
            groupMemberships = new ArrayList<GroupMembership>();
            this.groupMembershipsByCourseId.put(courseMembership.getCourseId(), groupMemberships);
        }
        
        groupMemberships.add(membership);
        
        groupMemberships = this.groupMembershipsByGroupId.get(membership.getGroupId());
        if (null == groupMemberships) {
            groupMemberships = new ArrayList<GroupMembership>();
            this.groupMembershipsByGroupId.put(membership.getGroupId(), groupMemberships);
        }
        
        groupMemberships.add(membership);
    }

    protected void removeGroupMembershipById(Id id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public GroupMembership loadById(Id id) throws KeyNotFoundException, PersistenceException {
        final GroupMembership membership = this.groupMembershipsById.get(id);
        
        if (null == membership) {
            throw new KeyNotFoundException("Could not find course membership \""
                + id.getExternalString() + "\".");
        } else {
            return membership;
        }
    }

    @Override
    public GroupMembership loadById(Id id, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadById(id);
    }

    @Override
    public BbList<GroupMembership> loadByCourseId(final Id courseId) throws KeyNotFoundException, PersistenceException {
        final List<GroupMembership> memberships = this.groupMembershipsByCourseId.get(courseId);
        
        if (null == memberships) {
            this.persistenceManager.getCourseLoader().loadById(courseId);
            return new BbList<GroupMembership>();
        } else {
            return new BbList<GroupMembership>(memberships);
        }
    }

    @Override
    public BbList<GroupMembership> loadByCourseId(final Id courseId, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadByCourseId(courseId);
    }

    @Override
    public BbList<GroupMembership> loadByGroupId(final Id groupId) throws KeyNotFoundException, PersistenceException {
        final List<GroupMembership> memberships = this.groupMembershipsByGroupId.get(groupId);
        
        if (null == memberships) {
            this.persistenceManager.getGroupLoader().loadById(groupId);
            return new BbList<GroupMembership>();
        } else {
            return new BbList<GroupMembership>(memberships);
        }
    }

    @Override
    public BbList<GroupMembership> loadByGroupId(final Id groupId, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadByGroupId(groupId);
    }

    @Override
    public GroupMembership loadByGroupAndUserId(final Id groupId, final Id userId) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GroupMembership loadByGroupAndUserId(final Id groupId, final Id userId, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadByGroupAndUserId(groupId, userId);
    }

    @Override
    public List<GroupMembership> loadByGroupAndRole(final Id groupId, Role role) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<GroupMembership> loadByGroupAndRole(final Id groupId, Role role, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadByGroupAndRole(groupId, role);
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
