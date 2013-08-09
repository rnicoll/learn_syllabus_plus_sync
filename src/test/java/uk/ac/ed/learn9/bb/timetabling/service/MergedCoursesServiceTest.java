package uk.ac.ed.learn9.bb.timetabling.service;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import uk.ac.ed.learn9.bb.timetabling.data.LearnCourseCode;
import uk.ac.ed.learn9.bb.timetabling.util.DbScriptUtil;

@ContextConfiguration(locations={"classpath:applicationContext-test.xml"})
public class MergedCoursesServiceTest extends AbstractJUnit4SpringContextTests {
    /**
     * Location of the script used to generate the test BBL Feeds schema,
     * expressed in the Spring resource location format.
     */
    public static final String LOCATION_BBL_FEEDS_DB_SCHEMA_RESOURCE = "classpath:bblfeeds_db_schema.sql";
    /**
     * Location of the script used to destroy the test BBL Feeds schema,
     * expressed in the Spring resource location format.
     */
    public static final String LOCATION_BBL_FEEDS_DB_DROP_RESOURCE = "classpath:bblfeeds_db_drop.sql";
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
    
    public MergedCoursesServiceTest() {
    }
    
    /**
     * Gets an instance of {@link MergedCoursesService}.
     * 
     * @return an instance of {@link MergedCoursesService}.
     */
    public MergedCoursesService getService() {
        return this.applicationContext.getBean(MergedCoursesService.class);
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
        
        final Connection bblFeedsConnection = this.getService().getBblFeedsDataSource().getConnection();
        
        try {
            final File bblFeedsDbSchema = this.applicationContext.getResource(LOCATION_BBL_FEEDS_DB_SCHEMA_RESOURCE).getFile();
            DbScriptUtil.runScript(bblFeedsConnection, bblFeedsDbSchema);
        } finally {
            bblFeedsConnection.close();
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
        
        final Connection bblFeedsConnection = this.getService().getBblFeedsDataSource().getConnection();
        
        try {
            final File bblFeedsDbSchema = this.applicationContext.getResource(LOCATION_BBL_FEEDS_DB_DROP_RESOURCE).getFile();
            DbScriptUtil.runScript(bblFeedsConnection, bblFeedsDbSchema);
        } finally {
            bblFeedsConnection.close();
        }
    }

    /**
     * Test of synchronisation of merged courses data from BBL feeds database
     * into the staging database.
     */
    @Test
    public void testGetMergedCourses() throws Exception {
        System.out.println("getMergedCourses");
        final LearnCourseCode childCourseCode = new LearnCourseCode("ELCH080072013-4SV1SEM2");
        final LearnCourseCode parentCourseCode = new LearnCourseCode("ELCH080072013-4SV1YR");
        final MergedCoursesService service = this.getService();
        
        assertTrue(service.getMergedCourses(childCourseCode).isEmpty());
        
        final List<LearnCourseCode> expResult = Collections.singletonList(childCourseCode);
        
        assertEquals(expResult, service.getMergedCourses(parentCourseCode));
    }

    /**
     * Test of synchronisation of merged courses data from BBL feeds database
     * into the staging database. Currently only an SQL test, does not actually
     * confirm data has synchronised correctly.
     */
    @Test
    public void testSynchroniseMergedCourses() throws Exception {
        System.out.println("synchroniseMergedCourses");
        final MergedCoursesService service = this.getService();
        
        service.synchroniseMergedCourses();
        
        // Have seen a problem where this fails after first run, so doing it
        // twice.
        service.synchroniseMergedCourses();
        
        final PreparedStatement statement = service.getStagingDataSource().getConnection().prepareStatement(
                "SELECT COUNT(LEARN_SOURCE_COURSE_CODE) FROM LEARN_MERGED_COURSE");
        try {
            final ResultSet rs = statement.executeQuery();
            
            try {
                assertTrue (rs.next());
                final int recordCount = rs.getInt(1);
                
                // It would be good to have a more specific test than just the
                // number of rows of data.
                assertEquals(78, recordCount);
            } finally {
                rs.close();
            }
        } finally {
            statement.close();
        }
    }
}
