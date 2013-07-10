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
    
    private CourseMockPersister coursePersister;
    private CourseCourseMockPersister courseCoursePersister;
    private CourseMembershipMockPersister courseMembershipPersister;
    private GroupMockPersister groupPersister;
    private GroupMembershipMockPersister groupMembershipPersister;
    private UserMockPersister userPersister;
    
    private             MockPersistenceManager() {
        
    }
    
    public static MockPersistenceManager getInstance() {
        final MockPersistenceManager manager = new MockPersistenceManager();
        
        manager.initialise();
        
        return manager;
    }

    private void initialise() {
        this.courseLoader = new CourseMockLoader(this);
        this.courseCourseLoader = new CourseCourseMockLoader(this);
        this.courseMembershipLoader = new CourseMembershipMockLoader(this);
        this.groupLoader = new GroupMockLoader(this);
        this.groupMembershipLoader = new GroupMembershipMockLoader(this);
        this.userLoader = new UserMockLoader(this);
        
        this.coursePersister = new CourseMockPersister(this);
        this.courseCoursePersister = new CourseCourseMockPersister(this);
        this.courseMembershipPersister = new CourseMembershipMockPersister(this);
        this.groupPersister = new GroupMockPersister(this);
        this.groupMembershipPersister = new GroupMembershipMockPersister(this);
        this.userPersister = new UserMockPersister(this);
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

    /**
     * @return the coursePersister
     */
    public CourseMockPersister getCoursePersister() {
        return coursePersister;
    }

    /**
     * @return the courseCoursePersister
     */
    public CourseCourseMockPersister getCourseCoursePersister() {
        return courseCoursePersister;
    }

    /**
     * @return the courseMembershipPersister
     */
    public CourseMembershipMockPersister getCourseMembershipPersister() {
        return courseMembershipPersister;
    }

    /**
     * @return the groupPersister
     */
    public GroupMockPersister getGroupPersister() {
        return groupPersister;
    }

    /**
     * @return the groupMembershipPersister
     */
    public GroupMembershipMockPersister getGroupMembershipPersister() {
        return groupMembershipPersister;
    }

    /**
     * @return the userPersister
     */
    public UserMockPersister getUserPersister() {
        return userPersister;
    }
}
