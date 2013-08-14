package uk.ac.ed.learn9.bb.timetabling.service;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import blackboard.data.course.Group;
import blackboard.data.course.GroupMembership;
import uk.ac.ed.learn9.bb.timetabling.blackboard.MockGroup;
import uk.ac.ed.learn9.bb.timetabling.util.DbScriptUtil;

/**
 *
 * @author jnicoll2
 */
@ContextConfiguration(locations={"classpath:applicationContext-test.xml"})
public class BlackboardServiceTest extends AbstractJUnit4SpringContextTests {
    private BlackboardMockService service = null;
    
    public BlackboardServiceTest() {
    }
    
    /**
     * Gets an instance of {@link BlackboardService}.
     * 
     * @return an instance of {@link BlackboardService}.
     */
    public BlackboardService getService() {
        return this.service;
    }

    private DataSource getStagingDataSource() {
        return this.applicationContext.getBean("stagingDataSource", javax.sql.DataSource.class);
    }
    
    /**
     * Gets an instance of {@link SConcurrencyService}.
     * 
     * @return an instance of {@link ConcurrencyService}.
     */
    public SynchronisationRunService getSynchronisationRunService() {
        return this.applicationContext.getBean(SynchronisationRunService.class);
    }
    
    /**
     * Constructs schemas for the test reporting and staging databases, before
     * each test is run.
     * 
     * @throws IOException if there was a problem accessing the database creation
     * scripts.
     * @throws SQLException if there was a problem applying the database creation
     * scripts.
     * @see #after() 
     */
    @Before
    public void before() throws IOException, SQLException {
        final BlackboardService contextService = this.applicationContext.getBean(BlackboardService.class);
        
        this.service = new BlackboardMockService();
        this.service.setForceAllMailTo(contextService.getForceAllMailTo());
        this.service.setMailSender(contextService.getMailSender());
        this.service.setTemplateMessage(contextService.getTemplateMessage());
        this.service.setVelocityEngine(contextService.getVelocityEngine());
        
        final Connection syncConnection = this.getStagingDataSource().getConnection();
        
        try {
            final File syncDbSchema = this.applicationContext.getResource(SynchronisationServiceTest.LOCATION_STAGING_DB_SCHEMA_RESOURCE).getFile();
            DbScriptUtil.runScript(syncConnection, syncDbSchema);
        } finally {
            syncConnection.close();
        }
    }
    
    /**
     * Drops the schemas for the test reporting and staging databases, after
     * each test is run, to ensure there are no side-effects from each test.
     * 
     * @throws IOException if there was a problem accessing the database drop
     * scripts.
     * @throws SQLException if there was a problem applying the database drop
     * scripts.
     * @see #before() 
     */
    @After
    public void after() throws IOException, SQLException {
        this.service = null;
        
        final Connection syncConnection = this.getStagingDataSource().getConnection();
        
        try {
            final File syncDbSchema = this.applicationContext.getResource(SynchronisationServiceTest.LOCATION_STAGING_DB_DROP_RESOURCE).getFile();
            DbScriptUtil.runScript(syncConnection, syncDbSchema);
        } finally {
            syncConnection.close();
        }
    }

    /**
     * Test of applyNewEnrolmentChanges method, of class BlackboardService.
     */
    @Test
    public void testApplyNewEnrolmentChanges() throws Exception {
    }

    /**
     * Test of applyPreviouslyFailedEnrolmentChanges method, of class BlackboardService.
     */
    @Test
    public void testApplyPreviouslyFailedEnrolmentChanges() throws Exception {
        final Connection connection = this.getStagingDataSource().getConnection();
        try {
            this.getService().applyPreviouslyFailedEnrolmentChanges(connection);
        } finally {
            connection.close();
        }
    }
    
    /**
     * Validate code for testing if a student can safely be removed from a group
     * without risk of loss of data.
     */
    @Test
    public void testIsGroupMembershipRemovalUnsafe()
        throws Exception {
        final BlackboardService service = this.getService();
        final Group availableGroup = new Group();
        final MockGroup unavailableGroup = new MockGroup();
        final GroupMembership groupMembership = new GroupMembership();
        
        availableGroup.setIsAvailable(true);
        unavailableGroup.setIsAvailable(false);
        
        unavailableGroup.setHasGroupToolWithGradeableItem(false);
        unavailableGroup.setIsDiscussionBoardAvailable(false);
        
        assertFalse(service.isGroupMembershipRemovalUnsafe(unavailableGroup, groupMembership));
        assertTrue(service.isGroupMembershipRemovalUnsafe(availableGroup, groupMembership));
        
        unavailableGroup.setHasGroupToolWithGradeableItem(true);
        assertTrue(service.isGroupMembershipRemovalUnsafe(unavailableGroup, groupMembership));
        
        unavailableGroup.setIsDiscussionBoardAvailable(true);
        assertTrue(service.isGroupMembershipRemovalUnsafe(unavailableGroup, groupMembership));
        
        unavailableGroup.setHasGroupToolWithGradeableItem(false);
        assertTrue(service.isGroupMembershipRemovalUnsafe(unavailableGroup, groupMembership));
    }

    /**
     * Test of mapModulesToCourses method, of class BlackboardService.
     */
    @Test
    public void testMapModulesToCourses() throws Exception {
        final Connection connection = this.getStagingDataSource().getConnection();
        try {
            this.getService().mapModulesToCourses(connection);
        } finally {
            connection.close();
        }
    }

    /**
     * Test of mapStudentSetsToUsers method, of class BlackboardService.
     */
    @Test
    public void testMapStudentSetsToUsers() throws Exception {
        final Connection connection = this.getStagingDataSource().getConnection();
        try {
            this.getService().mapStudentSetsToUsers(connection);
        } finally {
            connection.close();
        }
    }
}
