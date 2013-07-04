package uk.ac.ed.learn9.bb.timetabling.service;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import uk.ac.ed.learn9.bb.timetabling.util.DbScriptUtil;

@ContextConfiguration(locations={"classpath:applicationContext-test.xml"})
public class EugexServiceTest extends AbstractJUnit4SpringContextTests {
    /**
     * Location of the script used to generate the test reporting database schema,
     * expressed in the Spring resource location format.
     */
    public static final String LOCATION_EUGEX_DB_SCHEMA_RESOURCE = "classpath:eugex_db_schema.sql";
    /**
     * Location of the script used to destroy the test reporting database schema,
     * expressed in the Spring resource location format.
     */
    public static final String LOCATION_EUGEX_DB_DROP_RESOURCE = "classpath:eugex_db_drop.sql";
    /**
     * Location of the script used to generate the test staging database schema,
     * expressed in the Spring resource location format.
     */
    public static final String LOCATION_STAGING_DB_SCHEMA_RESOURCE = SynchronisationServiceTest.LOCATION_STAGING_DB_SCHEMA_RESOURCE;
    /**
     * Location of the script used to destroy the test staging database schema,
     * expressed in the Spring resource location format.
     */
    public static final String LOCATION_STAGING_DB_DROP_RESOURCE = SynchronisationServiceTest.LOCATION_STAGING_DB_DROP_RESOURCE;
    
    /**
     * Default constructor.
     */
    public EugexServiceTest() {
    }
    
    /**
     * Gets an instance of {@link SynchronisationService}.
     * 
     * @return an instance of {@link SynchronisationService}.
     */
    public EugexSynchroniseService getService() {
        return this.applicationContext.getBean(EugexSynchroniseService.class);
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
        
        final Connection eugexConnection = this.getService().getEugexDataSource().getConnection();
        
        try {
            final File eugexDbSchema = this.applicationContext.getResource(LOCATION_EUGEX_DB_SCHEMA_RESOURCE).getFile();
            DbScriptUtil.runScript(eugexConnection, eugexDbSchema);
        } finally {
            eugexConnection.close();
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
        final Connection syncConnection = this.getService().getStagingDataSource().getConnection();
        
        try {
            final File syncDbSchema = this.applicationContext.getResource(LOCATION_STAGING_DB_DROP_RESOURCE).getFile();
            DbScriptUtil.runScript(syncConnection, syncDbSchema);
        } finally {
            syncConnection.close();
        }
        
        final Connection eugexConnection = this.getService().getEugexDataSource().getConnection();
        
        try {
            final File eugexDbSchema = this.applicationContext.getResource(LOCATION_EUGEX_DB_DROP_RESOURCE).getFile();
            DbScriptUtil.runScript(eugexConnection, eugexDbSchema);
        } finally {
            eugexConnection.close();
        }
    }

    /**
     * Test of synchroniseVleActiveCourses method, of class EugexService.
     */
    @Test
    public void testSynchroniseVleActiveCourses() throws Exception {
        System.out.println("synchroniseVleActiveCourses");
        final EugexSynchroniseService instance = this.getService();
        instance.synchroniseVleActiveCourses();
    }
}
