package uk.ac.ed.learn9.bb.timetabling.service;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import uk.ac.ed.learn9.bb.timetabling.dao.SynchronisationRunDao;
import uk.ac.ed.learn9.bb.timetabling.data.cache.SynchronisationRun;
import uk.ac.ed.learn9.bb.timetabling.util.DbScriptUtil;

@ContextConfiguration(locations={"classpath:applicationContext-test.xml"})
public class ConcurrencyServiceTest extends AbstractJUnit4SpringContextTests {
    
    public ConcurrencyServiceTest() {
    }
    
    public ConcurrencyService getService() {
        return this.applicationContext.getBean(ConcurrencyService.class);
    }
    
    @Before
    public void before() throws IOException, SQLException {
        final Connection syncConnection = this.getService().getCacheDataSource().getConnection();
        
        try {
            final File syncDbSchema = this.applicationContext.getResource(SynchronisationServiceTest.LOCATION_SYNC_DB_SCHEMA_RESOURCE).getFile();
            DbScriptUtil.runScript(syncConnection, syncDbSchema);
        } finally {
            syncConnection.close();
        }
    }
    
    @After
    public void after() throws IOException, SQLException {
        final Connection syncConnection = this.getService().getCacheDataSource().getConnection();
        
        try {
            final File syncDbSchema = this.applicationContext.getResource(SynchronisationServiceTest.LOCATION_SYNC_DB_DROP_RESOURCE).getFile();
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
     * Test of timeoutOldSessions method, of class ConcurrencyService.
     */
    @Test
    public void testTimeoutOldSessions() throws Exception {
        System.out.println("timeoutOldSessions");
        ConcurrencyService instance = this.getService();
        Connection cacheDatabase = instance.getCacheDataSource().getConnection();
        try {
            final Timestamp now = new Timestamp(System.currentTimeMillis());
            
            instance.timeoutOldSessions(cacheDatabase, now);
        } finally {
            cacheDatabase.close();
        }
    }
}
