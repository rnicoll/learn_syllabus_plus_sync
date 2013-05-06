package uk.ac.ed.learn9.bb.timetabling.data;

import java.sql.Timestamp;

/**
 *
 * @author jnicoll2
 */
public interface EnrolmentChange extends Comparable<EnrolmentChange> {
    /**
     * Returns a human readable label for this change type.
     * 
     * @return the type of change, for example "Add" or "Remove".
     */
    public String getChangeType();

    /**
     * Returns the synchronisation run this change belongs to.
     * 
     * @return the synchronisation run this change belongs to.
     */
    public SynchronisationRun getRun();

    /**
     * Returns the activity that the student is being added to.
     * 
     * @return the activity that the student is being added to.
     */
    public Activity getActivity();

    /**
     * @return the studentSet
     */
    public StudentSet getStudentSet();

    /**
     * @return the time at which this change was applied to Learn.
     */
    public Timestamp getUpdateCompleted();
}
