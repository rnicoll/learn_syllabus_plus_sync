package uk.ac.ed.learn9.bb.timetabling.data;

/**
 * Type-safe object for wrapping course codes as used in Learn. Used to help
 * differentiate from course codes in Timetabling.
 */
public class TimetablingCourseCode extends AbstractCourseCode<TimetablingCourseCode> {
    /**
     * A regular expression used to validate timetabling course codes.
     */
    public static final String TT_COURSE_CODE_REGEXP = "[A-Z]+[0-9]+_[A-Z][A-Z0-9]+_[A-Z][A-Z0-9]+";
    
    public              TimetablingCourseCode(final String setValue) {
        super(setValue);
        
        if (!setValue.matches(TT_COURSE_CODE_REGEXP)) {
            throw new IllegalArgumentException("Course code \""
                + setValue + "\" is invalid; does not match expected format.");
        }
    }
}
