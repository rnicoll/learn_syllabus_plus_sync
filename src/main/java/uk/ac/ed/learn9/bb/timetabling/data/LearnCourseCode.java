package uk.ac.ed.learn9.bb.timetabling.data;

/**
 * Type-safe object for wrapping course codes as used in Learn. Used to help
 * differentiate from course codes in Timetabling.
 */
public class LearnCourseCode extends AbstractCourseCode<LearnCourseCode> {
    /**
     * A regular expression used to validate timetabling course codes.
     */
    public static final String LEARN_COURSE_CODE_REGEXP = "[A-Z]+[0-9]+-[0-9][A-Z0-9]+";
    
    /**
     * Constructs the Learn course code with the given value.
     * 
     * @param setValue the course code value.
     * @throws IllegalArgumentException if the course code is invalid.
     */
    public              LearnCourseCode(final String setValue) {
        super(setValue);
        
        if (!setValue.matches(LEARN_COURSE_CODE_REGEXP)) {
            throw new IllegalArgumentException("Course code \""
                + setValue + "\" is invalid; does not match expected format.");
        }
    }
    
    /**
     * Builds the course code for a course in Learn, based on the component parts
     * of the code.
     * 
     * @param courseCode the course code for the course in general, for example "PLIT08005".
     * @param ayrCode an academic year code, for example "2013/4".
     * @param occurrence the occurrence of the course, for example "SV1".
     * @param semester the semester the course is running in, for example "SEM1".
     */
    public static LearnCourseCode buildCode(final String courseCode,
            final AcademicYearCode ayrCode, final String occurrence, final String semester) {
        assert null != courseCode;
        assert null != ayrCode;
        assert null != occurrence;
        assert null != semester;
        
        final String learnAyrCode = ayrCode.toString().replace('/', '-');
                    
        return new LearnCourseCode(courseCode + learnAyrCode + occurrence + semester);
    }
}
