package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Wrapper around a pair of an INSERT and an UPDATE statement, used to write
 * changes back to the database from a result set.
 */
class CloningStatements extends Object {
    private final PreparedStatement insertStatement;
    private final PreparedStatement updateStatement;
    private final String sourcePrimaryKeyField;
    private final String destinationPrimaryKeyField;
    private final SortedMap<String, String> fieldMappings = new TreeMap<String, String>();
    private final Map<String, Integer> fieldTypes = new HashMap<String, Integer>();

    public CloningStatements(final Connection database, final String setTable,
            final String setSourcePrimaryKeyField, final String setDestinationPrimaryKeyField,
            final Map<String, String> setFieldMappings, final ResultSet destinationRs)
                throws SQLException {
        this.sourcePrimaryKeyField = setSourcePrimaryKeyField;
        this.destinationPrimaryKeyField = setDestinationPrimaryKeyField;
        this.fieldMappings.putAll(setFieldMappings);
        
        this.insertStatement = database.prepareStatement(buildInsertStatement(setTable,
                setDestinationPrimaryKeyField, this.fieldMappings.values()));
        this.updateStatement = database.prepareStatement(buildUpdateStatement(setTable,
                setDestinationPrimaryKeyField, this.fieldMappings.values()));
        final ResultSetMetaData metadata = destinationRs.getMetaData();
        
        // Store column names as upper case so we're not at risk of case
        // sensitivity issues.
        final int columnCount = metadata.getColumnCount();
        for (int colIdx = 1; colIdx <= columnCount; colIdx++) {
            final String columnName = metadata.getColumnName(colIdx);
            this.fieldTypes.put(columnName.toUpperCase(), metadata.getColumnType(colIdx));
        }
    }
    
    private static String buildInsertStatement(final String table,
            final String destinationPrimaryKeyField,
            final Collection<String> orderedFields)
            throws SQLException {
        
        final StringBuilder query = new StringBuilder("INSERT INTO ")
                .append(table).append(" (")
                .append(destinationPrimaryKeyField);
        
        for (String otherField : orderedFields) {
            query.append(", ").append(otherField);
        }
        query.append(") VALUES (?");
        for (String unused : orderedFields) {
            query.append(", ?");
        }
        query.append(")");
        
        return query.toString();
    }
    
    private static String buildUpdateStatement(final String table,
            final String destinationPrimaryKeyField,
            final Collection<String> orderedFields)
            throws SQLException {
        
        if (orderedFields.isEmpty()) {
            throw new IllegalArgumentException("Ordered field set must not be empty, as it would mean there are no fields to update.");
        }
        
        final StringBuilder query = new StringBuilder("UPDATE ")
                .append(table).append(" SET ");
        boolean firstField = true;
        
        for (String otherField : orderedFields) {
            if (firstField) {
                firstField = false;
            } else {
                query.append(", ");
            }
            query.append(otherField).append("=?");
        }
        query.append(" WHERE ").append(destinationPrimaryKeyField).append("=?");
        
        return query.toString();
    }
            
    protected static void copyIntoStatement(final PreparedStatement statement, final ResultSet sourceRow,
        final int colIdx, final Map.Entry<String, String> entry, final int colType) throws SQLException {
        switch (colType) {
            case Types.INTEGER:
                statement.setInt(colIdx, sourceRow.getInt(entry.getKey()));
                break;
            case Types.CHAR:
            case Types.CLOB:
            case Types.VARCHAR:
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
    }

    public int insert(final ResultSet sourceRow) throws SQLException {
        final String sourcePk = sourceRow.getString(this.sourcePrimaryKeyField);
        int colIdx = 1;
        
        this.insertStatement.setString(colIdx++, sourcePk);
        for (Map.Entry<String, String> entry: this.fieldMappings.entrySet()) {
            final Integer colType = this.fieldTypes.get(entry.getValue().toUpperCase());
            
            if (null == colType) {
                throw new RuntimeException("Could not determine column type of column \""
                    + entry.getValue() + "\"; known fields are "
                    + this.fieldTypes);
            }
            
            copyIntoStatement(this.insertStatement, sourceRow, colIdx++,
                    entry, colType);
        }
        
        return this.insertStatement.executeUpdate();
    }

    public int update(final ResultSet sourceRow) throws SQLException {
        final String sourcePk = sourceRow.getString(this.sourcePrimaryKeyField);
        int colIdx = 1;
        for (Map.Entry<String, String> entry: this.fieldMappings.entrySet()) {
            final Integer colType = this.fieldTypes.get(entry.getValue().toUpperCase());
            
            if (null == colType) {
                throw new RuntimeException("Could not determine column type of column \""
                    + entry.getValue());
            }
            copyIntoStatement(this.updateStatement, sourceRow, colIdx++,
                    entry, colType);
        }
        this.updateStatement.setString(colIdx, sourcePk);
        return this.updateStatement.executeUpdate();
    }
    
}
