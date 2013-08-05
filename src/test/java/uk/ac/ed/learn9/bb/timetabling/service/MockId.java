package uk.ac.ed.learn9.bb.timetabling.service;

import blackboard.persist.DataType;
import blackboard.persist.Id;


class MockId extends Id {
    private final String id;
    
    public MockId(final DataType dataType, String id) {
        super(dataType);
        this.id = id;
    }

    @Override
    public boolean isSet() {
        return null != this.id;
    }

    @Override
    public String toExternalString() {
        return this.id;
    }

    @Override
    public String toString() {
        return this.id;
    }

    @Override
    public void setContainer() {
        // Do something?
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MockId)) {
            return false;
        }
        
        final MockId other = (MockId)o;
        
        return this.id.equals(other.id);
    }

    @Override
    public int compareTo(Id id) throws ClassCastException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

}
