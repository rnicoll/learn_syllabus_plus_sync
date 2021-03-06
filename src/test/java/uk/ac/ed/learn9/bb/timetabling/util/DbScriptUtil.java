
package uk.ac.ed.learn9.bb.timetabling.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Runs a script of commands against a database, for example to assemble
 * a database schema.
 */
public class DbScriptUtil {
    /**
     * Runs a script file against a database. This attempts to fairly intelligently
     * cut individual commands from the file by looking for semicolons at the end
     * of a command, but does not claim to cover all possibilities and is intended
     * only for test cases.
     * 
     * @param connection a connection to the database to run the script against.
     * @param scriptFile the file to read SQL statements from.
     * @throws IOException if there was a problem reading from the script file.
     * @throws SQLException if there was a problem communicating with the database.
     */
    public static void runScript(Connection connection, File scriptFile)
        throws IOException, SQLException {
        final BufferedReader reader = new BufferedReader(new FileReader(scriptFile));
        StringBuilder buffer = new StringBuilder();
        final Statement statement = connection.createStatement();
        
        try {
            // Count the number of BEGIN/END keywords, used to wrap procedural
            // chunks that may include ";" characters without ending the statement.
            int beginCount = 0;
            int endCount = 0;
            
            for (String line = reader.readLine(); null != line; line = reader.readLine()) {
                final String trimmedLine = line.trim().toUpperCase();
                
                if (trimmedLine.length() == 0) {
                    continue;
                }
                
                buffer.append(line).append("\n");
                
                // Watch out for keywords that suggest the start of a procedural
                // chunk
                if (trimmedLine.contains("BEGIN ATOMIC")) {
                    beginCount++;
                } else if (beginCount > endCount
                    && trimmedLine.contains("END;")) {
                    endCount++;
                }
                
                if (trimmedLine.endsWith(";")
                    && (beginCount == endCount)) {
                    try {
                        statement.executeUpdate(buffer.toString());
                    } catch(SQLException e) {
                        throw new SQLException("Failed to execute SQL "
                            + buffer.toString(), e);
                    }
                    buffer = new StringBuilder();
                    beginCount = 0;
                    endCount = 0;
                }
            }
        } finally {
            statement.close();
        }
    }
    
}
