package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Wrapper around prepared statements for setting the outcome of a change
 * to be applied to Learn.
 * 
 * @see BlackboardService#applyPreviouslyFailedEnrolmentChanges(java.sql.Connection) 
 * @see BlackboardService#applyEnrolmentChanges(java.sql.Connection, uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun) 
 */
class ChangeOutcomeUpdateStatement {
    public enum Result {
        SUCCESS,
        COURSE_MISSING,
        GROUP_MISSING,
        CANNOT_REMOVE_SAFELY,
        STUDENT_MISSING,
        NOT_ON_COURSE,
        ALREADY_REMOVED,
        ALREADY_IN_GROUP;
    };
    
    private final Timestamp now = new Timestamp(System.currentTimeMillis());
    private final PreparedStatement statement;
    
    public              ChangeOutcomeUpdateStatement(final Connection connection)
            throws SQLException {
        this.statement = connection.prepareStatement(
            "UPDATE enrolment_change_part "
                + "SET update_completed=?, "
                    + "result_code=? "
                + "WHERE part_id=? "
                    + "AND update_completed IS NULL");
    }
    
    public void close() throws SQLException {
        this.statement.close();
    }
    
    public boolean markAlreadyRemoved(final int changeId)
            throws SQLException {
        return this.update(null, Result.ALREADY_REMOVED, changeId);
    }
    
    public boolean markAlreadyInGroup(final int changeId)
            throws SQLException {
        return this.update(null, Result.ALREADY_IN_GROUP, changeId);
    }
    
    public boolean markCourseMissing(final int changeId)
            throws SQLException {
        return this.update(null, Result.COURSE_MISSING, changeId);
    }
    
    public boolean markGroupMissing(final int changeId)
            throws SQLException {
        return this.update(null, Result.GROUP_MISSING, changeId);
    }
    
    public boolean markRemoveUnsafe(final int changeId)
            throws SQLException {
        return this.update(null, Result.CANNOT_REMOVE_SAFELY, changeId);
    }
    
    public boolean markNotOnCourse(final int changeId)
            throws SQLException {
        return this.update(null, Result.NOT_ON_COURSE, changeId);
    }

    public boolean markStudentMissing(int changeId)
            throws SQLException {
        return this.update(null, Result.STUDENT_MISSING, changeId);
    }
    
    public boolean markSuccess(final int changeId)
            throws SQLException {
        return this.update(this.now, Result.SUCCESS, changeId);
    }

    /**
     * Write out the result for a change part.
     * 
     * @param now the timestamp to mark the result as being at.
     * @param result the result to write out.
     * @param partId the ID of the change part to update.
     * @return true if successfully written, false otherwise.
     * @throws SQLException if there was a problem writing out the change.
     */
    private boolean update(final Timestamp now, final Result result, final int partId)
            throws SQLException {
        int paramIdx = 1;
        
        this.statement.setTimestamp(paramIdx++, now);
        this.statement.setString(paramIdx++, result.name());
        this.statement.setInt(paramIdx++, partId);
        return this.statement.executeUpdate() > 0;
    }
}
