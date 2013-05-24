package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Abstract service for cloning tables from one database to another. Intended
 * for copying data from the timetabling database, to the local copy used by the
 * synchronisation service, but could be used for other tasks if desired.
 * 
 * Normally this would be called via the {@link #cloneTable(java.sql.Connection, java.sql.Connection, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Map)}
 * method; the other clone methods are intended for cases with more complex
 * queries.
 */
public abstract class AbstractCloneService extends Object {

    /**
     * Clones the data in a table (or view) from one database to another.
     *
     * @param source the database to be read from.
     * @param destination the database to be written into. The auto-commit
     * status on this database will be modified as part of the clone process.
     * @param sourceTable the name of the table to read records from.
     * @param destinationTable the name of the table to write records to.
     * @param sourcePkField the name of the primary key field on the source
     * table. Tables with compound primary keys are not supported.
     * @param destinationPkField the name of the primary key field on the
     * destination table. Tables with compound primary keys are not supported.
     * @param fieldMappings a mapping from source field names to destination
     * fields, for non-primary key fields to be cloned.
     * 
     * @throws SQLException if there was a problem accessing one of the
     * databases.
     */
    public void cloneTable(final Connection source, final Connection destination,
            final String sourceTable, final String destinationTable,
            final String sourcePkField, final String destinationPkField,
            final Map<String, String> fieldMappings)
            throws SQLException {
        destination.setAutoCommit(false);
        try {
            final PreparedStatement destinationStatement = destination.prepareStatement(
                    buildQuery(destinationTable, destinationPkField, fieldMappings.values()));
            try {
                final PreparedStatement sourceStatement = source.prepareStatement(
                        buildQuery(sourceTable, sourcePkField, fieldMappings.keySet()));
                try {
                    cloneQuery(destinationTable, sourceStatement, destinationStatement,
                            sourcePkField, destinationPkField,
                            fieldMappings);
                    destination.commit();
                } finally {
                    sourceStatement.close();
                }
            } finally {
                destinationStatement.close();
            }
        } finally {
            destination.rollback();
            destination.setAutoCommit(true);
        }
    }

    /**
     * Clones the data in from one query statement into another.
     *
     * @param destinationTable the name of the table to write records to.
     * @param sourceStatement the prepared statement to read data from.
     * @param destinationStatement the prepared statement to write data into.
     * @param sourcePkField the name of the primary key field on the source
     * table. Tables with compound primary keys are not supported.
     * @param destinationPkField the name of the primary key field on the
     * destination table. Tables with compound primary keys are not supported.
     * @param fieldMappings a mapping from source field names to destination
     * fields, for non-primary key fields to be cloned.
     * 
     * @throws SQLException if there was a problem accessing one of the
     * databases.
     */
    public void cloneQuery(final String destinationTable,
            final PreparedStatement sourceStatement, final PreparedStatement destinationStatement,
            final String sourcePkField, final String destinationPkField,
            final Map<String, String> fieldMappings)
            throws SQLException {
        final ResultSet destinationRs = destinationStatement.executeQuery();
        try {
            final ResultSet sourceRs = sourceStatement.executeQuery();
            try {
                cloneResultSet(destinationTable, sourceRs, destinationRs,
                    sourcePkField, destinationPkField, fieldMappings);
            } finally {
                sourceRs.close();
            }
        } finally {
            destinationRs.close();
        }
    }

    /**
     * Clones the data in from one result set into another.
     *
     * @param destinationTable the name of the table to write records to.
     * @param sourceRs the result set to read data from.
     * @param destinationRs the result set to write data into.
     * @param sourcePkField the name of the primary key field on the source
     * table. Tables with compound primary keys are not supported.
     * @param destinationPkField the name of the primary key field on the
     * destination table. Tables with compound primary keys are not supported.
     * @param fieldMappings a mapping from source field names to destination
     * fields, for non-primary key fields to be cloned.
     * 
     * @throws SQLException if there was a problem accessing one of the
     * databases.
     */
    public void cloneResultSet(final String destinationTable,
            final ResultSet sourceRs, final ResultSet destinationRs,
            final String sourcePkField, final String destinationPkField,
            final Map<String, String> fieldMappings)
            throws SQLException {
        final CloningStatements cloningStatements = new CloningStatements(
                destinationRs.getStatement().getConnection(),
                destinationTable, sourcePkField, destinationPkField,
                fieldMappings, destinationRs);

        // First, update existing records
        if (destinationRs.next()) {
            String destinationPk = destinationRs.getString(destinationPkField);
            
            while (sourceRs.next()) {
                final String sourcePk = sourceRs.getString(sourcePkField);

                // Continue onwards until we find a match
                if (destinationPk.compareTo(sourcePk) < 0) {
                    // We don't delete these records as they may have data attached
                    // to them.
                    
                    while (destinationRs.next()) {
                        destinationPk = destinationRs.getString(destinationPkField);
                        if (destinationPk.compareTo(sourcePk) >= 0) {
                            break;
                        }
                    }
                    if (destinationRs.isAfterLast()) {
                        // No match, insert and then jump to just inserting new records
                        cloningStatements.insert(sourceRs);
                        break;
                    }
                }

                // Check if we've found a match, or gone straight past the source key value
                if (destinationPk.equals(sourcePk)) {
                    // Synchronize the data across - this only handles strings because that's fine here,
                    // but we should handle other data types
                    for (String sourceFieldName : fieldMappings.keySet()) {
                        final String destinationFieldName = fieldMappings.get(sourceFieldName);
                        final String sourceVal = sourceRs.getString(sourceFieldName);
                        final String destinationVal = destinationRs.getString(destinationFieldName);

                        // Delibrately testing two objects being the same, not just
                        // equal. Equality tests come later.
                        if (sourceVal == destinationVal) {
                            // Handle both sides are null
                            continue;
                        }

                        if (null == sourceVal
                                || null == destinationVal
                                || !sourceVal.equals(destinationVal)) {
                            // There are differences, sync
                            cloningStatements.update(sourceRs);
                        }
                    }
                } else if (destinationPk.compareTo(sourcePk) > 0) {
                    cloningStatements.insert(sourceRs);
                }
            }
        }

        // Insert any remaining rows
        while (sourceRs.next()) {
            cloningStatements.insert(sourceRs);
        }
    }

    /**
     * Builds a select statement against a single table.
     * 
     * @param table the name of the table to access.
     * @param pkField the name of the primary key field in the table. Multiple/no
     * primary key fields are not supported.
     * @param otherFields a collection of the names of other fields to be accessed.
     * @return a select statement to select the named fields from the table.
     */
    public String buildQuery(final String table, final String pkField, final Collection<String> otherFields) {
        final StringBuilder query = new StringBuilder("SELECT ")
                .append(pkField);

        for (String fieldName : otherFields) {
            query.append(", ").append(fieldName);
        }

        query.append(" FROM ").append(table)
                .append(" ORDER BY ").append(pkField);

        return query.toString();
    }
}
