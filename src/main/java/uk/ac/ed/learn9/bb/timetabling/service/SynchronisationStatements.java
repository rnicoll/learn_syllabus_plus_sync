package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

/**
 * Wrapper around a pair of an INSERT and an UPDATE statement, used to write
 * changes back to the database from a result set.
 */
class SynchronisationStatements extends Object {
    private final PreparedStatement insertStatement;
    private final PreparedStatement updateStatement;
    private final SortedSet<String> sourcePrimaryKeyFields;
    private final SortedMap<String, String> fieldMappings = new TreeMap<String, String>();
    private final Map<String, Integer> fieldTypes = new HashMap<String, Integer>();

    /**
     * Constructs a pair of SQL statements:
     * 
     * 1. An INSERT statement for writing out new entries based on the contents
     * of the source database.
     * 2. An UPDATE statement for updating existing entries where they have
     * changed in the source database.
     * 
     * @param database the database to write changes into.
     * @param setTable the table to write changes into, within the database.
     * @param setSourcePrimaryKeyFields the names of the primary key fields in
     * the <b>source</b> table.
     * @param setFieldMappings a mapping from the names of fields in the source
     * database, to those in the destination database.
     * @param destinationRs a result set across all fields in the target table,
     * used to determine field types.
     * @throws SQLException if there is a problem working with the database.
     */
    public SynchronisationStatements(final Connection database, final String setTable,
            final SortedSet<String> setSourcePrimaryKeyFields,
            final Map<String, String> setFieldMappings, final ResultSet destinationRs)
                throws SQLException {
        final List<String> orderedFields = new ArrayList<String>();
    
        this.fieldMappings.putAll(setFieldMappings);
        orderedFields.addAll(this.fieldMappings.values());
        
        this.sourcePrimaryKeyFields = setSourcePrimaryKeyFields;
        
        this.insertStatement = database.prepareStatement(buildInsertStatement(setTable,
            orderedFields));
        this.updateStatement = database.prepareStatement(buildUpdateStatement(setTable,
            this.sourcePrimaryKeyFields, this.fieldMappings));
        final ResultSetMetaData metadata = destinationRs.getMetaData();
        
        // Store column names as upper case so we're not at risk of case
        // sensitivity issues.
        final int columnCount = metadata.getColumnCount();
        for (int colIdx = 1; colIdx <= columnCount; colIdx++) {
            final String columnName = metadata.getColumnName(colIdx);
            this.fieldTypes.put(columnName.toUpperCase(), metadata.getColumnType(colIdx));
        }
    }
    
    /**
     * Builds the INSERT statement to write into the destination database.
     * 
     * @param table the table to write into.
     * @param orderedFields an ordered list of fields on the destination table.
     * @return an SQL statement.
     */
    private static String buildInsertStatement(final String table,
            final List<String> orderedFields) {
        boolean firstField = true;
        final StringBuilder query = new StringBuilder("INSERT INTO ")
                .append(table).append(" (");
        final StringBuilder queryParameters = new StringBuilder();
        
        for (String fieldName : orderedFields) {
            if (firstField) {
                firstField = false;
            } else {
                query.append(", ");
                queryParameters.append(", ");
            }
            query.append(fieldName);
            queryParameters.append("?");
        }
        query.append(") VALUES (").append(queryParameters).append(")");
        
        return query.toString();
    }
    
    private static String buildUpdateStatement(final String table,
            final SortedSet<String> sourcePrimaryKeyFields,
            final SortedMap<String, String> fieldMappings) {
        assert !sourcePrimaryKeyFields.isEmpty();
        assert fieldMappings.size() > sourcePrimaryKeyFields.size();
        
        if (fieldMappings.isEmpty()) {
            throw new IllegalArgumentException("Ordered fields set must not be empty, as it would mean there are no fields to update.");
        }
        
        final StringBuilder query = new StringBuilder("UPDATE ")
                .append(table).append(" SET ");
        boolean firstField = true;
        
        for (Map.Entry<String, String> entry: fieldMappings.entrySet()) {
            if (sourcePrimaryKeyFields.contains(entry.getKey())) {
                continue;
            }
            
            if (firstField) {
                firstField = false;
            } else {
                query.append(", ");
            }
            query.append(entry.getValue()).append("=?");
        }
        query.append(" WHERE ");
        firstField = true;
        for (String sourcePrimaryKeyField: sourcePrimaryKeyFields) {
            if (firstField) {
                firstField = false;
            } else {
                query.append(" AND ");
            }
            query.append(fieldMappings.get(sourcePrimaryKeyField)).append("=?");
        }
        
        return query.toString();
    }
           
    /**
     * Copies a field from the given result set, in a prepared statement, ready
     * for use in an UPDATE/INSERT statement.
     * 
     * @param statement the statement to set the value on.
     * @param sourceRow the result set to pull data from.
     * @param colIdx the index within the statement of the column to set a value for.
     * @param entry a map entry for the source/destination column names.
     * @param colType the type (as in java.sql.Types) of the column.
     * @throws SQLException 
     */
    private static void copyIntoStatement(final PreparedStatement statement, final ResultSet sourceRow,
        final int colIdx, final Map.Entry<String, String> entry, final int colType,
        final boolean allowNull) throws SQLException {        
        switch (colType) {
            case Types.INTEGER:
                statement.setInt(colIdx, sourceRow.getInt(entry.getKey()));
                break;
            case Types.CHAR:
            case Types.CLOB:
            case Types.VARCHAR:
            case Types.NCHAR:
            case Types.NCLOB:
            case Types.NVARCHAR:
            case Types.NUMERIC: // Okay, that's not ideal, sorry...
                statement.setString(colIdx, sourceRow.getString(entry.getKey()));
                break;
            case Types.FLOAT:
                statement.setFloat(colIdx, sourceRow.getFloat(entry.getKey()));
                break;
            case Types.DOUBLE:
                statement.setDouble(colIdx, sourceRow.getDouble(entry.getKey()));
                break;
            default:
                throw new UnsupportedOperationException("Unsure how to copy column \""
                    + entry.getKey() + "\" of type #" + colType);
        }
        
        // Set it to null if the value retrieved was null.
        if (sourceRow.wasNull()) {
            if (!allowNull) {
                throw new SQLException("Column \""
                    + entry.getKey() + "\" must not be null, but found value was null.");
            }
            
            statement.setNull(colIdx, colType);
        }
    }

    /**
     * Insert a row into the destination database based on data in the current
     * row of the source result set.
     * 
     * @param sourceRow the row to read data from.
     * @return the number of rows insert (should always be 1).
     * @throws SQLException if anything went wrong with the insertion.
     */
    public int insert(final ResultSet sourceRow) throws SQLException {
        int colIdx = 1;
        
        for (Map.Entry<String, String> entry: this.fieldMappings.entrySet()) {            
            final Integer colType = this.fieldTypes.get(entry.getValue().toUpperCase());
            
            if (null == colType) {
                throw new RuntimeException("Could not determine column type of column \""
                    + entry.getValue() + "\"; known fields are "
                    + this.fieldTypes);
            }
            
            copyIntoStatement(this.insertStatement, sourceRow, colIdx++,
                    entry, colType, !this.sourcePrimaryKeyFields.contains(entry.getKey()));
        }
        
        return this.insertStatement.executeUpdate();
    }

    public int update(final ResultSet sourceRow) throws SQLException {
        int colIdx = 1;
        for (Map.Entry<String, String> entry: this.fieldMappings.entrySet()) {
            if (this.sourcePrimaryKeyFields.contains(entry.getKey())) {
                continue;
            }
            final Integer colType = this.fieldTypes.get(entry.getValue().toUpperCase());
            
            if (null == colType) {
                throw new RuntimeException("Could not determine column type of column \""
                    + entry.getValue());
            }
            copyIntoStatement(this.updateStatement, sourceRow, colIdx++,
                    entry, colType, !this.sourcePrimaryKeyFields.contains(entry.getKey()));
        }
        for (String sourcePrimaryKeyField: this.sourcePrimaryKeyFields) {
            final String sourcePk = sourceRow.getString(sourcePrimaryKeyField);
            this.updateStatement.setString(colIdx++, sourcePk);
        }
        
        return this.updateStatement.executeUpdate();
    }
    
}
