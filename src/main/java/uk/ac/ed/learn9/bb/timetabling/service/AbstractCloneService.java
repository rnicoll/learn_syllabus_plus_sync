package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

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
     * Enum to describe which parts of a synchronisation should be done.
     */
    public enum Mode {
        INSERT_UPDATE(true, true),
        INSERT_ONLY(true,false),
        UPDATE_ONLY(false,true),
        DRY_RUN(false,false);
        
        private boolean doInsert;
        private boolean doUpdate;
        
                Mode(final boolean setInsert, final boolean setUpdate) {
            this.doInsert = setInsert;
            this.doUpdate = setUpdate;
        }
                
        public boolean getInsert() {
            return this.doInsert;
        }
                
        public boolean getUpdate() {
            return this.doUpdate;
        }
    }

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
     * @param fieldMappings a mapping from source field names to destination
     * fields.
     * 
     * @throws SQLException if there was a problem accessing one of the
     * databases.
     */
    public void cloneTable(final Connection source, final Connection destination,
            final String sourceTable, final String destinationTable,
            final String sourcePkField, final Map<String, String> fieldMappings)
            throws SQLException {
        final String destinationPkField = fieldMappings.get(sourcePkField);
        final SortedSet<String> destinationPkFields = new TreeSet<String>(){{
            add(destinationPkField);
        }};
        final SortedSet<String> sourcePkFields = new TreeSet<String>(){{
            add(sourcePkField);
        }};
        
        destination.setAutoCommit(false);
        try {
            final PreparedStatement destinationStatement = destination.prepareStatement(
                    buildQuery(destinationTable, destinationPkFields, fieldMappings.values()));
            try {
                final PreparedStatement sourceStatement = source.prepareStatement(
                        buildQuery(sourceTable, sourcePkFields, fieldMappings.keySet()));
                try {
                    cloneQuery(destinationTable, sourceStatement, destinationStatement,
                            sourcePkField, fieldMappings);
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
     * Clones the data in a table (or view) from one database to another.
     *
     * @param source the database to be read from.
     * @param destination the database to be written into. The auto-commit
     * status on this database will be modified as part of the clone process.
     * @param sourceTable the name of the table to read records from.
     * @param destinationTable the name of the table to write records to.
     * @param sourcePkField the name of the primary key field on the source
     * table. Tables with compound primary keys are not supported.
     * @param fieldMappings a mapping from source field names to destination
     * fields.
     * 
     * @throws SQLException if there was a problem accessing one of the
     * databases.
     */
    public void cloneTable(final Connection source, final Connection destination,
            final String sourceTable, final String destinationTable,
            final SortedSet<String> sourcePkFields, final Map<String, String> fieldMappings)
            throws SQLException {
        final SortedSet<String> destinationPkFields =  new TreeSet<String>();
        
        for (String sourcePkField: sourcePkFields) {
            destinationPkFields.add(fieldMappings.get(sourcePkField));
        }
        
        destination.setAutoCommit(false);
        try {
            final PreparedStatement destinationStatement = destination.prepareStatement(
                    buildQuery(destinationTable, destinationPkFields, fieldMappings.values()));
            try {
                final PreparedStatement sourceStatement = source.prepareStatement(
                        buildQuery(sourceTable, sourcePkFields, fieldMappings.keySet()));
                try {
                    cloneQuery(destinationTable, sourceStatement, destinationStatement,
                            sourcePkFields, fieldMappings);
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
     * table.
     * @param fieldMappings a mapping from source field names to destination
     * fields.
     * 
     * @throws SQLException if there was a problem accessing one of the
     * databases.
     */
    public void cloneQuery(final String destinationTable,
            final PreparedStatement sourceStatement, final PreparedStatement destinationStatement,
            final String sourcePkField, final Map<String, String> fieldMappings)
            throws SQLException {
        final ResultSet destinationRs = destinationStatement.executeQuery();
        try {
            final ResultSet sourceRs = sourceStatement.executeQuery();
            try {
                cloneResultSet(destinationTable, sourceRs, destinationRs,
                    sourcePkField, fieldMappings, Mode.INSERT_UPDATE);
            } finally {
                sourceRs.close();
            }
        } finally {
            destinationRs.close();
        }
    }

    /**
     * Clones the data in from one query statement into another.
     *
     * @param destinationTable the name of the table to write records to.
     * @param sourceStatement the prepared statement to read data from.
     * @param destinationStatement the prepared statement to write data into.
     * @param sourcePkFields the name of the primary key fields on the source
     * table.
     * @param fieldMappings a mapping from source field names to destination
     * fields.
     * 
     * @throws SQLException if there was a problem accessing one of the
     * databases.
     */
    public void cloneQuery(final String destinationTable,
            final PreparedStatement sourceStatement, final PreparedStatement destinationStatement,
            final SortedSet<String> sourcePkFields, final Map<String, String> fieldMappings)
            throws SQLException {
        final ResultSet destinationRs = destinationStatement.executeQuery();
        try {
            final ResultSet sourceRs = sourceStatement.executeQuery();
            try {
                cloneResultSet(destinationTable, sourceRs, destinationRs,
                    sourcePkFields, fieldMappings, Mode.INSERT_UPDATE);
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
     * table.
     * @param fieldMappings a mapping from source field names to destination
     * fields.
     * 
     * @throws SQLException if there was a problem accessing one of the
     * databases.
     */
    public void cloneResultSet(final String destinationTable,
            final ResultSet sourceRs, final ResultSet destinationRs,
            final String sourcePkField,
            final Map<String, String> fieldMappings, final Mode mode)
            throws SQLException {
        final SortedSet<String> sourcePkFields = new TreeSet<String>(){{
            add(sourcePkField);
        }};
        this.cloneResultSet(destinationTable, sourceRs, destinationRs, sourcePkFields, fieldMappings,
            mode);
    }

    /**
     * Clones the data in from one result set into another.
     *
     * @param destinationTable the name of the table to write records to.
     * @param sourceRs the result set to read data from.
     * @param destinationRs the result set to write data into.
     * @param sourcePkFields the name of the primary key fields on the source
     * table.
     * @param fieldMappings a mapping from source field names to destination
     * fields.
     * 
     * @throws SQLException if there was a problem accessing one of the
     * databases.
     */
    public void cloneResultSet(final String destinationTable, final ResultSet sourceRs, final ResultSet destinationRs,
            final SortedSet<String> sourcePkFields, final Map<String, String> fieldMappings,
            final Mode mode)
            throws SQLException {        
        final CloningStatements cloningStatements = new CloningStatements(
                destinationRs.getStatement().getConnection(),
                destinationTable, sourcePkFields,
                fieldMappings, destinationRs);

        // First, update existing records
        if (destinationRs.next()) {
            PrimaryKey destinationPk = this.buildDestinationPk(destinationRs, sourcePkFields, fieldMappings);
            
            while (sourceRs.next()) {
                final PrimaryKey sourcePk = this.buildSourcePk(sourceRs, sourcePkFields);

                // Continue onwards until we find a match
                if (destinationPk.compareTo(sourcePk) < 0) {
                    // We don't delete these records as they may have data attached
                    // to them.
                    
                    while (destinationRs.next()) {
                        destinationPk = this.buildDestinationPk(destinationRs, sourcePkFields, fieldMappings);
                        if (destinationPk.compareTo(sourcePk) >= 0) {
                            break;
                        }
                    }
                    if (destinationRs.isAfterLast()) {
                        // No match, insert and then jump to just inserting new records
                        if (mode.getInsert()) {
                            cloningStatements.insert(sourceRs);
                        }
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
                            
                            if (mode.getUpdate()) {
                                cloningStatements.update(sourceRs);
                            }
                        }
                    }
                } else if (destinationPk.compareTo(sourcePk) > 0) {
                    if (mode.getInsert()) {
                        cloningStatements.insert(sourceRs);
                    }
                }
            }
        }

        // Insert any remaining rows
        if (mode.getInsert()) {
            while (sourceRs.next()) {
                cloningStatements.insert(sourceRs);
            }
        }
    }

    /**
     * Builds a select statement against a single table.
     * 
     * @param table the name of the table to access.
     * @param pkFields the name of the primary key fields in the table.
     * @param fields a collection of the names of all fields to be accessed.
     * @return a select statement to select the named fields from the table.
     */
    public String buildQuery(final String table, final SortedSet<String> pkFields,
            final Collection<String> fields) {
        assert !pkFields.isEmpty();
        assert fields.size() >= pkFields.size();
        
        boolean firstField = true;
        final StringBuilder query = new StringBuilder("SELECT ");

        for (String fieldName : fields) {
            assert null != fieldName;
            if (firstField) {
                firstField = false;
            } else {
                query.append(", ");
            }
            query.append(fieldName);
        }

        query.append(" FROM ").append(table)
                .append(" ORDER BY ");
        firstField = true;
        for (String pkFieldName: pkFields) {
            assert null != pkFieldName;
            if (firstField) {
                firstField = false;
            } else {
                query.append(", ");
            }
            query.append(pkFieldName);
        }

        return query.toString();
    }
    
    /**
     * Builds the primary key from a row taken from the table that data is being
     * written into.
     * 
     * @param rs the result set containing the row of data to build a primary key from.
     * @param sourceFields the primary key fields in the source table.
     * @param fieldMappings a mapping from fields in the source table, to those
     * in the destination table. Used to convert the source fields list into
     * destination fields.
     * @return the extracted primary key.
     * @throws SQLException if there was a problem reading data from the result
     * set.
     */
    private PrimaryKey buildDestinationPk(final ResultSet rs, final SortedSet<String> sourceFields,
            final Map<String, String> fieldMappings) throws SQLException {
        final String[] components = new String[sourceFields.size()];

        int componentIdx = 0;
        for (String sourceField: sourceFields) {
            components[componentIdx++] = rs.getString(fieldMappings.get(sourceField));
        }

        return new PrimaryKey(components);
    }
    
    /**
     * Builds the primary key from a row taken from the table that data is being
     * read from.
     * 
     * @param rs the result set containing the row of data to build a primary key from.
     * @param sourceFields the primary key fields in the source table.
     * @return the extracted primary key.
     * @throws SQLException if there was a problem reading data from the result
     * set.
     */
    private PrimaryKey buildSourcePk(final ResultSet rs, final SortedSet<String> sourceFields)
            throws SQLException {
        final String[] components = new String[sourceFields.size()];

        int componentIdx = 0;
        for (String sourceField: sourceFields) {
            components[componentIdx++] = rs.getString(sourceField);
        }

        return new PrimaryKey(components);
    }
    
    /**
     * Generic class for storing a primary key extracted from a database
     * table row.
     */
    public class PrimaryKey extends Object implements Comparable<PrimaryKey> {
        private final String[] components;
        
        public          PrimaryKey(final String[] setComponents) {
            this.components = setComponents;
        }
        
        @Override
        public int compareTo(final PrimaryKey other) {
            for (int componentIdx = 0; componentIdx < this.components.length; componentIdx++) {
                int res = this.components[componentIdx].compareTo(other.components[componentIdx]);
                
                if (res != 0) {
                    return res;
                }
            }
            
            return 0;
        }
        
        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof PrimaryKey)) {
                return false;
            }
            
            final PrimaryKey other = (PrimaryKey)o;
            for (int componentIdx = 0; componentIdx < this.components.length; componentIdx++) {
                int res = this.components[componentIdx].compareTo(other.components[componentIdx]);
                
                if (res != 0) {
                    return false;
                }
            }
            
            return true;
        }
        
        @Override
        public int hashCode() {
            int hash = 1;
            
            for (int componentIdx = 0; componentIdx < this.components.length; componentIdx++) {
                hash = hash * 31 + this.components[componentIdx].hashCode();
            }
            
            return hash;
        }
    }
}
