package uk.ac.ed.learn9.bb.timetabling.blackboard;

/**
 * This object plays loosely the same role as the BbPersistenceManager,
 * in that it tracks the various persisters and loaders.
 */
public class MockPersistenceManager {
    private CourseMockLoader courseLoader;
    private GroupMockLoader groupLoader;
    private UserMockLoader userLoader;
    
    private             MockPersistenceManager() {
        
    }
    
    public static MockPersistenceManager getInstance() {
        final MockPersistenceManager manager = new MockPersistenceManager();
        
        manager.initialiseLoaders();
        
        return manager;
    }

    private void initialiseLoaders() {
        this.userLoader = new UserMockLoader(this);
        this.courseLoader = new CourseMockLoader(this);
    }

    /**
     * @return the courseLoader
     */
    public CourseMockLoader getCourseLoader() {
        return courseLoader;
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
