package uk.ac.ed.learn9.bb.timetabling.service;

import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseCourseDbLoader;
import blackboard.persist.course.CourseDbLoader;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.persist.course.GroupDbLoader;
import blackboard.persist.course.GroupDbPersister;
import blackboard.persist.course.GroupMembershipDbLoader;
import blackboard.persist.course.GroupMembershipDbPersister;
import blackboard.persist.user.UserDbLoader;
import uk.ac.ed.learn9.bb.timetabling.blackboard.MockPersistenceManager;


public class BlackboardMockService extends BlackboardService {
    private MockPersistenceManager persistenceManager = MockPersistenceManager.getInstance();
    
    @Override
    protected CourseMembershipDbLoader getCourseMembershipDbLoader() throws PersistenceException {
        return this.persistenceManager.getCourseMembershipLoader();
    }

    @Override
    protected CourseDbLoader getCourseDbLoader() throws PersistenceException {
        return this.persistenceManager.getCourseLoader();
    }
    
    @Override
    protected CourseCourseDbLoader getCourseCourseDbLoader() throws PersistenceException {
        return this.persistenceManager.getCourseCourseLoader();
    }
    
    @Override
    protected GroupMembershipDbLoader getGroupMembershipDbLoader() throws PersistenceException {
        return this.persistenceManager.getGroupMembershipLoader();
    }

    @Override
    protected GroupMembershipDbPersister getGroupMembershipDbPersister() throws PersistenceException {
        return this.persistenceManager.getGroupMembershipPersister();
    }

    @Override
    protected GroupDbLoader getGroupDbLoader() throws PersistenceException {
        return this.persistenceManager.getGroupLoader();
    }

    @Override
    protected GroupDbPersister getGroupDbPersister() throws PersistenceException {
        return this.persistenceManager.getGroupPersister();
    }
  
    @Override
    protected UserDbLoader getUserDbLoader() throws PersistenceException {
        return this.persistenceManager.getUserLoader();
    }
}
