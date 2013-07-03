package uk.ac.ed.learn9.bb.timetabling.dao;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import blackboard.persist.Id;
import uk.ac.ed.learn9.bb.timetabling.util.DbScriptUtil;

/**
 *
 * @author jnicoll2
 */
@ContextConfiguration(locations={"classpath:applicationContext-test.xml"})
public class EnrolmentChangePartDaoTest extends AbstractJUnit4SpringContextTests {
    public EnrolmentChangePartDaoTest() {
    }
    
    /**
     * Name of the staging data source bean.
     */
    public static final String BEAN_STAGING_DATA_SOURCE = "stagingDataSource";
    
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
    
    /**
     * Gets an instance of {@link EnrolmentChangePartDao}.
     * 
     * @return an instance of {@link EnrolmentChangePartDao}.
     */
    public EnrolmentChangePartDao getDao() {
        return (EnrolmentChangePartDao)this.applicationContext.getBean("enrolmentChangePartDao");
    }
    
    /**
     * Get a connection to the staging database.
     * 
     * @return a connection to the staging database.
     * @throws SQLException if there was a problem connecting to the database.
     */
    private Connection getConnection() throws SQLException {
        final DataSource stagingDatasource = this.applicationContext.getBean(BEAN_STAGING_DATA_SOURCE, javax.sql.DataSource.class);
        return stagingDatasource.getConnection();
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
        final Connection stagingConnection = this.getConnection();
        
        try {
            final File syncDbSchema = this.applicationContext.getResource(LOCATION_STAGING_DB_SCHEMA_RESOURCE).getFile();
            DbScriptUtil.runScript(stagingConnection, syncDbSchema);
        } finally {
            stagingConnection.close();
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
        final Connection stagingConnection = this.getConnection();
        
        try {
            final File syncDbSchema = this.applicationContext.getResource(LOCATION_STAGING_DB_DROP_RESOURCE).getFile();
            DbScriptUtil.runScript(stagingConnection, syncDbSchema);
        } finally {
            stagingConnection.close();
        }
    }

    /**
     * Test of getByCourse method, of class EnrolmentChangePartDao.
     */
    @Test
    public void testGetByCourse() {
        System.out.println("getByCourse");
        
        final EnrolmentChangePartDao dao = this.getDao();
        final List result = dao.getByCourse(Id.UNSET_ID);
    }
}
