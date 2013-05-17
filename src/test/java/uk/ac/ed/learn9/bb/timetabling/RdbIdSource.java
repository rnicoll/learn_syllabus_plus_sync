package uk.ac.ed.learn9.bb.timetabling;

/**
 * ID generator for the reporting database.
 */
public interface RdbIdSource {
    public static final int ID_LENGTH = 32;
    
    public String getId();
}
