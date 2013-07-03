package uk.ac.ed.learn9.bb.timetabling.data;

/**
 * Type-safe object for wrapping string-based primary key codes.
 * 
 * @param <T> the type of code (as in the implementation class).
 */
public abstract class AbstractCode<T extends AbstractCode> extends Object implements Comparable<T>, CharSequence {
    private final String val;
    
    /**
     * Constructs the code object wrapping around the given value.
     * 
     * @param setValue the code value to set. MUST NOT be null.
     * @throws IllegalArgumentException if the value is invalid (for example
     * null).
     */
    public              AbstractCode(final String setValue)
        throws IllegalArgumentException {
        if (null == setValue) {
            throw new IllegalArgumentException("Code value cannot be null.");
        }
        
        this.val = setValue;
    }
    
    /**
     * Compares this code with another based on string comparison of the stored values.
     * 
     * @param other the other code to compare with.
     * @return 0 if they match, -1 if this is less than the other code, 1 if
     * greater.
     */
    @Override
    public int compareTo(final AbstractCode other) {
        return this.val.compareTo(other.val);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (null == o) {
            return false;
        }
        
        if (o instanceof AbstractCode) {
            final AbstractCode other = (AbstractCode)o;
            
            return this.val.equals(other.val);
        } else if (o instanceof CharSequence) {
            return this.val.equals(o.toString());
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
