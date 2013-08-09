package uk.ac.ed.learn9.bb.timetabling.blackboard;

import blackboard.data.course.Group;
import blackboard.persist.PersistenceException;

/**
 * Mock group which doesn't back on a Learn instance, so all getters have
 * matching setters.
 */
public class MockGroup extends Group {
    private Boolean groupToolWithGradeableItem;
    
    @Override
    public boolean hasGroupToolWithGradeableItem() throws PersistenceException {
        if (null == this.groupToolWithGradeableItem) {
            return super.hasGroupToolWithGradeableItem();
        }
        return this.groupToolWithGradeableItem;
    }
    
    /**
     * Overrides the normal value for whether this group has a group tool
     * with gradeable item(s).
     */
    public void setHasGroupToolWithGradeableItem(final boolean newValue) {
        this.groupToolWithGradeableItem = newValue;
    }

}
