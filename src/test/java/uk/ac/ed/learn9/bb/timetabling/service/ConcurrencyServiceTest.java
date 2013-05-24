package uk.ac.ed.learn9.bb.timetabling.service;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun;
import uk.ac.ed.learn9.bb.timetabling.service.ConcurrencyService.SynchronisationAlreadyInProgressException;
import uk.ac.ed.learn9.bb.timetabling.util.DbScriptUtil;

@ContextConfiguration(locations={"classpath:applicationContext-test.xml"})
public class ConcurrencyServiceTest extends AbstractJUnit4SpringContextTests {
    private static final long A_FEW_DAYS_IN_MILLIS = 3 * 24 * 60 * 60 * 1000L;
    
    public ConcurrencyServiceTest() {
    }
    
    public ConcurrencyService getService() {
        return this.applicationContext.getBean(ConcurrencyService.class);
    }
    
    /**
     * Constructs schemas for the test staging database, before each test is run.
     * 
     * @throws IOException if there was a problem accessing the database creation
     * script.
     * @throws SQLException if there was a problem applying the database creation
     * script.
     * @see #after() 
     */
    @Before
    public void before() throws IOException, SQLException {
        final Connection syncConnection = this.getService().getStagingDataSource().getConnection();
        
        try {
            final File syncDbSchema = this.applicationContext.getResource(SynchronisationServiceTest.LOCATION_STAGING_DB_SCHEMA_RESOURCE).getFile();
            DbScriptUtil.runScript(syncConnection, syncDbSchema);
        } finally {
            syncConnection.close();
        }
    }
    
    /**
     * Drops the staging database schema, after each test is run, to ensure there
     * are no side-effects from each test.
     * 
     * @throws IOException if there was a problem accessing the database drop script.
     * @throws SQLException if there was a problem applying the database drop script.
     * @see #before() 
     */
    @After
    public void after() throws IOException, SQLException {
        final Connection syncConnection = this.getService().getStagingDataSource().getConnection();
        
        try {
            final File syncDbSchema = this.applicationContext.getResource(SynchronisationServiceTest.LOCATION_STAGING_DB_DROP_RESOURCE).getFile();
            DbScriptUtil.runScript(syncConnection, syncDbSchema);
        } finally {
            syncConnection.close();
        }
    }

    /**
     * Test of startNewRun method, of class ConcurrencyService.
     */
    @Test
    public void testStartNewRun() throws Exception {
        System.out.println("startNewRun");
        final ConcurrencyService instance = this.getService();
        final SynchronisationRun result = instance.startNewRun();
    }

    /**
     * Test of startNewRun method, of class ConcurrencyService.
     */
    @Test(expected=SynchronisationAlreadyInProgressException.class)
    public void testStartNewRunCollision() throws Exception {
        System.out.println("startNewRun");
        final ConcurrencyService instance = this.getService();
        final SynchronisationRun resultA = instance.startNewRun();
        final SynchronisationRun resultB = instance.startNewRun();
    }

    /**
     * Test of timeoutOldSessions method, of class ConcurrencyService.
     */
    @Test
    public void testTimeoutOldSessions() throws Exception {
        System.out.println("timeoutOldSessions");
        ConcurrencyService instance = this.getService();
        Connection cacheDatabase = instance.getStagingDataSource().getConnection();
        try {
            final Timestamp now = new Timestamp(System.currentTimeMillis());
            final Timestamp aWhileAgo = new Timestamp(now.getTime() - A_FEW_DAYS_IN_MILLIS);
            int runId = 1;
            
            instance.insertRunRecord(cacheDatabase, runId++, aWhileAgo);
            instance.insertRunRecord(cacheDatabase, runId++, now);
            assertEquals(1, instance.timeoutOldSessions(cacheDatabase, now));
        } finally {
            cacheDatabase.close();
        }
    }
}
