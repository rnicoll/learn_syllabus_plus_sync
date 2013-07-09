package uk.ac.ed.learn9.bb.timetabling.blackboard;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blackboard.base.AppVersion;
import blackboard.base.BbList;
import blackboard.data.course.Group;
import blackboard.persist.BbPersistenceManager;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.GroupDbLoader;

/**
 *
 * @author jnicoll2
 */
public class GroupMockLoader implements GroupDbLoader {
    private AppVersion appVersion;
    private final Map<Id, Group> groupById = new HashMap<Id, Group>();
    private final Map<Id, List<Group>> groupByCourseId = new HashMap<Id, List<Group>>();
    private MockPersistenceManager persistenceManager;
    
    public                  GroupMockLoader(final MockPersistenceManager setPersistenceManager) {
        this.persistenceManager = setPersistenceManager;
    }
    
    /**
     * Add a group to the mock loader.
     * 
     * @param group group to be stored ready for loading.
     */
    public void addGroup(final Group group) {
        assert null != group;
        assert null != group.getId();
        assert null != group.getCourseId();
        
        this.groupById.put(group.getId(), group);
        List<Group> groupsInCourse = this.groupByCourseId.get(group.getCourseId());
        
        if (null == groupsInCourse) {
            groupsInCourse = new ArrayList<Group>();
            this.groupByCourseId.put(group.getCourseId(), groupsInCourse);
        }
        
        groupsInCourse.add(group);
    }

    @Override
    public Group loadById(Id id) throws KeyNotFoundException, PersistenceException {
        final Group group = this.groupById.get(id);
        
        if (null == group) {
            throw new KeyNotFoundException("Could not find group \""
                + id.getExternalString() + "\".");
        } else {
            return group;
        }
    }

    @Override
    public Group loadById(Id id, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadById(id);
    }

    @Override
    public BbList<Group> loadByCourseId(final Id courseId) throws KeyNotFoundException, PersistenceException {
        return new BbList<Group>(this.internalLoadByCourseId(courseId));
    }
    
    private List<Group> internalLoadByCourseId(final Id courseId) throws KeyNotFoundException, PersistenceException {
        final List<Group> groups = this.groupByCourseId.get(courseId);
        
        if (null == groups) {
            // This throws KeyNotFoundException for us if the course ID is invalid.
            this.persistenceManager.getCourseLoader().loadById(courseId);
            
            return Collections.EMPTY_LIST;
        } else {
            return groups;
        }
    }

    @Override
    public BbList<Group> loadByCourseId(final Id courseId, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadByCourseId(courseId);
    }

    @Override
    public List<Group> loadAvailableByCourseId(final Id courseId) throws KeyNotFoundException, PersistenceException {
        final List<Group> candidates = this.internalLoadByCourseId(courseId);
        final List<Group> availableGroups = new ArrayList<Group>(candidates.size());
        
        for (Group group: candidates) {
            if (group.getIsAvailable()) {
                availableGroups.add(group);
            }
        }
        
        return availableGroups;
    }

    @Override
    public List<Group> loadAvailableByCourseId(final Id courseId, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadAvailableByCourseId(courseId);
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
