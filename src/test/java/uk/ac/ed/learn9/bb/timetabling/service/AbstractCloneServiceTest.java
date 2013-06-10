package uk.ac.ed.learn9.bb.timetabling.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Tests for methods in {@link AbstractCloneService}.
 */
@ContextConfiguration(locations={"classpath:applicationContext-test.xml"})
public class AbstractCloneServiceTest extends AbstractJUnit4SpringContextTests {

    public AbstractCloneServiceTest() {
    }
    
    private AbstractCloneService getService() {
        return new AbstractCloneServiceImpl();
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
        AbstractCloneService instance = this.getService();
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
        AbstractCloneService instance = this.getService();
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
        final List<String> pkFields = Collections.singletonList("ACTIVITY_ID");
        final Collection<String> allFields = pkFields;
        final AbstractCloneService instance = this.getService();
        final String expResult = "SELECT ACTIVITY_ID FROM ACTIVITY ORDER BY ACTIVITY_ID";
        final String result = instance.buildQuery(table, pkFields, allFields);
        
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
        final List<String> pkFields = Collections.singletonList("ACTIVITY_ID");
        final Collection<String> allFields = Arrays.asList(new String[] {"ACTIVITY_ID", "FIELD_1", "FIELD_2"});
        final AbstractCloneService instance = this.getService();
        final String expResult = "SELECT ACTIVITY_ID, FIELD_1, FIELD_2 FROM ACTIVITY ORDER BY ACTIVITY_ID";
        final String result = instance.buildQuery(table, pkFields, allFields);
        
        assertEquals(expResult, result);
    }

    public class AbstractCloneServiceImpl extends AbstractCloneService {
    }
}
