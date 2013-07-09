package uk.ac.ed.learn9.bb.timetabling.data;

/**
 * Result codes for the possible outcomes of running a synchronisation
 * process.
 */
public enum SynchronisationResult {
    /** Indicates at the synchronisation run was abandoned without any
     * changes made, typically because another run was already in progress.
     */
    ABANDONED, /**
     * Indicates there was an unrecoverable error while performing the
     * synchronisation process, such as a database link failure.
     */ FATAL, /**
     * Indicates that the synchronisation run completed successfully.
     */ SUCCESS, /**
     * Indicates that the synchronisation run appears to have timed out.
     */ TIMEOUT
    
}
