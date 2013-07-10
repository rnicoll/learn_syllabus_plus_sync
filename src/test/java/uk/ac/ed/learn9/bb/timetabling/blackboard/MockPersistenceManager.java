package uk.ac.ed.learn9.bb.timetabling.blackboard;

import blackboard.data.course.CourseMembership;

/**
 * This object plays loosely the same role as the BbPersistenceManager,
 * in that it tracks the various persisters and loaders.
 */
public class MockPersistenceManager {
    private CourseMockLoader courseLoader;
    private CourseCourseMockLoader courseCourseLoader;
    private CourseMembershipMockLoader courseMembershipLoader;
    private GroupMockLoader groupLoader;
    private GroupMembershipMockLoader groupMembershipLoader;
    private UserMockLoader userLoader;
    
    private             MockPersistenceManager() {
        
    }
    
    public static MockPersistenceManager getInstance() {
        final MockPersistenceManager manager = new MockPersistenceManager();
        
        manager.initialiseLoaders();
        
        return manager;
    }

    private void initialiseLoaders() {
        this.courseLoader = new CourseMockLoader(this);
        this.courseCourseLoader = new CourseCourseMockLoader(this);
        this.courseMembershipLoader = new CourseMembershipMockLoader(this);
        this.groupLoader = new GroupMockLoader(this);
        this.groupMembershipLoader = new GroupMembershipMockLoader(this);
        this.userLoader = new UserMockLoader(this);
    }

    /**
     * @return the courseLoader
     */
    public CourseMockLoader getCourseLoader() {
        return courseLoader;
    }

    public CourseCourseMockLoader getCourseCourseLoader() {
        return this.courseCourseLoader;
    }

    public CourseMembershipMockLoader getCourseMembershipLoader() {
        return this.courseMembershipLoader;
    }

    /**
     * @return the groupMembershipLoader
     */
    public GroupMembershipMockLoader getGroupMembershipLoader() {
        return groupMembershipLoader;
    }

    /**
     * @return the groupLoader
     */
    public GroupMockLoader getGroupLoader() {
        return groupLoader;
    }

    /**
     * @return the userLoader
     */
    public UserMockLoader getUserLoader() {
        return userLoader;
    }
}
