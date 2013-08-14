/**
 * Drop the constraint which stops multiple synchronisation runs from
 * generating differences against a single run, as this causes problems
 * where runs fail and are unsafe to generate differences again, but "lock"
 * the run they are based on.
 */
 
ALTER TABLE SYNCHRONISATION_RUN_PREV DROP CONSTRAINT SYNC_RUN_PREV_UNIQ;