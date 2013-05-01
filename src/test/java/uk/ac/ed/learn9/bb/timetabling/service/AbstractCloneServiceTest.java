package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import static org.junit.Assert.*;

/**
 *
 * @author jnicoll2
 */
public class AbstractCloneServiceTest {

    public AbstractCloneServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of cloneTable method, of class AbstractCloneService.
     */
    /*
    @org.junit.Test
    public void testCloneTable() throws Exception {
        System.out.println("cloneTable");
        Connection source = null;
        Connection destination = null;
        String sourceTable = "";
        String destinationTable = "";
        String sourcePkField = "";
        String destinationPkField = "";
        Map<String, String> fieldMappings = null;
        AbstractCloneService instance = new AbstractCloneServiceImpl();
        instance.cloneTable(source, destination, sourceTable, destinationTable, sourcePkField, destinationPkField, fieldMappings);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */

    /**
     * Test of cloneQuery method, of class AbstractCloneService.
     */
    /*
    @org.junit.Test
    public void testCloneQuery() throws Exception {
        System.out.println("cloneQuery");
        PreparedStatement sourceStatement = null;
        PreparedStatement destinationStatement = null;
        String sourcePkField = "";
        String destinationPkField = "";
        Map<String, String> fieldMappings = null;
        AbstractCloneService instance = new AbstractCloneServiceImpl();
        instance.cloneQuery(sourceStatement, destinationStatement, sourcePkField, destinationPkField, fieldMappings);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */

    /**
     * Test of buildQuery method, of class AbstractCloneService.
     */
    @org.junit.Test
    public void testBuildQuery() {
        System.out.println("buildQuery");
        
        final String table = "ACTIVITY";
        final String pkField = "ACTIVITY_ID";
        final Collection<String> otherFields = Collections.emptySet();
        final AbstractCloneService instance = new AbstractCloneServiceImpl();
        final String expResult = "SELECT ACTIVITY_ID FROM ACTIVITY ORDER BY ACTIVITY_ID";
        final String result = instance.buildQuery(table, pkField, otherFields);
        
        assertEquals(expResult, result);
    }

    /**
     * Tests building a query against a table, where there are parameters
     * beyond the primary key.
     */
    @org.junit.Test
    public void testBuildQueryWithParameters() {
        System.out.println("buildQuery");
        
        final String table = "ACTIVITY";
        final String pkField = "ACTIVITY_ID";
        final Collection<String> otherFields = Arrays.asList(new String[] {"FIELD_1", "FIELD_2"});
        final AbstractCloneService instance = new AbstractCloneServiceImpl();
        final String expResult = "SELECT ACTIVITY_ID, FIELD_1, FIELD_2 FROM ACTIVITY ORDER BY ACTIVITY_ID";
        final String result = instance.buildQuery(table, pkField, otherFields);
        
        assertEquals(expResult, result);
    }

    public class AbstractCloneServiceImpl extends AbstractCloneService {
    }
}