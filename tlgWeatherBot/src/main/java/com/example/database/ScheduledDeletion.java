package com.example.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The ScheduledDeletion class provides a utility method to delete old rows from specific database tables
 * at a scheduled interval. It connects to the database and executes prepared statements to delete rows
 * older than the specified interval from the 'multipleCities', 'forecasts', and 'subscribes' tables.
 * The number of rows deleted from each table is logged. If an SQL exception occurs, it is logged.
 * 
 * <p>This class cannot be instantiated.</p>
 * 
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * ScheduledDeletion.run(60); // Deletes rows older than 60 minutes
 * }
 * </pre>
 */
public class ScheduledDeletion {

    private static final String DB_URL = Database.DATABASE_URL; // Update with your database path

    private static final String DELETE_OLD_ROWS_QUERY1 = "DELETE FROM multipleCities WHERE created_at < ?";
    private static final String DELETE_OLD_ROWS_QUERY2 = "DELETE FROM forecasts WHERE created_at < ?";
    private static final String DELETE_NULL_TIME_ROWS = "DELETE FROM subscribes WHERE time is NULL";
    private static final Logger logger = Logger.getLogger(ScheduledDeletion.class.getName());

/**
 * Runs the scheduled deletion of old rows from the database tables.
 *
 * @param intervalInMinutes the interval in minutes to determine which rows are considered old
 *                          and should be deleted.
 * 
 * This method connects to the database and executes three prepared statements to delete old rows
 * from the tables 'multipleCities', 'forecasts', and 'subscribes'. It logs the number of rows deleted
 * from each table. If an SQL exception occurs, it logs the exception.
 */
    public static void run(int intervalInMinutes) {
        try (Connection connection = DriverManager.getConnection(DB_URL);
                PreparedStatement preparedStatement = connection
                        .prepareStatement(DELETE_OLD_ROWS_QUERY1);
                PreparedStatement preparedStatement2 = connection
                        .prepareStatement(DELETE_OLD_ROWS_QUERY2);
                PreparedStatement preparedStatement3 = connection
                        .prepareStatement(DELETE_NULL_TIME_ROWS)) {
            preparedStatement.setString(1,
                    Instant.now().minusSeconds(intervalInMinutes).toString());
            preparedStatement2.setString(1,
                    Instant.now().minusSeconds(intervalInMinutes).toString());
            final int rowsDeletedT1 = preparedStatement.executeUpdate();
            final int rowsDeletedT2 = preparedStatement2.executeUpdate();
            final int rowsDeletedT3 = preparedStatement3.executeUpdate();

            logger.log(Level.INFO, () -> String.format(
                    "%d old rows deleted from multipleCities, " +
                            "%d old rows deleted from forecasts, " +
                            "%d old rows deleted from subscribes",
                    rowsDeletedT1, rowsDeletedT2, rowsDeletedT3));
        } catch (final SQLException e) {
            logger.log(Level.SEVERE, e::toString);
        }
    }

    
    // Private constructor to hide the implicit public one
    private ScheduledDeletion() {
        throw new IllegalStateException("This is a utility class and cannot be instantiated");
    }
}
