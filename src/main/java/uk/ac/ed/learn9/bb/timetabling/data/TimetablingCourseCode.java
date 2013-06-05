package uk.ac.ed.learn9.bb.timetabling.data;

/**
 * Type-safe object for wrapping course codes as used in Learn. Used to help
 * differentiate from course codes in Timetabling.
 */
public class TimetablingCourseCode extends AbstractCourseCode<TimetablingCourseCode> {
    /**
     * A regular expression used to validate timetabling course codes.
     */
    public static final String TT_COURSE_CODE_REGEXP = "[A-Z]+[0-9]+_[A-Z][A-Z0-9]+_[A-Z][A-Z0-9\\+]+";
    
    /**
     * Constructs the timetabling course code with the given value.
     * 
     * @param setValue the course code value.
     * @throws IllegalArgumentException if the course code is invalid.
     */
    public              TimetablingCourseCode(final String setValue) {
        super(setValue);
        
        if (!setValue.matches(TT_COURSE_CODE_REGEXP)) {
            throw new IllegalArgumentException("Course code \""
                + setValue + "\" is invalid; does not match expected format.");
        }
    }
    
    /**
     * Builds the course code for a course in Timetabling, based on the component parts
     * of the code.
     * 
     * @param courseCode the course code for the course in general, for example "PLIT08005".
     * @param occurrence the occurrence of the course, for example "SV1".
     * @param semester the semester the course is running in, for example "SEM1".
     * @return the course code as used in timetabling.
     */
    public static TimetablingCourseCode buildCode(final String courseCode,
            final String occurrence, final String semester)
            throws IllegalArgumentException {
        assert null != courseCode;
        assert null != occurrence;
        assert null != semester;
                    
        return new TimetablingCourseCode(courseCode + "_"
            + occurrence + "_"
            + semester);
    }
}
