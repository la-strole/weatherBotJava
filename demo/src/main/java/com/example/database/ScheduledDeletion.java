package com.example.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The ScheduledDeletion class provides functionality to schedule a task that
 * deletes old rows from
 * a database table at a fixed interval. The class uses a
 * ScheduledExecutorService to periodically
 * execute a SQL DELETE statement that removes rows older than a specified time.
 * 
 * <p>
 * Usage example:
 * 
 * <pre>
 * {@code
 * ScheduledDeletion.run(15); // Schedules the task to run every 15 minutes
 * }
 * </pre>
 * 
 * <p>
 * Note: Ensure that the database URL is correctly set in the DB_URL variable.
 * 
 * <p>
 * Methods:
 * <ul>
 * <li>{@link #run(long)} - Schedules the deletion task to run at a fixed
 * interval.
 * </ul>
 */
public class ScheduledDeletion {

    private static final String DB_URL = Database.DATABASE_URL; // Update with your database path
    private static final String DELETE_OLD_ROWS_QUERY1 = "DELETE FROM multipleCities WHERE created_at < ?";
    private static final String DELETE_OLD_ROWS_QUERY2 = "DELETE FROM forecasts WHERE created_at < ?";

    private static final Logger logger = Logger.getLogger(ScheduledDeletion.class.getName());

    /**
     * Schedules a task to delete old rows from the database at a fixed interval.
     *
     * @param intervalInMinutes the interval in minutes at which the task should run
     */
    public static void run(long intervalInMinutes) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable deleteOldRowsTask = () -> {
            try (Connection connection = DriverManager.getConnection(DB_URL);
                    PreparedStatement preparedStatement = connection
                            .prepareStatement(DELETE_OLD_ROWS_QUERY1);
                    PreparedStatement preparedStatement2 = connection
                            .prepareStatement(DELETE_OLD_ROWS_QUERY2)) {
                preparedStatement.setString(1,
                        Instant.now().minusSeconds(intervalInMinutes * 60).toString());
                preparedStatement2.setString(1,
                        Instant.now().minusSeconds(intervalInMinutes * 60).toString());
                int rowsDeletedT1 = preparedStatement.executeUpdate();
                int rowsDeletedT2 = preparedStatement2.executeUpdate();

                logger.info(String.format("%d old rows deleted from t1, %d old rows deleted from t2",
                        rowsDeletedT1, rowsDeletedT2));
            } catch (SQLException e) {
                logger.severe(e.toString());
            }
        };

        // Schedule the task to run every 15 minutes
        scheduler.scheduleAtFixedRate(deleteOldRowsTask, 0, intervalInMinutes, TimeUnit.MINUTES);
    }
}
