package uk.ac.ed.learn9.bb.timetabling.data;

/**
 * Type-safe object for wrapping course codes. This is used because course
 * code syntax is different in Timetabling and Learn, and therefore it helps
 * ensure the two are not mixed up.
 * 
 * @param <T> the type of course code (as in the implementation class).
 */
public abstract class AbstractCourseCode<T extends AbstractCourseCode> extends AbstractCode<T> {
    /**
     * Constructs the course code with the given value.
     * 
     * @param setValue the course code value.
     * @throws IllegalArgumentException if the course code is invalid.
     */
    public              AbstractCourseCode(final String setValue)
        throws IllegalArgumentException {
        super(setValue);
    }
}
