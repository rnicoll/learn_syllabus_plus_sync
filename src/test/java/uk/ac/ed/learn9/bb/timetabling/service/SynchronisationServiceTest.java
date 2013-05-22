package uk.ac.ed.learn9.bb.timetabling.service;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.springframework.beans.BeansException;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import uk.ac.ed.learn9.bb.timetabling.RdbIdSource;
import uk.ac.ed.learn9.bb.timetabling.SequentialRdbIdSource;
import uk.ac.ed.learn9.bb.timetabling.dao.ActivityDao;
import uk.ac.ed.learn9.bb.timetabling.dao.ModuleDao;
import uk.ac.ed.learn9.bb.timetabling.data.AcademicYearCode;
import uk.ac.ed.learn9.bb.timetabling.data.cache.Activity;
import uk.ac.ed.learn9.bb.timetabling.data.cache.ActivityTemplate;
import uk.ac.ed.learn9.bb.timetabling.data.cache.ActivityType;
import uk.ac.ed.learn9.bb.timetabling.data.cache.Module;
import uk.ac.ed.learn9.bb.timetabling.util.DbScriptUtil;
import uk.ac.ed.learn9.bb.timetabling.util.RdbUtil;

@ContextConfiguration(locations={"classpath:applicationContext-test.xml"})
public class SynchronisationServiceTest extends AbstractJUnit4SpringContextTests {
    public static final String LOCATION_RDB_DB_SCHEMA_RESOURCE = "classpath:rdb_db_schema.sql";
    public static final String LOCATION_RDB_DB_DROP_RESOURCE = "classpath:rdb_db_drop.sql";
    public static final String LOCATION_SYNC_DB_SCHEMA_RESOURCE = "classpath:sync_db_schema.sql";
    public static final String LOCATION_SYNC_DB_DROP_RESOURCE = "classpath:sync_db_drop.sql";
    
    public SynchronisationServiceTest() {
    }
    
    public SynchronisationService getService() {
        return this.applicationContext.getBean(SynchronisationService.class);
    }
    
    @Before
    public void before() throws IOException, SQLException {
        final Connection syncConnection = this.getService().getCacheDataSource().getConnection();
        
        try {
            final File syncDbSchema = this.applicationContext.getResource(LOCATION_SYNC_DB_SCHEMA_RESOURCE).getFile();
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
    }
    
    @After
    public void after() throws IOException, SQLException {
        final Connection syncConnection = this.getService().getCacheDataSource().getConnection();
        
        try {
            final File syncDbSchema = this.applicationContext.getResource(LOCATION_SYNC_DB_DROP_RESOURCE).getFile();
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
     * Test of applyEnrolmentChanges method, of class SynchronisationService.
     */
    /*
    @Test
    public void testApplyEnrolmentChanges() throws Exception {
        System.out.println("applyEnrolmentChanges");
        SynchronisationRun run = null;
        SynchronisationService instance = this.getService();
        instance.applyEnrolmentChanges(run);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
     */

    /**
     * Test of buildGroupsDescription method, of class SynchronisationService.
     */
    @Test
    public void testBuildGroupsDescription() throws Exception {
        System.out.println("buildGroupsDescription");
        
        SynchronisationService instance = this.getService();
        
        assertEquals("Tutorial 1 of 3",
            instance.buildGroupDescription("Course Name/1", "Tutorial", 3));
        assertEquals("Tutorial 1",
            instance.buildGroupDescription("Course Name/1", "Tutorial", null));
        
    }

    /**
     * Test of createGroupsForActivities method, of class SynchronisationService.
     */
    /*
    @Test
    public void testCreateGroupsForActivities() throws Exception {
        System.out.println("createGroupsForActivities");
        SynchronisationRun run = null;
        SynchronisationService instance = this.getService();
        instance.createGroupsForActivities(run);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
     */

    /**
     * Test of mapModulesToCourses method, of class SynchronisationService.
     */
    /*
    @Test
    public void testMapModulesToCourses() throws Exception {
        System.out.println("mapModulesToCourses");
        SynchronisationService instance = this.getService();
        instance.mapModulesToCourses();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
     */
    
    /**
     * Test of synchroniseTimetablingData method, of class SynchronisationService.
     */
    @Test
    public void testTimetablingSynchroniseData() throws Exception {
        System.out.println("synchroniseTimetablingData");
        final SynchronisationService instance = this.getService();
        
        AcademicYearCode academicYearCode = new AcademicYearCode("2012/3");
        final Connection rdbConnection = instance.getRdbDataSource().getConnection();
        try {
            // Test simply creating a module
            final RdbIdSource rdbIdSource = new SequentialRdbIdSource();
            final Module expResultModule = RdbUtil.createTestModule(rdbConnection, academicYearCode, rdbIdSource);
        
            instance.synchroniseTimetablingData();

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
            
            instance.synchroniseTimetablingData();
            
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
            
            instance.synchroniseTimetablingData();
            
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
        System.out.println("activityCourseCodeGeneration");
        final SynchronisationService instance = this.getService();
        final AcademicYearCode academicYearCode = new AcademicYearCode("2012/3");            
        final Connection rdbConnection = instance.getRdbDataSource().getConnection();
        
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

            instance.synchroniseTimetablingData();
            
            // Check the set of activity templates in the database view against
            // the ones we expect to see
            final Set<String> expResultIds = Collections.singleton(activityTemplateSync.getTemplateId());
            final Set<String> resultIds = new HashSet<String>();
            
            final Connection cacheConnection = instance.getCacheDataSource().getConnection();
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
     * Tests that activity course codes are generated correctly.
     */
    @Test
    public void testModuleCourseCodeGeneration() throws Exception {
        System.out.println("activityCourseCodeGeneration");
        final SynchronisationService instance = this.getService();
        final AcademicYearCode academicYearCode = new AcademicYearCode("2012/3");
        final Connection rdbConnection = instance.getRdbDataSource().getConnection();
        try {
            final RdbIdSource rdbIdSource = new SequentialRdbIdSource();
            final Module expResultModule = RdbUtil.createTestModule(rdbConnection, academicYearCode, rdbIdSource);
        
            instance.synchroniseTimetablingData();

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
     * Test of generateGroupNames method, of class SynchronisationService.
     */
    @Test
    public void testGenerateGroupNames() throws Exception {
        System.out.println("generateGroupNames");
        final AcademicYearCode academicYearCode = new AcademicYearCode("2012/3");
        final SynchronisationService instance = this.getService();
        final Connection rdbConnection = instance.getRdbDataSource().getConnection();
        try {
            final RdbIdSource rdbIdSource = new SequentialRdbIdSource();
            final ActivityType tutorialType = RdbUtil.createTestActivityType(rdbConnection, "Tutorial", rdbIdSource);
            final Module module = RdbUtil.createTestModule(rdbConnection, academicYearCode, rdbIdSource);
            
            // Generate a pair of activities that we can build names for
            final ActivityTemplate activityTemplateSync = RdbUtil.createTestActivityTemplate(rdbConnection, module,
                    tutorialType, "Tutorials", RdbUtil.TemplateForVle.FOR_VLE, rdbIdSource);
            
            int activityId = 1;
            
            RdbUtil.createTestActivity(rdbConnection, activityTemplateSync,
                    module, RdbUtil.SchedulingMethod.SCHEDULED, activityId++, rdbIdSource);
            RdbUtil.createTestActivity(rdbConnection, activityTemplateSync,
                    module, RdbUtil.SchedulingMethod.SCHEDULED, activityId++, rdbIdSource);
            
            final Connection cacheConnection = instance.getCacheDataSource().getConnection();
            try {
                instance.synchroniseTimetablingData();
                instance.generateGroupNames(cacheConnection);
            } finally {
                cacheConnection.close();
            }
            // XXX: Validate the generated group names
        } finally {
            rdbConnection.close();
        }
    }

    public ActivityDao getActivityDao() throws BeansException {
        return this.applicationContext.getBean(ActivityDao.class);
    }

    public ModuleDao getModuleDao() throws BeansException {
        return this.applicationContext.getBean(ModuleDao.class);
    }

    /**
     * Test of mapStudentSetsToUsers method, of class SynchronisationService.
     */
    /*
    @Test
    public void testMapStudentSetsToUsers() throws Exception {
        System.out.println("mapStudentSetsToUsers");
        SynchronisationRun run = null;
        SynchronisationService instance = this.getService();
        instance.mapStudentSetsToUsers(run);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
     */

    /**
     * Test of updateGroupDescriptions method, of class SynchronisationService.
     */
    /*
    @Test
    public void testUpdateGroupDescriptions() throws Exception {
        System.out.println("updateGroupDescriptions");
        SynchronisationService instance = this.getService();
        instance.updateGroupDescriptions();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */
}
