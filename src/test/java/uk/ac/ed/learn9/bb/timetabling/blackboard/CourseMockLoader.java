package uk.ac.ed.learn9.bb.timetabling.blackboard;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blackboard.base.AppVersion;
import blackboard.base.BbList;
import blackboard.data.course.Course;
import blackboard.data.course.Course.ServiceLevel;
import blackboard.data.course.CourseMembership.Role;
import blackboard.persist.BbPersistenceManager;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseDbLoader;
import blackboard.persist.course.CourseSearch;

/**
 * Mock user loading class for use in automated testing.
 */
public class CourseMockLoader implements CourseDbLoader {
    private AppVersion appVersion;
    private final Map<Id, Course> courseById = new HashMap<Id, Course>();
    private final Map<String, Course> courseByCourseId = new HashMap<String, Course>();
    private Course systemCourse = null;
    private MockPersistenceManager persistenceManager;
    
    public                  CourseMockLoader(final MockPersistenceManager setPersistenceManager) {
        this.persistenceManager = setPersistenceManager;
    }
    
    /**
     * Add a course to the mock loader.
     * 
     * @param course course to be stored ready for loading.
     */
    public void addCourse(final Course course) {
        assert null != course;
        assert null != course.getId();
        assert null != course.getCourseId();
        
        this.courseById.put(course.getId(), course);
        this.courseByCourseId.put(course.getCourseId(), course);
    }
    
    public void setSystemCourse(final Course newSystemCourse) {
        this.systemCourse = newSystemCourse;
    }
 
    @Override
    public Course loadById(Id id) throws KeyNotFoundException, PersistenceException {
        final Course course = this.courseById.get(id);
        
        if (null == course) {
            throw new KeyNotFoundException("Could not find course \""
                + id.getExternalString() + "\".");
        } else {
            return course;
        }
    }

    @Override
    public Course loadById(Id id, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadById(id);
    }

    @Override
    public Course loadById(Id id, Connection cnctn, boolean bHeavy) throws KeyNotFoundException, PersistenceException {
        return this.loadById(id);
    }

    @Override
    public Course loadByCourseId(final String courseId) throws KeyNotFoundException, PersistenceException {
        final Course course = this.courseByCourseId.get(courseId);
        
        if (null == course) {
            throw new KeyNotFoundException("Could not find course \""
                + courseId + "\".");
        } else {
            return course;
        }
    }

    @Override
    public boolean doesCourseIdExist(final String courseId) {
        return this.courseByCourseId.containsKey(courseId);
    }

    @Override
    public boolean doesCourseIdExist(String string, Connection cnctn) {
        return this.doesCourseIdExist(string);
    }

    @Override
    public Course loadByCourseId(String courseId, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadByCourseId(courseId);
    }

    @Override
    public Course loadByCourseId(String courseId, Connection cnctn, boolean bHeavy) throws KeyNotFoundException, PersistenceException {
        return this.loadByCourseId(courseId);
    }

    @Override
    public BbList<Course> loadByUserId(final Id userId) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<Course> loadByUserId(final Id userId, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<Course> loadByUserId(final Id userId, Connection cnctn, boolean bHeavy) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<Course> loadByUserIdAndSortColumns(Id id, List<String> list, ServiceLevel sl) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<Course> loadByUserIdAndSortColumns(Id id, List<String> list, ServiceLevel sl, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<Course> loadByUserIdAndDirectEnrollments(Id id) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<Course> loadByUserIdAndDirectEnrollments(Id id, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<Course> loadByServiceLevelAndRoles(Id id, ServiceLevel sl, List<Role> list) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<Course> loadByUserIdAndCourseMembershipRole(Id id, Role role) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<Course> loadByUserIdAndCourseMembershipRole(Id id, Role role, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<Course> loadByUserIdAndCourseMembershipRole(Id id, Role role, Connection cnctn, boolean bHeavy) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<Course> loadByUserIdForLearners(Id id) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<Course> loadByUserIdForLearners(Id id, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<Course> loadByUserIdForLearners(Id id, Connection cnctn, boolean bHeavy) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Course loadByBatchUid(final String batchUid) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Course loadByBatchUid(final String batchUid, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Course loadByBatchUid(final String batchUid, Connection cnctn, boolean bHeavy) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<Course> loadByDataSourceBatchUid(final String batchUid) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<Course> loadByDataSourceBatchUid(final String batchUid, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<Course> loadAllCourses() throws KeyNotFoundException, PersistenceException {
        return new BbList<Course>(this.courseById.values());
    }

    @Override
    public BbList<Course> loadAllCourses(Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadAllCourses();
    }

    @Override
    public BbList<Course> loadAllByServiceLevel(ServiceLevel sl) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<Course> loadAllByServiceLevel(ServiceLevel sl, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadAllByServiceLevel(sl);
    }

    @Override
    public Course loadSystemCourse() throws KeyNotFoundException, PersistenceException {
        return this.systemCourse;
    }

    @Override
    public Course loadSystemCourse(Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadSystemCourse();
    }

    @Override
    public BbList<Course> loadCourseAndInstructorListByServiceLevel(ServiceLevel sl) throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<Course> loadCourseAndInstructorListByServiceLevel(ServiceLevel sl, Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadCourseAndInstructorListByServiceLevel(sl);
    }

    @Override
    public BbList<Course> loadTemplates() throws KeyNotFoundException, PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<Course> loadTemplates(Connection cnctn) throws KeyNotFoundException, PersistenceException {
        return this.loadTemplates();
    }

    @Override
    public List<Course> loadByCourseSearch(CourseSearch cs) throws PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<Course> loadByCourseCategoryId(Id id) throws PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<Course> loadByCourseCategoryId(Id id, Connection cnctn) throws PersistenceException {
        return this.loadByCourseCategoryId(id);
    }

    @Override
    public BbList<Course> loadByOrgCategoryId(Id id) throws PersistenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BbList<Course> loadByOrgCategoryId(Id id, Connection cnctn) throws PersistenceException {
        return this.loadByOrgCategoryId(id);
    }

    @Override
    public void init(BbPersistenceManager bpm, final AppVersion av) {
        this.appVersion = av;
    }

    @Override
    public AppVersion getAppVersion() {
        return this.appVersion;
    }
}
