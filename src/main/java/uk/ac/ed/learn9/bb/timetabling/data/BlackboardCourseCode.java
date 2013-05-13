package uk.ac.ed.learn9.bb.timetabling.data;

/**
 * Type-safe object for wrapping course codes as used in Learn. Used to help
 * differentiate from course codes in Timetabling.
 */
public class BlackboardCourseCode extends AbstractCourseCode<BlackboardCourseCode> {
    public              BlackboardCourseCode(final String setValue) {
        super(setValue);
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
    public static BlackboardCourseCode buildCode(final String courseCode,
            final String ayrCode, final String occurrence, final String semester) {
        assert null != courseCode;
        assert null != ayrCode;
        assert null != occurrence;
        assert null != semester;
        
        final String learnAyrCode = ayrCode.replace('/', '-');
                    
        return new BlackboardCourseCode(courseCode + learnAyrCode + occurrence + semester);
    }
}
