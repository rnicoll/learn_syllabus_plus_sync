package uk.ac.ed.learn9.bb.timetabling.service;

/**
 * Exception thrown if the differences from the last run are beyond likely
 * realistic change values (set by the threshold in the configuration).
 */
public class ThresholdException extends Exception {
    public              ThresholdException(final String message) {
        super(message);
    }
}
