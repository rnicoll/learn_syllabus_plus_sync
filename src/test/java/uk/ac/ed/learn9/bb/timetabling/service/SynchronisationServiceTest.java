package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.ac.ed.learn9.bb.timetabling.dao.SynchronisationRunDao;
import uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun;

public class SynchronisationServiceTest {
    
    public SynchronisationServiceTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
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

    /**
     * Test of synchroniseData method, of class SynchronisationService.
     */
    /*
    @Test
    public void testSynchroniseData() throws Exception {
        System.out.println("synchroniseData");
        SynchronisationService instance = new SynchronisationService();
        instance.synchroniseData();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
     */

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
