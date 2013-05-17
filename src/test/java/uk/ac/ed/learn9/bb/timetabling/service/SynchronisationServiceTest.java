package uk.ac.ed.learn9.bb.timetabling.service;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import uk.ac.ed.learn9.bb.timetabling.RdbIdSource;
import uk.ac.ed.learn9.bb.timetabling.SequentialRdbIdSource;
import uk.ac.ed.learn9.bb.timetabling.dao.ModuleDao;
import uk.ac.ed.learn9.bb.timetabling.data.AcademicYearCode;
import uk.ac.ed.learn9.bb.timetabling.data.Module;
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

    /**
     * Test of applyEnrolmentChanges method, of class SynchronisationService.
     */
    /*
    @Test
    public void testApplyEnrolmentChanges() throws Exception {
        System.out.println("applyEnrolmentChanges");
        SynchronisationRun run = null;
        SynchronisationService instance = new SynchronisationService();
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
        
        SynchronisationService instance = new SynchronisationService();
        
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
        SynchronisationService instance = new SynchronisationService();
        instance.createGroupsForActivities(run);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
     */

    /**
     * Test of generateGroupNames method, of class SynchronisationService.
     */
    /*
    @Test
    public void testGenerateGroupNames() throws Exception {
        System.out.println("generateGroupNames");
        Connection connection = null;
        SynchronisationService instance = new SynchronisationService();
        instance.generateGroupNames(connection);
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
        SynchronisationService instance = new SynchronisationService();
        instance.mapModulesToCourses();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
     */
    
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
     * Test of synchroniseTimetablingData method, of class SynchronisationService.
     */
    @Test
    public void testTimetablingSynchroniseData() throws Exception {
        System.out.println("synchroniseTimetablingData");
        final SynchronisationService instance = this.getService();
        
        final AcademicYearCode academicYearCode = new AcademicYearCode("2013/4");
        final Module testModule;
        final Connection rdbConnection = instance.getRdbDataSource().getConnection();
        try {
            final RdbIdSource rdbIdSource = new SequentialRdbIdSource();
            testModule = RdbUtil.createTestModule(rdbConnection, academicYearCode, rdbIdSource);
        } finally {
            rdbConnection.close();
        }
        
        instance.synchroniseTimetablingData();
        
        final ModuleDao moduleDao = this.applicationContext.getBean(ModuleDao.class);
        final List<Module> modules = moduleDao.getAll();
        
        assertEquals(1, modules.size());
    }

    /**
     * Test of startNewRun method, of class SynchronisationService.
     */
    /*
    @Test
    public void testStartNewRun() throws Exception {
        System.out.println("startNewRun");
        Connection destination = null;
        SynchronisationService instance = new SynchronisationService();
        SynchronisationRun expResult = null;
        SynchronisationRun result = instance.startNewRun(destination);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
     */

    /**
     * Test of mapStudentSetsToUsers method, of class SynchronisationService.
     */
    /*
    @Test
    public void testMapStudentSetsToUsers() throws Exception {
        System.out.println("mapStudentSetsToUsers");
        SynchronisationRun run = null;
        SynchronisationService instance = new SynchronisationService();
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
        SynchronisationService instance = new SynchronisationService();
        instance.updateGroupDescriptions();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */
}
