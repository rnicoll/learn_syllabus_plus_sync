package uk.ac.ed.learn9.bb.timetabling.dao;

import java.util.List;

import blackboard.persist.Id;
import uk.ac.ed.learn9.bb.timetabling.data.ModuleCourse;

/**
 * Data access object for loading module-course relationships from the staging
 * database.
 */
public interface ModuleCourseDao {
    /**
     * Retrieves an module-course by its ID.
     * 
     * @param moduleCourseId a unique module-course relationship ID.
     */
    public ModuleCourse getById(final int moduleCourseId);
    
    /**
     * Retrieves module-courses by the ID used for the module in timetabling.
     * 
     * @param moduleId a timetabling module ID.
     */
    public List<ModuleCourse> getByTimetablingId(final String moduleId);

    /**
     * Retrieves module-courses by the ID of the Learn course.
     * 
     * @param id a Learn course ID.
     * @return a list of module-courses matching the given Learn course.
     */
    public List<ModuleCourse> getByLearnId(final Id id);
}
