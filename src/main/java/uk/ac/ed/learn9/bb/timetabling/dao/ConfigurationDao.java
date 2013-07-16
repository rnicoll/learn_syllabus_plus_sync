package uk.ac.ed.learn9.bb.timetabling.dao;

import uk.ac.ed.learn9.bb.timetabling.data.Configuration;

/**
 * Data access object for configuration.
 */
public interface ConfigurationDao {
    public Configuration getDefault();
}
