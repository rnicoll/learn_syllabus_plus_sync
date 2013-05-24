package uk.ac.ed.learn9.bb.timetabling;

/**
 * ID generator for the reporting database. Although currently there is only
 * the sequential ID generator, this exists in case there is a need for generators
 * that use pre-defined or randomly generated IDs.
 */
public interface RdbIdSource {
    /**
     * Length, in characters, of primary key fields in the timetabling reporting
     * database. Used to determine how long generated IDs should be.
     */
    public static final int ID_LENGTH = 32;
    
    /**
     * Gets a new ID for a record in the reporting database. Guaranteed not to
     * return the same value twice from the same object, as the returned value
     * is intended for use as a primary key.
     * 
     * @return a new ID for a record in the reporting database.
     */
    public String getId();
}
