package uk.ac.ed.learn9.bb.timetabling.service;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import blackboard.data.course.Course;
import blackboard.data.course.Group;
import blackboard.persist.Id;

import uk.ac.ed.learn9.bb.timetabling.RdbIdSource;
import uk.ac.ed.learn9.bb.timetabling.SequentialRdbIdSource;
import uk.ac.ed.learn9.bb.timetabling.dao.ActivityDao;
import uk.ac.ed.learn9.bb.timetabling.dao.ActivityGroupDao;
import uk.ac.ed.learn9.bb.timetabling.dao.ActivityTemplateDao;
import uk.ac.ed.learn9.bb.timetabling.dao.ActivityTypeDao;
import uk.ac.ed.learn9.bb.timetabling.dao.EnrolmentChangeDao;
import uk.ac.ed.learn9.bb.timetabling.dao.EnrolmentChangePartDao;
import uk.ac.ed.learn9.bb.timetabling.dao.ModuleCourseDao;
import uk.ac.ed.learn9.bb.timetabling.dao.ModuleDao;
import uk.ac.ed.learn9.bb.timetabling.dao.StudentSetDao;
import uk.ac.ed.learn9.bb.timetabling.data.AcademicYearCode;
import uk.ac.ed.learn9.bb.timetabling.data.Activity;
import uk.ac.ed.learn9.bb.timetabling.data.ActivityGroup;
import uk.ac.ed.learn9.bb.timetabling.data.ActivityTemplate;
import uk.ac.ed.learn9.bb.timetabling.data.ActivityType;
import uk.ac.ed.learn9.bb.timetabling.data.Configuration;
import uk.ac.ed.learn9.bb.timetabling.data.EnrolmentChange;
import uk.ac.ed.learn9.bb.timetabling.data.Module;
import uk.ac.ed.learn9.bb.timetabling.data.ModuleCourse;
import uk.ac.ed.learn9.bb.timetabling.data.StudentSet;
import uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun;
import uk.ac.ed.learn9.bb.timetabling.util.DbScriptUtil;
import uk.ac.ed.learn9.bb.timetabling.util.RdbUtil;
import uk.ac.ed.learn9.bb.timetabling.util.StagingUtil;

@ContextConfiguration(locations={"classpath:applicationContext-test.xml"})
public class SynchronisationServiceTest extends AbstractJUnit4SpringContextTests {
    /**
     * Location of the script used to generate the test reporting database schema,
     * expressed in the Spring resource location format.
     */
    public static final String LOCATION_RDB_DB_SCHEMA_RESOURCE = "classpath:rdb_db_schema.sql";
    /**
     * Location of the script used to destroy the test reporting database schema,
     * expressed in the Spring resource location format.
     */
    public static final String LOCATION_RDB_DB_DROP_RESOURCE = "classpath:rdb_db_drop.sql";
    /**
     * Location of the script used to generate the test staging database schema,
     * expressed in the Spring resource location format.
     */
    public static final String LOCATION_STAGING_DB_SCHEMA_RESOURCE = "classpath:sync_db_schema.sql";
    /**
     * Location of the script used to destroy the test staging database schema,
     * expressed in the Spring resource location format.
     */
    public static final String LOCATION_STAGING_DB_DROP_RESOURCE = "classpath:sync_db_drop.sql";
    
    private BlackboardMockService blackboardService = null;
    
    /**
     * Default constructor.
     */
    public SynchronisationServiceTest() {
    }
    
    private ActivityDao getActivityDao() {
        return this.applicationContext.getBean("activityDao", ActivityDao.class);
    }
    
    private ActivityGroupDao getActivityGroupDao() {
        return this.applicationContext.getBean("activityGroupDao", ActivityGroupDao.class);
    }
    
    private ActivityTemplateDao getActivityTemplateDao() {
        return this.applicationContext.getBean("activityTemplateDao", ActivityTemplateDao.class);
    }
    
    private ActivityTypeDao getActivityTypeDao() {
        return this.applicationContext.getBean("activityTypeDao", ActivityTypeDao.class);
    }
    
    /**
     * Gets an instance of {@link BlackboardService}.
     * 
     * @return an instance of {@link BlackboardService}.
     */
    public BlackboardService getBlackboardService() {
        return this.blackboardService;
    }
    
    private EnrolmentChangeDao getEnrolmentChangeDao() {
        return this.applicationContext.getBean("enrolmentChangeDao", EnrolmentChangeDao.class);
    }
    
    private EnrolmentChangePartDao getEnrolmentChangePartDao() {
        return this.applicationContext.getBean("enrolmentChangePartDao", EnrolmentChangePartDao.class);
    }
    
    private ModuleDao getModuleDao() {
        return this.applicationContext.getBean("moduleDao", ModuleDao.class);
    }
    
    private ModuleCourseDao getModuleCourseDao() {
        return this.applicationContext.getBean("moduleCourseDao", ModuleCourseDao.class);
    }
    
    /**
     * Gets an instance of {@link SynchronisationService}.
     * 
     * @return an instance of {@link SynchronisationService}.
     */
    public SynchronisationService getService() {
        return this.applicationContext.getBean(SynchronisationService.class);
    }
    
    private StudentSetDao getStudentSetDao() {
        return this.applicationContext.getBean("studentSetDao", StudentSetDao.class);
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
        final Connection syncConnection = this.getService().getStagingDataSource().getConnection();
        
        try {
            final File syncDbSchema = this.applicationContext.getResource(LOCATION_STAGING_DB_SCHEMA_RESOURCE).getFile();
            DbScriptUtil.runScript(syncConnection, syncDbSchema);
        } finally {
            syncConnection.close();
        }
        
        final Connection rdbConnection = this.getService().getRdbDataSource().getConnection();
        
        try {
            final File rdbDbSchema = this.applicationContext.getResource(LOCATION_RDB_DB_SCHEMA_RESOURCE).getFile();
            DbScriptUtil.runScript(rdbConnection, rdbDbSchema);
        } finally {
            rdbConnection.close();
        }
        
        // Set up the mock Blackboard service
        final BlackboardService contextBlackboardService = this.applicationContext.getBean(BlackboardService.class);
        
        this.blackboardService = new BlackboardMockService();
        this.blackboardService.setForceAllMailTo(contextBlackboardService.getForceAllMailTo());
        this.blackboardService.setMailSender(contextBlackboardService.getMailSender());
        this.blackboardService.setTemplateMessage(contextBlackboardService.getTemplateMessage());
        this.blackboardService.setVelocityEngine(contextBlackboardService.getVelocityEngine());
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
        final Connection syncConnection = this.getService().getStagingDataSource().getConnection();
        
        try {
            final File syncDbSchema = this.applicationContext.getResource(LOCATION_STAGING_DB_DROP_RESOURCE).getFile();
            DbScriptUtil.runScript(syncConnection, syncDbSchema);
        } finally {
            syncConnection.close();
        }
        
        final Connection rdbConnection = this.getService().getRdbDataSource().getConnection();
        
        try {
            final File rdbDbSchema = this.applicationContext.getResource(LOCATION_RDB_DB_DROP_RESOURCE).getFile();
            DbScriptUtil.runScript(rdbConnection, rdbDbSchema);
        } finally {
            rdbConnection.close();
        }
    }
    
    /**
     * Tests that assertBelowRemoveThreshold() ignores the remove change count
     * where no value is set in the database.
     */
    @Test
    public void testAssertBelowRemoveThresholdNoConfiguration()
        throws Exception {
        System.out.println("assertBelowRemoveThresholdNoConfiguration");
        
        final SynchronisationService service = this.getService();
        final Configuration configuration = new Configuration();
        
        configuration.setRemoveThresholdCount(null);
        
        service.assertBelowRemoveThreshold(configuration, Integer.MAX_VALUE);
    }
    
    /**
     * Tests that assertBelowRemoveThreshold() does not error when the threshold
     * has not been exceeded.
     */
    @Test
    public void testAssertBelowRemoveThresholdOkay()
        throws Exception {
        final SynchronisationService service = this.getService();
        final Configuration configuration = new Configuration();
        
        configuration.setRemoveThresholdCount(2);
        
        service.assertBelowRemoveThreshold(configuration, 1);
        service.assertBelowRemoveThreshold(configuration, 2);
    }
    
    /**
     * Tests that assertBelowRemoveThreshold() does error where the threshold
     * has been exceeded.
     */
    @Test(expected=ThresholdException.class)
    public void testAssertBelowRemoveThreshold()
        throws Exception {
        final SynchronisationService service = this.getService();
        final Configuration configuration = new Configuration();
        
        configuration.setRemoveThresholdCount(1);
        
        service.assertBelowRemoveThreshold(configuration, 2);
    }

    /**
     * Test of buildGroupsDescription method, of class SynchronisationService.
     */
    @Test
    public void testBuildGroupsDescription() throws Exception {
        System.out.println("buildGroupsDescription");
        
        SynchronisationService synchronisationService = this.getService();
        
        assertEquals("Tutorial 1 of 3",
            synchronisationService.buildGroupDescription("Course Name/1", "Tutorial", 3));
        assertEquals("Tutorial 1",
            synchronisationService.buildGroupDescription("Course Name/1", "Tutorial", null));
        
    }
    
    /**
     * Test of synchroniseTimetablingData method, of class SynchronisationService.
     */
    @Test
    public void testTimetablingSynchroniseData() throws Exception {
        System.out.println("synchroniseTimetablingData");
        final SynchronisationService synchronisationService = this.getService();
        
        AcademicYearCode academicYearCode = new AcademicYearCode("2012/3");
        final Connection rdbConnection = synchronisationService.getRdbDataSource().getConnection();
        try {
            // Test simply creating a module
            final RdbIdSource rdbIdSource = new SequentialRdbIdSource();
            final Module expResultModule = RdbUtil.createTestModule(rdbConnection, academicYearCode, rdbIdSource);
        
            synchronisationService.synchroniseTimetablingData();

            final ModuleDao moduleDao = getModuleDao();
            List<Module> modules = moduleDao.getAll();

            assertEquals(1, modules.size());

            Module resultModule = modules.get(0);
            assertEquals(expResultModule.getModuleId(), resultModule.getModuleId());
            assertEquals(expResultModule.getTimetablingCourseCode(), resultModule.getTimetablingCourseCode());
            assertEquals(academicYearCode.toString(), resultModule.getTimetablingAcademicYear());
        
            // Change the academic year code, re-sync and check it's updated
            // correctly.
            academicYearCode = new AcademicYearCode("2013/4");
            RdbUtil.updateModuleAyr(rdbConnection, academicYearCode, expResultModule);
            
            synchronisationService.synchroniseTimetablingData();
            
            modules = moduleDao.getAll();
            
            assertEquals(1, modules.size());

            resultModule = modules.get(0);
            assertEquals(expResultModule.getModuleId(), resultModule.getModuleId());
            assertEquals(expResultModule.getTimetablingCourseCode(), resultModule.getTimetablingCourseCode());
            assertEquals(academicYearCode.toString(), resultModule.getTimetablingAcademicYear());
            
            // Test building a full activity
            
            final ActivityType tutorialType = RdbUtil.createTestActivityType(rdbConnection, "Tutorial", rdbIdSource);
            final ActivityTemplate activityTemplateSync = RdbUtil.createTestActivityTemplate(rdbConnection, expResultModule,
                    tutorialType, "Tutorials", RdbUtil.TemplateForVle.FOR_VLE, rdbIdSource);
            int activityId = 1;
            
            final Activity expResultActivity
                = RdbUtil.createTestActivity(rdbConnection, activityTemplateSync,
                    expResultModule, RdbUtil.SchedulingMethod.SCHEDULED, activityId++, rdbIdSource);
            
            synchronisationService.synchroniseTimetablingData();
            
            final ActivityDao activityDao = this.getActivityDao();
            final List<Activity> activities = activityDao.getAll();
            
            assertEquals(1, activities.size());
            
            final Activity resultActivity = activities.get(0);
            
            assertEquals(expResultActivity.getActivityId(), resultActivity.getActivityId());
        } finally {
            rdbConnection.close();
        }
    }
    
    /**
     * Tests that activity templates that are not intended to be synchronised,
     * are corrected excluded.
     */
    @Test
    public void testActivityTemplateFiltering() throws Exception {
        System.out.println("activityTemplateFiltering");
        final SynchronisationService synchronisationService = this.getService();
        final AcademicYearCode academicYearCode = new AcademicYearCode("2012/3");            
        final Connection rdbConnection = synchronisationService.getRdbDataSource().getConnection();
        
        try {
            final RdbIdSource rdbIdSource = new SequentialRdbIdSource();
            final ActivityType lectureType = RdbUtil.createTestActivityType(rdbConnection, "Lecture", rdbIdSource);
            final ActivityType tutorialType = RdbUtil.createTestActivityType(rdbConnection, "Tutorial", rdbIdSource);
            final Module module = RdbUtil.createTestModule(rdbConnection, academicYearCode, rdbIdSource);
            
            // We generate three activities:
            // 1. To sync
            // 2. Marked to sync, but with a whole-class activity only (so not synced)
            // 3. Marked not to be synced
            final ActivityTemplate activityTemplateSync = RdbUtil.createTestActivityTemplate(rdbConnection, module,
                    tutorialType, "Tutorials", RdbUtil.TemplateForVle.FOR_VLE, rdbIdSource);
            final ActivityTemplate activityTemplateWholeClass = RdbUtil.createTestActivityTemplate(rdbConnection, module,
                    lectureType, "Labs", RdbUtil.TemplateForVle.FOR_VLE, rdbIdSource);
            final ActivityTemplate activityTemplateNoSync = RdbUtil.createTestActivityTemplate(rdbConnection, module,
                    lectureType, "Lectures", RdbUtil.TemplateForVle.NOT_FOR_VLE, rdbIdSource);
            
            int activityId = 1;
            
            RdbUtil.createTestActivity(rdbConnection, activityTemplateSync,
                    module, RdbUtil.SchedulingMethod.SCHEDULED, activityId++, rdbIdSource);
            RdbUtil.createTestActivity(rdbConnection, activityTemplateSync,
                    module, RdbUtil.SchedulingMethod.SCHEDULED, activityId++, rdbIdSource);
            RdbUtil.createTestActivity(rdbConnection, activityTemplateWholeClass,
                    module, RdbUtil.SchedulingMethod.SCHEDULED, activityId++, rdbIdSource);
            RdbUtil.createTestActivity(rdbConnection, activityTemplateNoSync,
                    module, RdbUtil.SchedulingMethod.SCHEDULED, activityId++, rdbIdSource);
            RdbUtil.createTestActivity(rdbConnection, activityTemplateNoSync,
                    module, RdbUtil.SchedulingMethod.SCHEDULED, activityId++, rdbIdSource);

            synchronisationService.synchroniseTimetablingData();
            
            // Check the set of activity templates in the database view against
            // the ones we expect to see
            final Set<String> expResultIds = Collections.singleton(activityTemplateSync.getTemplateId());
            final Set<String> resultIds = new HashSet<String>();
            
            final Connection cacheConnection = synchronisationService.getStagingDataSource().getConnection();
            try {
                final PreparedStatement selectStatement = cacheConnection.prepareStatement("SELECT tt_template_id "
                    + "FROM sync_template_vw");
                try {
                    final ResultSet rs = selectStatement.executeQuery();
                    try {
                        while (rs.next()) {
                            resultIds.add(rs.getString("tt_template_id"));
                        }
                    } finally {
                        rs.close();
                    }
                } finally {
                    selectStatement.close();
                }
            } finally {
                cacheConnection.close();
            }
            
            assertEquals(expResultIds, resultIds);
        } finally {
            rdbConnection.close();
        }
    }

    /**
     * Test of createFullDiffForStudentsOnActivity method, of class
     * SynchronisationService. This method is used to re-generate all student sets on
     * an activity, for example where a group is being rebuilt from scratch.
     */
    @Test
    public void testCreateFullDiffForStudentsOnActivity() throws Exception {
        final ActivityDao activityDao = this.getActivityDao();
        final RdbIdSource rdbIdSource = new SequentialRdbIdSource();
        final SynchronisationRunService synchronisationRunService
                = this.getSynchronisationRunService();
        final SynchronisationRun run = synchronisationRunService.startNewRun();
        final Connection connection = this.getService().getStagingDataSource().getConnection();
        
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
                    this.getModuleCourseDao(), module, courseId, null);

            final ActivityGroupDao activityGroupDao = this.getActivityGroupDao();
            List<ActivityGroup> activityGroups;

            StagingUtil.createTestActivityGroup(connection,
                    activity, moduleCourse, groupId);
            activityGroups = activityGroupDao.getByActivity(activity);
            assertEquals(1, activityGroups.size());
        
            this.getService().createFullDiffForStudentsOnActivity(connection,
                run, activity.getActivityId());
            
            // TODO: Load the generated differences
        } finally {
            connection.close();
        }
    }
    
    /**
     * Tests that activity course codes are generated correctly.
     */
    @Test
    public void testModuleCourseCodeGeneration() throws Exception {
        System.out.println("moduleCourseCodeGeneration");
        final SynchronisationService synchronisationService = this.getService();
        final AcademicYearCode academicYearCode = new AcademicYearCode("2012/3");
        final Connection rdbConnection = synchronisationService.getRdbDataSource().getConnection();
        try {
            final RdbIdSource rdbIdSource = new SequentialRdbIdSource();
            final Module expResultModule = RdbUtil.createTestModule(rdbConnection, academicYearCode, rdbIdSource);
        
            synchronisationService.synchroniseTimetablingData();

            final ModuleDao moduleDao = getModuleDao();
            List<Module> modules = moduleDao.getAll();

            assertEquals(1, modules.size());

            Module resultModule = modules.get(0);
            assertEquals(expResultModule.getModuleId(), resultModule.getModuleId());
            assertEquals(expResultModule.getTimetablingCourseCode(), resultModule.getTimetablingCourseCode());
            assertEquals(expResultModule.getLearnCourseCode(), resultModule.getLearnCourseCode());
            assertEquals(academicYearCode.toString(), resultModule.getTimetablingAcademicYear());
        } finally {
            rdbConnection.close();
        }
    }
    
    /**
     * Very simple test for difference generation code; at this point this
     * just tests the SQL is valid, without verifying the results. This test
     * is a simplified version of {@link #testGenerateDiff()}
     */
    @Test
    public void testDoGenerateDiff() throws Exception {
        System.out.println("doGenerateDiff");
        final SynchronisationService synchronisationService = this.getService();
        final SynchronisationRunService synchronisationRunService = this.getSynchronisationRunService();
        final Connection stagingConnection = synchronisationService.getStagingDataSource().getConnection();
        try {
            final SynchronisationRun result = synchronisationRunService.startNewRun();
            synchronisationService.doGenerateDiff(result, stagingConnection);
        } finally {
            stagingConnection.close();
        }
    }
    
    /**
     * Very simple test for enrolment import and difference generation code.
     */
    @Test
    public void testGenerateDiff() throws Exception {
        System.out.println("generateDiff");
        final Activity activityA;
        final Activity activityB;
        final ActivityDao activityDao = this.getActivityDao();
        final RdbIdSource rdbIdSource = new SequentialRdbIdSource();
        final Module module;
        final StudentSet studentSet;
        final SynchronisationRunService synchronisationRunService
                = this.getSynchronisationRunService();
        final SynchronisationRun runA = synchronisationRunService.startNewRun();
        Connection connection = this.getService().getStagingDataSource().getConnection();
        
        try {
            int activityId = 1;
            final String courseCode = "ENLI11007_SV1_SEM2";
            final Id courseId = new MockId(Course.DATA_TYPE, "_1_");
            final Id groupId = new MockId(Group.DATA_TYPE, "_2_");
            final boolean learnCourseAvailable = true;
            final String moduleName = "Test module";
            final AcademicYearCode academicYear = new AcademicYearCode("2013/4");
            final boolean webCtActive = true;
            
            module = StagingUtil.createTestModule(connection,
                this.getModuleDao(), courseCode, moduleName, academicYear, webCtActive,
                rdbIdSource);

            assertEquals("ENLI110072013-4SV1SEM2", module.getLearnCourseCode());

            final ActivityTemplate activityTemplate
                    = StagingUtil.createActivityTemplate(connection, this.getActivityTemplateDao(),
                    "Activity template", (String)null, rdbIdSource);
            final ActivityType activityType 
                    = StagingUtil.createActivityType(connection, this.getActivityTypeDao(),
                    "Activity type", rdbIdSource);
            
            activityA
                = StagingUtil.createTestActivity(connection, activityDao, activityTemplate,
                    activityType, module, RdbUtil.SchedulingMethod.SCHEDULED,
                    activityId, rdbIdSource);
            activityB
                = StagingUtil.createTestActivity(connection, activityDao, activityTemplate,
                    activityType, module, RdbUtil.SchedulingMethod.SCHEDULED,
                    activityId, rdbIdSource);
            
            final ModuleCourse moduleCourse = StagingUtil.createModuleCourse(connection,
                    this.getModuleCourseDao(), module, courseId, learnCourseAvailable);
            
            assertEquals(moduleCourse.getModule(), module);

            StagingUtil.createTestActivityGroup(connection,
                    activityA, moduleCourse, groupId);
            
            studentSet = StagingUtil.createTestStudentSet(connection,
                    this.getStudentSetDao(), rdbIdSource, "s1234567890");
            
            StagingUtil.createStudentSetActivity(connection, runA, studentSet,
                    activityA);
        } finally {
            connection.close();
        }
            
        this.getService().generateDiff(runA);
        
        assertEquals(1, this.getEnrolmentChangeDao().getAll().size());
        assertEquals(1, this.getEnrolmentChangePartDao().getAll().size());
        
        synchronisationRunService.handleSuccessOutcome(runA);
        
        // Try generating a remove change
        
        final SynchronisationRun runB = synchronisationRunService.startNewRun();
            
        this.getService().generateDiff(runB);
        
        assertEquals(2, this.getEnrolmentChangeDao().getAll().size());        
        assertEquals(2, this.getEnrolmentChangePartDao().getAll().size());
        
        for (EnrolmentChange change: this.getEnrolmentChangeDao().getAll()) {
            assertEquals(activityA, change.getActivity());
            assertEquals(studentSet, change.getStudentSet());
            
            if (change.getRun().getRunId() == runA.getRunId()) {
                assertEquals(change.getChangeType(), EnrolmentChange.Type.ADD);
            } else {
                assertEquals(change.getChangeType(), EnrolmentChange.Type.REMOVE);
            }
        }
        
        synchronisationRunService.handleSuccessOutcome(runB);
        
        // Generate no change
        
        final SynchronisationRun runC = synchronisationRunService.startNewRun();
            
        this.getService().generateDiff(runC);
        
        assertEquals(2, this.getEnrolmentChangeDao().getAll().size());        
        assertEquals(2, this.getEnrolmentChangePartDao().getAll().size());
    }

    /**
     * Test SQL for generating activity-group relationships in the database.
     */
    @Test
    public void testForgetCourse() throws Exception {
        System.out.println("forgetCourse");
        final SynchronisationService synchronisationService = this.getService();
        final Id brokenId = new MockId(Course.DATA_TYPE, "000");
        
        synchronisationService.forgetCourse(synchronisationService.getStagingDataSource().getConnection(), brokenId);
    }

    /**
     * Test SQL for generating activity-group relationships in the database.
     */
    @Test
    public void testGenerateActivityGroups() throws Exception {
        System.out.println("generateActivityGroups");
        final SynchronisationService synchronisationService = this.getService();
        
        // XXX: Need to have this actually generate data, rather than just be
        // an SQL validity check
        
        synchronisationService.generateActivityGroups();
    }

    /**
     * Test of generateGroupNames method, of class SynchronisationService.
     */
    @Test
    public void testGenerateGroupNames() throws Exception {
        System.out.println("generateGroupNames");
        final AcademicYearCode academicYearCode = new AcademicYearCode("2012/3");
        final Map<String, String> expectedActivityGroupNames = new HashMap<String, String>();
        final SynchronisationService synchronisationService = this.getService();
        final Connection rdbConnection = synchronisationService.getRdbDataSource().getConnection();
        try {
            final RdbIdSource rdbIdSource = new SequentialRdbIdSource();
            final ActivityType tutorialType = RdbUtil.createTestActivityType(rdbConnection, "Tutorial", rdbIdSource);
            final Module module = RdbUtil.createTestModule(rdbConnection, academicYearCode, rdbIdSource);
            
            // Generate a pair of activities that we can build names for
            final ActivityTemplate activityTemplate = RdbUtil.createTestActivityTemplate(rdbConnection, module,
                    tutorialType, "Tutorial", RdbUtil.TemplateForVle.FOR_VLE, rdbIdSource);
            
            int activityId = 1;
            
            expectedActivityGroupNames.put(RdbUtil.createTestActivity(rdbConnection, activityTemplate,
                    module, RdbUtil.SchedulingMethod.SCHEDULED, activityId++, rdbIdSource).getActivityId(),
                    "TT_Tutorial/1");
            expectedActivityGroupNames.put(RdbUtil.createTestActivity(rdbConnection, "Tutorial/" + activityId++, 
                    module, RdbUtil.SchedulingMethod.SCHEDULED, tutorialType, rdbIdSource).getActivityId(),
                    "TT_Tutorial/2");
            
            final Connection cacheConnection = synchronisationService.getStagingDataSource().getConnection();
            try {
                synchronisationService.synchroniseTimetablingData();
                synchronisationService.generateGroupNames(cacheConnection);
            } finally {
                cacheConnection.close();
            }
            
            // Validate the group names that have been generated
            final List<Activity> activities = this.getActivityDao().getAll();
            
            for (Activity activity: activities) {
                final String expectedName = expectedActivityGroupNames.get(activity.getActivityId());
                assertNotNull(expectedName);
                assertEquals(expectedName, activity.getLearnGroupName());
            }
        } finally {
            rdbConnection.close();
        }
    }

    /**
     * Test of generateModuleCourses method, of class SynchronisationService.
     */
    @Test
    public void testGenerateModuleCourses() throws Exception {
        System.out.println("generateModuleCourses");
        
        SynchronisationService synchronisationService = this.getService();
        
        // XXX: Generate module and merged module data to handle and test
        
        synchronisationService.generateModuleCourses();
    }
    
    /**
     * Very simple test for group description update code; basically just
     * tests the SQL is sane.
     */
    @Test
    public void testPurgeActivity() throws Exception {
        final ActivityDao activityDao = this.getActivityDao();
        final RdbIdSource rdbIdSource = new SequentialRdbIdSource();
        final SynchronisationRunService synchronisationRunService
                = this.getSynchronisationRunService();
        final SynchronisationService synchronisationService = this.getService();
        final Connection stagingDatabase = this.getService().getStagingDataSource().getConnection();
        
        System.out.println("purgeActivity");
        synchronisationService.setBlackboardService(this.getBlackboardService());
        
        try {
            int activityId = 1;
            final String courseCode = "ENLI11007_SV1_SEM2";
            final Id courseId = new MockId(Course.DATA_TYPE, "_1_");
            final Id groupId = new MockId(Group.DATA_TYPE, "_2_");
            final String moduleName = "Test module";
            final AcademicYearCode academicYear = new AcademicYearCode("2013/4");
            final boolean webCtActive = true;
            final Module module = StagingUtil.createTestModule(stagingDatabase,
                this.getModuleDao(), courseCode, moduleName, academicYear, webCtActive,
                rdbIdSource);

            assertEquals("ENLI110072013-4SV1SEM2", module.getLearnCourseCode());

            final ActivityTemplate activityTemplate
                    = StagingUtil.createActivityTemplate(stagingDatabase, this.getActivityTemplateDao(),
                    "Activity template", (String)null, rdbIdSource);
            final ActivityType activityType
                    = StagingUtil.createActivityType(stagingDatabase, this.getActivityTypeDao(),
                    "Activity type", rdbIdSource);
            Activity activityA
                = StagingUtil.createTestActivity(stagingDatabase, activityDao, activityTemplate,
                    activityType, module, RdbUtil.SchedulingMethod.SCHEDULED,
                    activityId++, rdbIdSource);
            Activity activityB
                = StagingUtil.createTestActivity(stagingDatabase, activityDao, activityTemplate,
                    activityType, module, RdbUtil.SchedulingMethod.SCHEDULED,
                    activityId++, rdbIdSource);
            final ModuleCourse moduleCourse = StagingUtil.createModuleCourse(stagingDatabase,
                    this.getModuleCourseDao(), module, courseId, null);

            StagingUtil.createTestActivityGroup(stagingDatabase,
                    activityA, moduleCourse, groupId);
            StagingUtil.createTestActivityGroup(stagingDatabase,
                    activityB, moduleCourse, groupId);
            
            // Check we have two activities
            assertEquals(2, this.getActivityDao().getAll().size());
            
            // Delete one activity
            synchronisationService.purgeActivity(stagingDatabase, activityA.getActivityId());
            assertEquals(1, this.getActivityDao().getAll().size());
            
            // Delete the same activity (no-op)
            synchronisationService.purgeActivity(stagingDatabase, activityA.getActivityId());
            assertEquals(1, this.getActivityDao().getAll().size());
            
            // Delete the last activity
            synchronisationService.purgeActivity(stagingDatabase, activityB.getActivityId());
            assertEquals(0, this.getActivityDao().getAll().size());
        } finally {
            stagingDatabase.close();
        }
    }
    
    /**
     * Very simple test for group description update code; basically just
     * tests the SQL is sane.
     */
    @Test
    public void testUpdateGroupDescriptions() throws Exception {
        final ActivityDao activityDao = this.getActivityDao();
        final RdbIdSource rdbIdSource = new SequentialRdbIdSource();
        final SynchronisationRunService synchronisationRunService
                = this.getSynchronisationRunService();
        final SynchronisationService synchronisationService = this.getService();
        final Connection stagingDatabase = this.getService().getStagingDataSource().getConnection();
        
        System.out.println("updateGroupDescriptions");
        synchronisationService.setBlackboardService(this.getBlackboardService());
        
        try {
            int activityId = 1;
            final String courseCode = "ENLI11007_SV1_SEM2";
            final Id courseId = new MockId(Course.DATA_TYPE, "_1_");
            final Id groupId = new MockId(Group.DATA_TYPE, "_2_");
            final String moduleName = "Test module";
            final AcademicYearCode academicYear = new AcademicYearCode("2013/4");
            final boolean webCtActive = true;
            final Module module = StagingUtil.createTestModule(stagingDatabase,
                this.getModuleDao(), courseCode, moduleName, academicYear, webCtActive,
                rdbIdSource);

            assertEquals("ENLI110072013-4SV1SEM2", module.getLearnCourseCode());

            final ActivityTemplate activityTemplate
                    = StagingUtil.createActivityTemplate(stagingDatabase, this.getActivityTemplateDao(),
                    "Activity template", (String)null, rdbIdSource);
            final ActivityType activityType
                    = StagingUtil.createActivityType(stagingDatabase, this.getActivityTypeDao(),
                    "Activity type", rdbIdSource);
            Activity activityA
                = StagingUtil.createTestActivity(stagingDatabase, activityDao, activityTemplate,
                    activityType, module, RdbUtil.SchedulingMethod.SCHEDULED,
                    activityId++, rdbIdSource);
            Activity activityB
                = StagingUtil.createTestActivity(stagingDatabase, activityDao, activityTemplate,
                    activityType, module, RdbUtil.SchedulingMethod.SCHEDULED,
                    activityId++, rdbIdSource);
            final ModuleCourse moduleCourse = StagingUtil.createModuleCourse(stagingDatabase,
                    this.getModuleCourseDao(), module, courseId, null);

            StagingUtil.createTestActivityGroup(stagingDatabase,
                    activityA, moduleCourse, groupId);
            StagingUtil.createTestActivityGroup(stagingDatabase,
                    activityB, moduleCourse, groupId);
            
            assertNull(activityDao.getById(activityA.getActivityId()).getDescription());
            assertNull(activityDao.getById(activityB.getActivityId()).getDescription());
            
            synchronisationService.updateGroupDescriptions(stagingDatabase);
            
            assertNotNull(activityDao.getById(activityA.getActivityId()).getDescription());
            assertNotNull(activityDao.getById(activityB.getActivityId()).getDescription());
        } finally {
            stagingDatabase.close();
        }
    }
}
