package uk.ac.ed.learn9.bb.timetabling.blackboard;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blackboard.base.AppVersion;
import blackboard.data.course.CourseCourse;
import blackboard.persist.BbPersistenceManager;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseCourseDbLoader;

/**
 *
 * @author jnicoll2
 */
public class CourseCourseMockLoader extends Object implements CourseCourseDbLoader {
    private AppVersion appVersion;
    private final MockPersistenceManager persistenceManager;
    private final Map<Id, CourseCourse> courseCoursesById = new HashMap<Id, CourseCourse>();
    private final Map<Id, CourseCourse> courseCoursesByChildId = new HashMap<Id, CourseCourse>();
    private final Map<Id, List<CourseCourse>> courseCoursesByParentId = new HashMap<Id, List<CourseCourse>>();
    
                            CourseCourseMockLoader(final MockPersistenceManager manager) {
        this.persistenceManager = manager;
    }
                            
    protected void addCourseCourse(final CourseCourse relationship) throws PersistenceException {
        this.courseCoursesById.put(relationship.getId(), relationship);
        this.courseCoursesByChildId.put(relationship.getChildCourseId(), relationship);
        
        List<CourseCourse> childRelationships = this.courseCoursesByParentId.get(relationship.getParentCourseId());
        
        if (null == childRelationships) {
            childRelationships = new ArrayList<CourseCourse>();
            this.courseCoursesByParentId.put(relationship.getParentCourseId(), childRelationships);
        }
        
        childRelationships.add(relationship);
    }

    protected void removeCourseCourseById(Id id) {
        final CourseCourse relationship = this.courseCoursesById.get(id);
        
        if (null == relationship) {
            return;
        }
        this.courseCoursesById.remove(id);
        this.courseCoursesByChildId.remove(relationship.getChildCourseId());
        
        final List<CourseCourse> childRelationships = this.courseCoursesByParentId.get(relationship.getParentCourseId());
        
        childRelationships.remove(relationship);
    }

    @Override
    public CourseCourse loadById(Id id) throws PersistenceException {
        final CourseCourse relationship = this.courseCoursesById.get(id);
        
        if (null == relationship) {
            throw new KeyNotFoundException("Could not find course-course relationship \""
                + id.getExternalString() + "\".");
        } else {
            return relationship;
        }
    }

    @Override
    public CourseCourse loadById(Id id, Connection cnctn) throws PersistenceException {
        return this.loadById(id);
    }

    @Override
    public List<CourseCourse> loadParents(final Id id) throws PersistenceException {
        return Collections.singletonList(loadParent(id));
    }

    @Override
    public List<CourseCourse> loadParents(final Id id, final Connection cnctn) throws PersistenceException {
        return this.loadByParentId(id);
    }

    @Override
    public CourseCourse loadParent(final Id childId) throws KeyNotFoundException, PersistenceException {
        final CourseCourse relationship = this.courseCoursesByChildId.get(childId);
        
        if (null == relationship) {
            throw new KeyNotFoundException("Could not find course-course relationship \""
                + childId.getExternalString() + "\".");
        } else {
            return relationship;
        }
    }

    @Override
    public CourseCourse loadParent(final Id id, final Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadParent(id);
    }

    @Override
    public CourseCourse loadByParentChildIds(final Id parentId, final Id childId) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CourseCourse loadByParentChildIds(final Id parentId, final Id childId, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadByParentChildIds(parentId, childId);
    }

    @Override
    public List<CourseCourse> loadByParentId(Id id) throws PersistenceException {
        final List<CourseCourse> relationships = this.courseCoursesByParentId.get(id);
        
        if (null == relationships) {
            // Verify the course ID - if it's invalid this throws KeyNotFoundException
            this.persistenceManager.getCourseLoader().loadById(id);
            return Collections.EMPTY_LIST;
        } else {
            return Collections.unmodifiableList(relationships);
        }
    }

    @Override
    public List<CourseCourse> loadByParentId(Id id, Connection cnctn) throws PersistenceException {
        return this.loadByParentId(id);
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
