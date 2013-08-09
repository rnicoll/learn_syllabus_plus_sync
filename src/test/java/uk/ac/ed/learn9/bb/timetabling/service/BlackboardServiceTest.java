package uk.ac.ed.learn9.bb.timetabling.service;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import blackboard.data.course.Course;
import blackboard.data.course.Group;
import blackboard.persist.Id;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import uk.ac.ed.learn9.bb.timetabling.RdbIdSource;
import uk.ac.ed.learn9.bb.timetabling.SequentialRdbIdSource;
import uk.ac.ed.learn9.bb.timetabling.dao.ActivityDao;
import uk.ac.ed.learn9.bb.timetabling.dao.ActivityGroupDao;
import uk.ac.ed.learn9.bb.timetabling.dao.ModuleCourseDao;
import uk.ac.ed.learn9.bb.timetabling.dao.ModuleDao;
import uk.ac.ed.learn9.bb.timetabling.data.AcademicYearCode;
import uk.ac.ed.learn9.bb.timetabling.data.Activity;
import uk.ac.ed.learn9.bb.timetabling.data.ActivityGroup;
import uk.ac.ed.learn9.bb.timetabling.data.ActivityTemplate;
import uk.ac.ed.learn9.bb.timetabling.data.ActivityType;
import uk.ac.ed.learn9.bb.timetabling.data.Module;
import uk.ac.ed.learn9.bb.timetabling.data.ModuleCourse;
import uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun;
import uk.ac.ed.learn9.bb.timetabling.util.DbScriptUtil;
import uk.ac.ed.learn9.bb.timetabling.util.RdbUtil;
import uk.ac.ed.learn9.bb.timetabling.util.StagingUtil;

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
    
    private ActivityDao getActivityDao() {
        return this.applicationContext.getBean("activityDao", ActivityDao.class);
    }
    
    private ActivityGroupDao getActivityGroupDao() {
        return this.applicationContext.getBean("activityGroupDao", ActivityGroupDao.class);
    }
    
    private ModuleDao getModuleDao() {
        return this.applicationContext.getBean("moduleDao", ModuleDao.class);
    }
    
    private ModuleCourseDao getModuleCourseDao() {
        return this.applicationContext.getBean("moduleCourseDao", ModuleCourseDao.class);
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
     * Test of generateGroupsForActivities method, of class BlackboardService.
     */
    @Test
    public void testCreateFullDiffForStudentsOnActivity() throws Exception {
        final ActivityDao activityDao = this.getActivityDao();
        final RdbIdSource rdbIdSource = new SequentialRdbIdSource();
        final SynchronisationRunService synchronisationRunService
                = this.getSynchronisationRunService();
        final SynchronisationRun run = synchronisationRunService.startNewRun();
        final Connection connection = this.getStagingDataSource().getConnection();
        
        try {
            int activityId = 1;
            final String courseCode = "ENLI11007_SV1_SEM2";
            final Id courseId = new MockId(Course.DATA_TYPE, "_1_");
            final Id groupId = new MockId(Group.DATA_TYPE, "_2_");
            final String moduleName = "Test module";
            final AcademicYearCode academicYear = new AcademicYearCode("2013/4");
            final boolean webCtActive = true;
            final Module module = StagingUtil.createTestModule(connection,
                this.getModuleDao(), courseCode, moduleName, academicYear, webCtActive,
                rdbIdSource);

            assertEquals("ENLI110072013-4SV1SEM2", module.getLearnCourseCode());

            final ActivityTemplate activityTemplateSync = null;
            final ActivityType activityType = null;
            final Activity activity
                = StagingUtil.createTestActivity(connection, activityDao, activityTemplateSync,
                    activityType, module, RdbUtil.SchedulingMethod.NOT_SCHEDULED,
                    activityId, rdbIdSource);
            final ModuleCourse moduleCourse = StagingUtil.createModuleCourse(connection,
                    this.getModuleCourseDao(), module, courseId);

            final ActivityGroupDao activityGroupDao = this.getActivityGroupDao();
            List<ActivityGroup> activityGroups;

            StagingUtil.createTestActivityGroup(connection,
                    activity, moduleCourse, groupId);
            activityGroups = activityGroupDao.getByActivity(activity);
            assertEquals(1, activityGroups.size());
        
            this.getService().createFullDiffForStudentsOnActivity(connection,
                run, activity.getActivityId());
        } finally {
            connection.close();
        }
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
