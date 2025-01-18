package com.example.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.DataValidation;
import com.example.exceptions.AppErrorCheckedException;
import com.example.exceptions.AppErrorException;

public class Database {
    public static final String DATABASE_URL = "jdbc:sqlite:database.db";

    private static final Logger logger = Logger.getLogger(Database.class.getName());
    private static final String RUNTIME_ERROR = "Runtime Error.";

    /**
     * Creates the necessary tables in the database if they do not already exist.
     * 
     * The tables created are:
     * - multipleCities: Stores information about multiple cities with columns for
     * id, chatId, msgId, coordinates, and created_at.
     * - forecasts: Stores weather forecasts with columns for id, chatId, msgId,
     * forecast, and created_at.
     * - subscribes: Stores subscription information with columns for id, chatId,
     * lon, lat, cityName, time, and created_at.
     * - fullForecast: Stores full forecast information with columns for id, chatId,
     * isFullForecast, and created_at.
     * 
     * The method also sets the SQLite auto_vacuum pragma to FULL.
     * 
     * @throws AppErrorException if there is an error creating the tables.
     */
    public static void createTable() {

        final String createTable1SQL = "CREATE TABLE IF NOT EXISTS multipleCities ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "chatId INT NOT NULL," + "msgId INT NOT NULL,"
                + "coordinates TEXT NOT NULL," + "created_at TEXT NOT NULL" + ")";
        final String createTable2SQL = "CREATE TABLE IF NOT EXISTS forecasts (" + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "chatId INT NOT NULL," + "msgId INT NOT NULL," + "forecast TEXT NOT NULL,"
                + "created_at TEXT NOT NULL" + ")";
        final String createTable3SQL = "CREATE TABLE IF NOT EXISTS subscribes ("
                + "id INT AUTO_INCREMENT PRIMARY KEY," + "chatId INT NOT NULL,"
                + "lon REAL NOT NULL," + "lat REAL NOT NULL," + "cityName TEXT NOT NULL,"
                + "time TEXT," + "created_at TEXT NOT NULL," + "UNIQUE(lon,lat,time)" + ")";
        final String createTable4SQL = "CREATE TABLE IF NOT EXISTS fullForecast ("
                + "id INT AUTO_INCREMENT PRIMARY KEY," + "chatId INT NOT NULL,"
                + "isFullForecast INT NOT NULL," + "created_at TEXT NOT NULL" + ")";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                PreparedStatement pragmaAutovacuum = conn.prepareStatement("PRAGMA auto_vacuum = FULL");
                PreparedStatement createTable1Stmt = conn.prepareStatement(createTable1SQL);
                PreparedStatement createTable2Stmt = conn.prepareStatement(createTable2SQL);
                PreparedStatement createTable3Stmt = conn.prepareStatement(createTable3SQL);
                PreparedStatement createTable4Stmt = conn.prepareStatement(createTable4SQL)) {
            pragmaAutovacuum.execute();
            createTable1Stmt.executeUpdate();
            createTable2Stmt.executeUpdate();
            createTable3Stmt.executeUpdate();
            createTable4Stmt.executeUpdate();
            logger.log(Level.INFO, "Tables created successfully");
        } catch (final SQLException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorException("Can not create table.");
        }
    }

    /**
     * Inserts or updates city coordinates for a given chat ID in the database.
     * 
     * @param chatId            The chat ID associated with the cities.
     * @param msgId             The message ID associated with the cities.
     * @param citiesCoordinates A JSONArray containing the coordinates of the
     *                          cities.
     * @throws AppErrorCheckedException If a database error occurs.
     */
    public static void insertCoordinates(final long chatId, final long msgId, final JSONArray citiesCoordinates)
            throws AppErrorCheckedException {

        // Check if the coordinates are already in the database.
        if (!ifChatIdInDb(chatId, "multipleCities")) {
            final String insertSQL = "INSERT INTO multipleCities (chatId, msgId, coordinates, created_at) VALUES (?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                    PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                insertStmt.setLong(1, chatId);
                insertStmt.setLong(2, msgId);
                insertStmt.setString(3, citiesCoordinates.toString());
                insertStmt.setString(4, Instant.now().toString());
                insertStmt.executeUpdate();
            } catch (final SQLException e) {
                logger.log(Level.SEVERE, e::toString);
                throw new AppErrorCheckedException(RUNTIME_ERROR);
            }
        } else {
            final String updateSQL = "UPDATE multipleCities SET coordinates = ? , msgId = ? , created_at = ?  WHERE chatId = ?";
            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                    PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
                updateStmt.setString(1, citiesCoordinates.toString());
                updateStmt.setLong(2, msgId);
                updateStmt.setString(3, Instant.now().toString());
                updateStmt.setLong(4, chatId);
                updateStmt.executeUpdate();
            } catch (final SQLException e) {
                logger.log(Level.SEVERE, e::toString);
                throw new AppErrorCheckedException(RUNTIME_ERROR);
            }
        }
    }

    /**
     * Retrieves the coordinates associated with a specific chat ID and message ID
     * from the database.
     *
     * @param chatId the ID of the chat to retrieve coordinates for
     * @param msgId  the ID of the message to retrieve coordinates for
     * @return a JSONArray containing the coordinates if found, otherwise an empty
     *         JSONArray
     * @throws AppErrorCheckedException if a database access error occurs
     */
    public static JSONArray getCoordinates(final long chatId, final long msgId)
            throws AppErrorCheckedException {
        final String selectSQL = "SELECT coordinates FROM multipleCities WHERE chatId = ? AND msgId = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                PreparedStatement selectStmt = conn.prepareStatement(selectSQL)) {
            selectStmt.setLong(1, chatId);
            selectStmt.setLong(2, msgId);
            final ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                return new JSONArray(rs.getString("coordinates"));

            } else {
                return new JSONArray();
            }
        } catch (final SQLException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }

    /**
     * Inserts or updates a forecast in the database for a given chat ID.
     * 
     * <p>
     * If the chat ID does not exist in the database, a new record is inserted.
     * Otherwise, the existing record is updated with the new forecast data.
     * </p>
     * 
     * @param chatId   The ID of the chat for which the forecast is being stored.
     * @param msgId    The ID of the message associated with the forecast.
     * @param forecast The forecast data in JSON format.
     * @throws AppErrorCheckedException If a database access error occurs.
     */
    public static void insertForecast(final long chatId, final long msgId, final JSONArray forecast)
            throws AppErrorCheckedException {

        // Check if the coordinates are already in the database
        if (!ifChatIdInDb(chatId, "forecasts")) {
            final String insertSQL = "INSERT INTO forecasts (chatId, msgId, forecast, created_at) VALUES (?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                    PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                insertStmt.setLong(1, chatId);
                insertStmt.setLong(2, msgId);
                insertStmt.setString(3, forecast.toString());
                insertStmt.setString(4, Instant.now().toString());
                insertStmt.executeUpdate();
            } catch (final SQLException e2) {
                logger.log(Level.SEVERE, e2::toString);
                throw new AppErrorCheckedException(RUNTIME_ERROR);
            }
        } else {
            final String updateSQL = "UPDATE forecasts SET forecast = ? , msgId = ? , created_at = ?  WHERE chatId = ?";
            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                    PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
                updateStmt.setString(1, forecast.toString());
                updateStmt.setLong(2, msgId);
                updateStmt.setString(3, Instant.now().toString());
                updateStmt.setLong(4, chatId);
                updateStmt.executeUpdate();
            } catch (final SQLException e) {
                logger.log(Level.SEVERE, e::toString);
                throw new AppErrorCheckedException(RUNTIME_ERROR);
            }
        }
    }

    /**
     * Retrieves the weather forecast for a specific chat and message ID from the
     * database.
     *
     * @param chatId the ID of the chat for which the forecast is being retrieved
     * @param msgId  the ID of the message for which the forecast is being retrieved
     * @return a JSONObject containing the forecast data, or an empty JSONObject if
     *         no forecast is found
     * @throws AppErrorCheckedException if a database access error or JSON parsing
     *                                  error occurs
     */
    public static JSONArray getForecast(final long chatId, final long msgId) throws AppErrorCheckedException {
        final String selectSQL = "SELECT forecast FROM forecasts WHERE chatId = ? AND msgId = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                PreparedStatement selectStmt = conn.prepareStatement(selectSQL)) {
            selectStmt.setLong(1, chatId);
            selectStmt.setLong(2, msgId);
            final ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                return new JSONArray(rs.getString("forecast"));
            } else {
                return new JSONArray();
            }
        } catch (SQLException | JSONException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }

    public static void addSubscriptionCity(final long chatId, final Double lon, final Double lat, String cityName)
            throws AppErrorCheckedException {

        final String insertSQL = "INSERT INTO subscribes (chatId, cityName, lon, lat, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
            if (!DataValidation.isCityNameValid(cityName) || !DataValidation.isLongitudeValid(lon)
                    || !DataValidation.isLatitudeValid(lat)) {
                logger.log(Level.SEVERE,
                        () -> String.format("Invalid value in city name=%s, lon=%f, lat=%f", cityName, lon, lat));
                throw new AppErrorCheckedException(RUNTIME_ERROR);
            }
            insertStmt.setLong(1, chatId);
            insertStmt.setString(2, cityName);
            insertStmt.setDouble(3, lon);
            insertStmt.setDouble(4, lat);
            insertStmt.setString(5, Instant.now().toString());
            insertStmt.executeUpdate();
        } catch (final SQLException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }

    }

    public static void addSubscriptionTime(final long chatId, final double lon, final double lat, final LocalTime time)
            throws AppErrorCheckedException {

        final String insertSQL = "UPDATE subscribes SET time = ?, created_at = ? WHERE chatID = ? AND lon = ? AND lat = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                PreparedStatement updateStmt = conn.prepareStatement(insertSQL)) {
            updateStmt.setString(1, time.toString());
            updateStmt.setString(2, Instant.now().toString());
            updateStmt.setLong(3, chatId);
            updateStmt.setDouble(4, lon);
            updateStmt.setDouble(5, lat);
            updateStmt.executeUpdate();
        } catch (final SQLException e) {
            logger.setLevel(Level.FINE);
            logger.log(Level.FINE, String.format("lon=%f lat=%f time=%s", lon, lat, time));
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }

    /**
     * Retrieves the subscription details for a specific chat ID from the
     * database.
     *
     * @param chatId the ID of the chat for which to retrieve subscription details
     * @return a JSONArray containing the subscription details if found, otherwise
     *         an empty JSONArray. Each object in the array represents a
     *         subscription and contains the following fields:
     *         - "cityName": the name of the city
     *         - "lon": the longitude of the city
     *         - "lat": the latitude of the city
     *         - "time": the time of the subscription (if any)
     * @throws AppErrorCheckedException if a database access error or JSON parsing
     *                                  error occurs
     */
    public static JSONArray getSubscription(final long chatId) throws AppErrorCheckedException {
        final String selectSQL = "SELECT cityName, lon, lat, time FROM subscribes WHERE chatId = ? AND time IS NOT NULL";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                PreparedStatement selectStmt = conn.prepareStatement(selectSQL)) {
            selectStmt.setLong(1, chatId);
            final ResultSet rs = selectStmt.executeQuery();
            final JSONArray result = new JSONArray();
            while (rs.next()) {
                final JSONObject obj = new JSONObject();
                obj.put("cityName", rs.getString("cityName"));
                obj.put("lon", rs.getDouble("lon"));

                obj.put("lat", rs.getDouble("lat"));
                obj.put("time", rs.getString("time"));
                result.put(obj);
            }
            return result;
        } catch (SQLException | JSONException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }

    /**
     * Cancels a subscription for a specific chat ID, longitude, and latitude.
     *
     * <p>
     * This function deletes the subscription record from the 'subscribes' table
     * in the database where the chat ID, longitude, and latitude match the
     * provided parameters.
     * </p>
     *
     * @param chatId the ID of the chat for which to cancel the subscription
     * @param lon    the longitude of the city for which to cancel the
     *               subscription
     * @param lat    the latitude of the city for which to cancel the
     *               subscription
     * @throws AppErrorCheckedException if a database access error occurs
     */
    public static void cancelSubscription(long chatId, double lon, double lat, LocalTime time)
            throws AppErrorCheckedException {
        final String updateSQL = "DELETE FROM subscribes WHERE chatId = ? AND lon = ? AND lat = ? AND time = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
            updateStmt.setLong(1, chatId);
            updateStmt.setDouble(2, lon);
            updateStmt.setDouble(3, lat);
            updateStmt.setString(4, time.toString());
            updateStmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }

    /**
     * Inserts or updates the full forecast status for a given chat ID in the
     * database.
     * 
     * If the chat ID does not exist in the database, a new record is inserted.
     * If the chat ID already exists, the existing record is updated.
     * 
     * @param chatId the ID of the chat to insert or update
     * @param value  the full forecast status to set
     * @throws AppErrorCheckedException if a database error occurs
     */
    public static void insertIsFullForecast(final long chatId, final boolean value) throws AppErrorCheckedException {

        // Check if the chatId is already in the database
        if (!ifChatIdInDb(chatId, "fullForecast")) {
            final String insertSQL = "INSERT INTO fullForecast (chatId, isFullForecast, created_at) VALUES (?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                    PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                insertStmt.setLong(1, chatId);
                insertStmt.setBoolean(2, value);
                insertStmt.setString(3, Instant.now().toString());
                insertStmt.executeUpdate();
            } catch (final SQLException e2) {
                logger.log(Level.SEVERE, e2::toString);
                throw new AppErrorCheckedException(RUNTIME_ERROR);
            }
        } else {
            final String updateSQL = "UPDATE fullForecast SET isFullForecast = ? , created_at = ?  WHERE chatId = ?";
            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                    PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
                updateStmt.setBoolean(1, value);
                updateStmt.setString(2, Instant.now().toString());
                updateStmt.setLong(3, chatId);
                updateStmt.executeUpdate();
            } catch (final SQLException e) {
                logger.log(Level.SEVERE, e::toString);
                throw new AppErrorCheckedException(RUNTIME_ERROR);
            }
        }
    }

    /**
     * Retrieves the full forecast status for a given chat ID from the database.
     *
     * @param chatId the ID of the chat for which to retrieve the forecast status.
     * @return true if the forecast is full, false otherwise. If no record is found,
     *         returns true by default.
     * @throws AppErrorCheckedException if a database access error occurs.
     */
    public static boolean getisFullForecast(final long chatId) throws AppErrorCheckedException {
        final String selectSQL = "SELECT isFullForecast FROM fullForecast WHERE chatId = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                PreparedStatement selectStmt = conn.prepareStatement(selectSQL)) {
            selectStmt.setLong(1, chatId);
            final ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("isFullForecast");
            } else {
                // By default forecast is full.
                return true;
            }
        } catch (final SQLException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }

    /**
     * Checks if a given chat ID exists in the specified table.
     *
     * @param chatId    The chat ID to check for existence in the database.
     * @param tableName The name of the table to search for the chat ID.
     * @return true if the chat ID exists in the table, false otherwise.
     * @throws AppErrorCheckedException If there is an error with the table name
     *                                  validation or a database access error
     *                                  occurs.
     */
    private static boolean ifChatIdInDb(final long chatId, final String tableName)
            throws AppErrorCheckedException {
        String selectSQL;
        if (DataValidation.isTableNameValid(tableName)) {
            selectSQL = "SELECT chatId FROM " + tableName + " WHERE chatId = ?";
        } else {
            logger.log(Level.SEVERE, () -> String.format(
                    "Invalid table name (From DataValidation): %s", tableName));
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                PreparedStatement selectStmt = conn.prepareStatement(selectSQL)) {
            selectStmt.setLong(1, chatId);
            final ResultSet rs = selectStmt.executeQuery();
            return rs.next();
        } catch (final SQLException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }

    private Database() {
        throw new IllegalStateException("Utility class");
    }
}
