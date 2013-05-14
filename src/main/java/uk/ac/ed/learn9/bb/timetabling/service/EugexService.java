package uk.ac.ed.learn9.bb.timetabling.service;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for monitoring which courses in Learn are synchronised from EUGEX.
 */
@Service
public class EugexService {
    @Autowired
    private DataSource cacheDataSource;
    @Autowired
    private DataSource eugexDataSource;

    void synchroniseVleActiveCourses() {
        // throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * @return the cacheDataSource
     */
    public DataSource getCacheDataSource() {
        return cacheDataSource;
    }

    /**
     * @return the eugexDataSource
     */
    public DataSource getEugexDataSource() {
        return eugexDataSource;
    }

    /**
     * @param cacheDataSource the cacheDataSource to set
     */
    public void setCacheDataSource(DataSource cacheDataSource) {
        this.cacheDataSource = cacheDataSource;
    }

    /**
     * @param eugexDataSource the eugexDataSource to set
     */
    public void setEugexDataSource(DataSource eugexDataSource) {
        this.eugexDataSource = eugexDataSource;
    }
    
}
