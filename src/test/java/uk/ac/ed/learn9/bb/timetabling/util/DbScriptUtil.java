
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

    public static void runScript(Connection connection, File scriptFile)
        throws IOException, SQLException {
        final BufferedReader reader = new BufferedReader(new FileReader(scriptFile));
        StringBuilder buffer = new StringBuilder();
        final Statement statement = connection.createStatement();
        
        try {
            for (String line = reader.readLine(); null != line; line = reader.readLine()) {
                final String trimmedLine = line.trim();
                
                if (trimmedLine.length() == 0) {
                    continue;
                }
                
                buffer.append(line).append("\n");
                
                if (trimmedLine.endsWith(";")) {
                    try {
                        statement.executeUpdate(buffer.toString());
                    } catch(SQLException e) {
                        throw new SQLException("Failed to execute SQL "
                            + buffer.toString(), e);
                    }
                    buffer = new StringBuilder();
                }
            }
        } finally {
            statement.close();
        }
    }
    
}
