package uk.ac.ed.learn9.bb.timetabling.data;

/**
 * Type-safe object for wrapping course codes as used in Learn. Used to help
 * differentiate from course codes in Timetabling.
 */
public class LearnCourseCode extends AbstractCourseCode<LearnCourseCode> {
    /**
     * A regular expression used to validate timetabling course codes.
     */
    public static final String LEARN_COURSE_CODE_REGEXP = "[A-Z]+[0-9]{5,}-[0-9][A-Z0-9]+";
    
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
     * @return the course code as used in Learn.
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

    /**
     * Extract the EUCLID course code from a Learn course code.
     * 
     * @return the EUCLID course code, for example "EDUA08064".
     */
    public String getEuclidCourseId() {
        // We know there's exactly one "-" character, because it's a requirement
        // of the regular expression used in the constructor
        final String[] parts = this.toString().split("-");
        final String codeAndAyr = parts[0];
        
        // Strip the last four digits (calendar year) from the first half
        return codeAndAyr.substring(0, codeAndAyr.length() - 4);
    }

    /**
     * Returns the instance identifier for the course in Learn, used to
     * differentiate it from other instances of the same EUCLID course.
     *
     * @return the instance identifier for the course in Learn, for example
     * "2012-3SS1SEM1".
     */
    public String getInstance() {
        final String euclidCourseId = this.getEuclidCourseId();
        return this.toString().substring(euclidCourseId.length());
    }
}
