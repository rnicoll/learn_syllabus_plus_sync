package uk.ac.ed.learn9.bb.timetabling.data;

/**
 * Type-safe object for wrapping course codes. This is used because course
 * code syntax is different in Timetabling and Learn, and therefore it helps
 * ensure the two are not mixed up.
 */
public abstract class AbstractCourseCode<T extends AbstractCourseCode> extends Object implements Comparable<T>, CharSequence {
    private final String val;
    
    public              AbstractCourseCode(final String setValue)
        throws IllegalArgumentException {
        if (null == setValue) {
            throw new IllegalArgumentException("Course code value cannot be null.");
        }
        
        this.val = setValue;
    }
    
    @Override
    public int compareTo(final AbstractCourseCode other) {
        return this.val.compareTo(other.val);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (null == o) {
            return false;
        }
        
        if (o instanceof CharSequence) {
            return this.val.equals(o);
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return val.hashCode();
    }
    
    @Override
    public String toString() {
        return this.val;
    }
    
    @Override
    public int length() {
        return val.length();
    }

    @Override
    public char charAt(int i) {
        return val.charAt(i);
    }

    @Override
    public CharSequence subSequence(int i, int i1) {
        return val.subSequence(i, i1);
    }
    
}
