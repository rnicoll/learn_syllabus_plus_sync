package uk.ac.ed.learn9.bb.timetabling.dao;

import java.util.List;

import uk.ac.ed.learn9.bb.timetabling.data.StudentSet;

/**
 * Data access object for loading Timetabling student sets from the staging
 * database.
 */
public interface StudentSetDao {
    /**
     * Retrieves a student set by its ID.
     * 
     * @param studentSetId a timetabling ID (as in a 32 character unique identifier).
     * @return the student set.
     */
    public StudentSet getById(final String studentSetId);
    
    /**
     * Retrieves all student sets in the staging database.
     * @return a list of student sets.
     */
    public List<StudentSet> getAll();
    
    /**
     * Refreshes a student set from the database.
     * 
     * @param student set the student set to be refreshed
     */
    public void refresh(final StudentSet studentSet);
}
