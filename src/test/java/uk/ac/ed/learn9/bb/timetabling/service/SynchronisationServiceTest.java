package uk.ac.ed.learn9.bb.timetabling.service;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import uk.ac.ed.learn9.bb.timetabling.util.DbScriptUtil;

@ContextConfiguration(locations={"classpath:applicationContext-test.xml"})
public class SynchronisationServiceTest extends AbstractJUnit4SpringContextTests {
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
        final File syncDbSchema = this.applicationContext.getResource(LOCATION_SYNC_DB_SCHEMA_RESOURCE).getFile();
        final Connection connection = this.getService().getCacheDataSource().getConnection();
        
        try {
            DbScriptUtil.runScript(connection, syncDbSchema);
        } finally {
            connection.close();
        }
    }
    
    @After
    public void after() throws IOException, SQLException {
        final File syncDbSchema = this.applicationContext.getResource(LOCATION_SYNC_DB_DROP_RESOURCE).getFile();
        final Connection connection = this.getService().getCacheDataSource().getConnection();
        
        try {
            DbScriptUtil.runScript(connection, syncDbSchema);
        } finally {
            connection.close();
        }
    }
    
    /**
     * Test of synchroniseTimetablingData method, of class SynchronisationService.
     */
    @Test
    public void testTimetablingSynchroniseData() throws Exception {
        System.out.println("synchroniseTimetablingData");
        final SynchronisationService instance = this.getService();
        instance.synchroniseTimetablingData();
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
