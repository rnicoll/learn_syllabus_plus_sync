package uk.ac.ed.learn9.bb.timetabling.data;

/**
 * Type-safe object for wrapping course codes as used in Learn. Used to help
 * differentiate from course codes in Timetabling.
 */
public class TimetablingCourseCode extends AbstractCourseCode<TimetablingCourseCode> {
    public              TimetablingCourseCode(final String setValue) {
        super(setValue);
        
        if (!setValue.matches("[A-Z]+[0-9]+_[A-Z][A-Z0-9]+_[A-Z][A-Z0-9]+")) {
            throw new IllegalArgumentException("Course code \""
                + setValue + "\" is invalid.");
        }
    }
    
    /**
     * Builds the course code for a course in Timetabling, based on the component parts
     * of the code.
     * 
     * @param courseCode the course code for the course in general, for example "PLIT08005".
     * @param occurrence the occurrence of the course, for example "SV1".
     * @param semester the semester the course is running in, for example "SEM1".
     */
    public static TimetablingCourseCode buildCode(final String courseCode,
            final String occurrence, final String semester) {
        assert null != courseCode;
        assert null != occurrence;
        assert null != semester;
                    
        return new TimetablingCourseCode(courseCode + "_"
            + occurrence + "_"
            + semester);
    }
    
    /**
     * Splits the course code into its component parts:
     * 
     * <ol>
     *   <li>Course code</li>
     *   <li>Occurrence</li>
     *   <li>Semester</li>
     * </ol>
     * @return 
     */
    public String[] splitCode() {
        // Split the course code into the course, semester and occurrence
        return this.toString().split("_");
    }
}
