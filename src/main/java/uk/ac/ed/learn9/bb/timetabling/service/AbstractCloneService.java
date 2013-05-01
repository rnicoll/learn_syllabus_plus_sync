package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Abstract service for cloning tables from one database to another. Intended
 * for copying data from the timetabling database, to the local copy used by the
 * synchronization service, but could be used for other tasks if desired.
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
     */
    public void cloneTable(final Connection source, final Connection destination,
            final String sourceTable, final String destinationTable,
            final String sourcePkField, final String destinationPkField,
            final Map<String, String> fieldMappings)
            throws SQLException {
        destination.setAutoCommit(false);
        try {
            final PreparedStatement destinationStatement = destination.prepareStatement(
                    buildQuery(destinationTable, destinationPkField, fieldMappings.values()),
                    ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            try {
                final PreparedStatement sourceStatement = source.prepareStatement(
                        buildQuery(sourceTable, sourcePkField, fieldMappings.keySet()),
                        ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                try {
                    cloneQuery(sourceStatement, destinationStatement,
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
     * @param sourceStatement the prepared statement to read data from. This
     * must be set up so it is scroll insensitive.
     * @param destinationStatement the prepared statement to write data into.
     * This must be set up to be updatable.
     * @param sourcePkField the name of the primary key field on the source
     * table. Tables with compound primary keys are not supported.
     * @param destinationPkField the name of the primary key field on the
     * destination table. Tables with compound primary keys are not supported.
     * @param fieldMappings a mapping from source field names to destination
     * fields, for non-primary key fields to be cloned.
     */
    public void cloneQuery(final PreparedStatement sourceStatement, final PreparedStatement destinationStatement,
            final String sourcePkField, final String destinationPkField,
            final Map<String, String> fieldMappings)
            throws SQLException {
        final ResultSet destinationRs = destinationStatement.executeQuery();
        try {
            final ResultSet sourceRs = destinationStatement.executeQuery();
            try {
                doClone(sourceRs, destinationRs, sourcePkField, destinationPkField, fieldMappings);
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
     * @param sourceRs the result set to read data from. This must be scroll
     * insensitive.
     * @param destinationRs the result set to write data into. This must be
     * updatable.
     * @param sourcePkField the name of the primary key field on the source
     * table. Tables with compound primary keys are not supported.
     * @param destinationPkField the name of the primary key field on the
     * destination table. Tables with compound primary keys are not supported.
     * @param fieldMappings a mapping from source field names to destination
     * fields, for non-primary key fields to be cloned.
     */
    private void doClone(final ResultSet sourceRs, final ResultSet destinationRs,
            final String sourcePkField, final String destinationPkField,
            final Map<String, String> fieldMappings)
            throws SQLException {
        String destinationPk = null;
        final Set<String> existingPks = new HashSet<String>();

        // First, update existing records, and note which records we have in the database
        while (sourceRs.next()) {
            final String sourcePk = sourceRs.getString(sourcePkField);

            // Check we have a destination primary key value before we start doing comparisons
            if (null == destinationPk) {
                if (!destinationRs.next()) {
                    // Out of existing entries, go to the writing stage.
                    break;
                }
                destinationPk = destinationRs.getString(destinationPkField);
            }

            while (destinationPk.compareTo(sourcePk) < 0) {
                // Records that don't exist any longer but we have old copies of; can be ignored
                if (!destinationRs.next()) {
                    // Out of existing entries, go to the writing stage.
                    break;
                }
                destinationPk = destinationRs.getString(destinationPkField);
            }

            // Check if we've found a match, or gone straight past the source key value
            if (destinationPk.equals(sourcePk)) {
                // Note that we have this record, and so don't need to insert it later.
                existingPks.add(sourcePk);

                boolean recordDirty = false;

                // Synchronize the data across - this only handles strings because that's fine here,
                // but we should handle other data types
                for (String sourceFieldName : fieldMappings.keySet()) {
                    final String destinationFieldName = fieldMappings.get(sourceFieldName);
                    final String sourceVal = sourceRs.getString(sourceFieldName);
                    final String destinationVal = destinationRs.getString(destinationFieldName);

                    if (sourceVal == destinationVal) {
                        // Handle both sides are null
                        continue;
                    }

                    if (null == sourceVal
                            || null == destinationVal
                            || !sourceVal.equals(destinationVal)) {
                        destinationRs.updateString(destinationFieldName, sourceVal);
                        recordDirty = true;
                    }
                }

                if (recordDirty) {
                    destinationRs.updateRow();
                }
            }
        }

        // Now insert new records
        sourceRs.first();
        destinationRs.moveToInsertRow();

        while (sourceRs.next()) {
            final String sourcePk = sourceRs.getString(sourcePkField);

            if (existingPks.contains(sourcePk)) {
                continue;
            }

            destinationRs.updateString(destinationPkField, sourcePk);
            for (String sourceFieldName : fieldMappings.keySet()) {
                final String destinationFieldName = fieldMappings.get(sourceFieldName);
                final String sourceVal = sourceRs.getString(sourceFieldName);
                destinationRs.updateString(destinationFieldName, sourceVal);
            }

            destinationRs.insertRow();
        }

        return;
    }

    /**
     * Builds a select statement against a single table.
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
