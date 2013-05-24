package uk.ac.ed.learn9.bb.timetabling.data;

/**
 * Type-safe object for wrapping academic year codes as stored in EUCLID,
 * for example "2012/3".
 */
public class AcademicYearCode extends AbstractCode<AcademicYearCode> {
    public              AcademicYearCode(final String setValue)
        throws IllegalArgumentException {
        super(setValue);
    }
}
