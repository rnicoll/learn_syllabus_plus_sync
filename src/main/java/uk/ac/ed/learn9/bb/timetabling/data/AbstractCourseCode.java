package uk.ac.ed.learn9.bb.timetabling.data;

/**
 * Type-safe object for wrapping course codes. This is used because course
 * code syntax is different in Timetabling and Learn, and therefore it helps
 * ensure the two are not mixed up.
 */
public abstract class AbstractCourseCode<T extends AbstractCourseCode> extends AbstractCode<T> {
    public              AbstractCourseCode(final String setValue)
        throws IllegalArgumentException {
        super(setValue);
    }
}
