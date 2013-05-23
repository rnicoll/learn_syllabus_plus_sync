package uk.ac.ed.learn9.bb.timetabling.dao;

import java.util.List;
import uk.ac.ed.learn9.bb.timetabling.data.Module;

/**
 * Data access object for loading Timetabling modules from the staging
 * database.
 */
public interface ModuleDao {
    /**
     * Retrieves an module by its ID.
     * 
     * @param moduleId a timetabling ID (as in a 32 character unique identifier).
     * @return the module.
     */
    public Module getById(final int moduleId);
    
    /**
     * Retrieves all modules in the staging database.
     * @return a list of modules.
     */
    public List<Module> getAll();
}
