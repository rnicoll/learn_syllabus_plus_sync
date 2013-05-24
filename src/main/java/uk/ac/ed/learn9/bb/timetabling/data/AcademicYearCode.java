package uk.ac.ed.learn9.bb.timetabling.data;

/**
 * Type-safe object for wrapping academic year codes as stored in EUCLID,
 * for example "2012/3".
 */
public class AcademicYearCode extends AbstractCode<AcademicYearCode> {
    /**
     * Regular expression for validating academic year codes.
     */
    public static final String ACADEMIC_YEAR_REGEXP = "[0-9][0-9][0-9][0-9]+/[0-9]";
    
    /**
     * Constructs the course code with the given value.
     * 
     * @param setValue the course code value.
     * @throws IllegalArgumentException if the course code is invalid.
     */
    public              AcademicYearCode(final String setValue)
        throws IllegalArgumentException {
        super(setValue);
        
        if (!setValue.matches(ACADEMIC_YEAR_REGEXP)) {
            throw new IllegalArgumentException("Course code \""
                + setValue + "\" is invalid; does not match expected format.");
        }
    }
}
