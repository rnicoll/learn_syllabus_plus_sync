package uk.ac.ed.learn9.bb.timetabling;

import java.util.Arrays;

/**
 * ID generator based on a fixed starting value,
 */
public class SequentialRdbIdSource implements RdbIdSource {
    private int id;
    
    /**
     * Constructs a new ID generator starting with a seed value of 1.
     */
    public              SequentialRdbIdSource() {
        this(1);
    }
    
    /**
     * Constructs a new ID generator with the given seed.
     * 
     * @param seed the seed value, must be above 0.
     */
    public              SequentialRdbIdSource(final int seed) {
        assert seed > 0;
        
        this.id = seed;
    }

    @Override
    public String getId() {
        final String idHex = Integer.toHexString(this.id++);
        
        final int paddingNeeded = ID_LENGTH - idHex.length();
        final char[] padding = new char[paddingNeeded];
        
        Arrays.fill(padding, 0, paddingNeeded, '0');
        
        return new String(padding) + idHex;
    }
}
