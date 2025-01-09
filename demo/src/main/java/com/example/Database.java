package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.exceptions.AppErrorCheckedException;
import com.example.exceptions.AppErrorException;

/**
 * The Database class provides methods for interacting with a SQLite database.
 * It includes methods for creating tables, inserting and updating records, and
 * retrieving data.
 * 
 * The class supports operations for managing chat IDs, city coordinates,
 * weather forecasts, and subscriptions.
 * 
 * The following tables are created and managed by this class:
 * - multipleCities: Stores information about multiple cities with columns for
 * id, chatId, msgId, coordinates, and created_at.
 * - forecasts: Stores weather forecast information with columns for id, chatId,
 * msgId, forecast, and created_at.
 * - subscribes: Stores subscription information with columns for id, chatId,
 * lon, lat, cityName, time, and created_at.
 * 
 * The class uses the SQLite auto_vacuum mode set to FULL.
 * 
 * Methods:
 * - createTable(): Creates the necessary tables in the database if they do not
 * already exist.
 * - insertCities(long chatId, long msgId, JSONArray citiesCoordinates): Inserts
 * or updates city coordinates for a given chat ID in the database.
 * - getCoordinates(long chatId, long msgId): Retrieves the coordinates stored
 * in the database for a given chat ID and message ID.
 * - insertForecast(long chatId, long msgId, JSONArray forecast): Inserts or
 * updates a weather forecast in the database for a given chat ID.
 * - getForecast(long chatId, long msgId): Retrieves the weather forecast from
 * the database for the specified chat and message IDs.
 * - insertSubscribe(long chatId, Double lon, Double lat, long time, String
 * language): Inserts or updates a subscription in the database for the given
 * chat ID.
 * - getSubscribe(long chatId): Retrieves the subscription details for a given
 * chat ID from the database.
 * 
 * Exceptions:
 * - AppErrorCheckedException: Thrown if there is an error with table name
 * validation or a database access error occurs.
 * - AppErrorException: Thrown if there is an error creating the tables.
 * 
 * Logging:
 * - Uses java.util.logging.Logger for logging errors and information.
 */
public class Database {
    public static final String DATABASE_URL = "jdbc:sqlite:database.db";

    private static final Logger logger = Logger.getLogger(Database.class.getName());
    static {
        logger.setLevel(Level.FINE);
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
    private static boolean ifChatIdInDb(long chatId, String tableName)
            throws AppErrorCheckedException {
        String selectSQL;
        if (DataValidation.isTableNameValid(tableName)) {
            selectSQL = "SELECT chatId FROM " + tableName + " WHERE chatId = ?";
        } else {
            logger.severe(String.format(
                    "Invalid table name (From DataValidation): %s", tableName));
            throw new AppErrorCheckedException("Database:ifChatIdInDb:\t Runtime error");
        }
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                PreparedStatement selectStmt = conn.prepareStatement(selectSQL)) {
            selectStmt.setLong(1, chatId);
            ResultSet rs = selectStmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            logger.severe("ifChatIdInDb:\t" + e);
            throw new AppErrorCheckedException("Runtime error");
        }
    }

    /**
     * Creates the necessary tables in the database if they do not already exist.
     * 
     * The tables created are:
     * - multipleCities: Stores information about multiple cities with columns for
     * id, chatId, msgId, coordinates, and created_at.
     * - forecasts: Stores weather forecast information with columns for id, chatId,
     * msgId, forecast, and created_at.
     * - subscribes: Stores subscription information with columns for id, chatId,
     * lon, lat, cityName, time, and created_at.
     * 
     * The method also sets the SQLite auto_vacuum mode to FULL.
     * 
     * @throws AppErrorException if there is an error creating the tables.
     */
    public static void createTable() {

        String createTable1SQL = "CREATE TABLE IF NOT EXISTS multipleCities (" + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "chatId INT NOT NULL," + "msgId INT NOT NULL,"
                + "coordinates TEXT NOT NULL," + "created_at TEXT NOT NULL" + ")";
        String createTable2SQL = "CREATE TABLE IF NOT EXISTS forecasts (" + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "chatId INT NOT NULL," + "msgId INT NOT NULL," + "forecast TEXT NOT NULL,"
                + "created_at TEXT NOT NULL" + ")";
        String createTable3SQL = "CREATE TABLE IF NOT EXISTS subscribes ("
                + "id INT AUTO_INCREMENT PRIMARY KEY," + "chatId INT NOT NULL,"
                + "lon REAL NOT NULL," + "lat REAL NOT NULL," + "cityName TEXT NOT NULL,"
                + "time TEXT NOT NULL," + "created_at TEXT NOT NULL" + ")";
        String createTable4SQL = "CREATE TABLE IF NOT EXISTS fullForecast ("
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

            logger.fine("Tables created successfully");
        } catch (SQLException e) {
            logger.severe(e.toString());
            throw new AppErrorException("Can not create table.");
        }
    }

    /**
     * Inserts or updates city coordinates for a given chat ID in the database.
     *
     * <p>
     * If the chat ID already exists in the database, the coordinates and message ID
     * are updated.
     * Otherwise, a new record is inserted.
     *
     * @param chatId            the chat ID to associate with the city coordinates
     * @param msgId             the message ID to associate with the city
     *                          coordinates
     * @param citiesCoordinates a JSON array containing the city coordinates
     * @throws AppErrorCheckedException if a database access error occurs
     *
     *
     */
    public static void insertCities(long chatId, long msgId, JSONArray citiesCoordinates)
            throws AppErrorCheckedException {

        // Check if the coordinates are already in the database
        if (!ifChatIdInDb(chatId, "multipleCities")) {
            String insertSQL = "INSERT INTO multipleCities (chatId, msgId, coordinates, created_at) VALUES (?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                    PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                insertStmt.setLong(1, chatId);
                insertStmt.setLong(2, msgId);
                insertStmt.setString(3, citiesCoordinates.toString());
                insertStmt.setString(4, Instant.now().toString());
                insertStmt.executeUpdate();
                logger.fine("Insert multiple cities coordinates into database");
            } catch (SQLException e) {
                logger.severe("insertCities:\tE" + e);
                throw new AppErrorCheckedException("Database:insertCities: Runtime Error.");
            }
        } else {
            String updateSQL = "UPDATE multipleCities SET coordinates = ? , msgId = ? , created_at = ?  WHERE chatId = ?";
            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                    PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
                updateStmt.setString(1, citiesCoordinates.toString());
                updateStmt.setLong(2, msgId);
                updateStmt.setString(3, Instant.now().toString());
                updateStmt.setLong(4, chatId);
                updateStmt.executeUpdate();
                logger.fine("Updated multiple cities in database");
            } catch (SQLException e) {
                logger.severe(e.toString());
                throw new AppErrorCheckedException("Runtime Error.");
            }
        }
    }

    /**
     * Retrieves the coordinates stored in the database for a given chat ID and
     * message ID.
     *
     * @param chatId the ID of the chat
     * @param msgId  the ID of the message
     * @return a JSONArray containing the coordinates, or an empty JSONArray if no
     *         coordinates are found
     * @throws AppErrorCheckedException if a database access error occurs
     */
    public static JSONArray getCoordinates(long chatId, long msgId)
            throws AppErrorCheckedException {
        String selectSQL = "SELECT coordinates FROM multipleCities WHERE chatId = ? AND msgId = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                PreparedStatement selectStmt = conn.prepareStatement(selectSQL)) {
            selectStmt.setLong(1, chatId);
            selectStmt.setLong(2, msgId);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                return new JSONArray(rs.getString("coordinates"));

            } else {
                return new JSONArray();
            }
        } catch (SQLException e) {
            logger.severe(e.toString());
            throw new AppErrorCheckedException("Runtime Error.");
        }
    }

    /**
     * Inserts or updates a weather forecast in the database for a given chat ID.
     * 
     * If the chat ID does not already exist in the database, a new record is
     * inserted.
     * Otherwise, the existing record is updated with the new forecast data.
     *
     * @param chatId   The ID of the chat for which the forecast is being stored.
     * @param msgId    The ID of the message associated with the forecast.
     * @param forecast A JSONArray containing the forecast data.
     * @throws AppErrorCheckedException If a database error occurs during the
     *                                  operation.
     */
    public static void insertForecast(long chatId, long msgId, JSONObject forecast)
            throws AppErrorCheckedException {

        // Check if the coordinates are already in the database

        if (!ifChatIdInDb(chatId, "forecasts")) {
            String insertSQL = "INSERT INTO forecasts (chatId, msgId, forecast, created_at) VALUES (?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                    PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                insertStmt.setLong(1, chatId);
                insertStmt.setLong(2, msgId);
                insertStmt.setString(3, forecast.toString());
                insertStmt.setString(4, Instant.now().toString());
                insertStmt.executeUpdate();
            } catch (SQLException e2) {
                logger.severe(e2.toString());
                throw new AppErrorCheckedException("Runtime Error.");
            }
        } else {
            String updateSQL = "UPDATE forecasts SET forecast = ? , msgId = ? , created_at = ?  WHERE chatId = ?";
            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                    PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
                updateStmt.setString(1, forecast.toString());
                updateStmt.setLong(2, msgId);
                updateStmt.setString(3, Instant.now().toString());
                updateStmt.setLong(4, chatId);
                updateStmt.executeUpdate();
            } catch (SQLException e) {
                logger.severe(e.toString());
                throw new AppErrorCheckedException("Runtime Error.");
            }
        }
    }

    /**
     * Retrieves the weather forecast from the database for the specified chat and
     * message IDs.
     *
     * @param chatId the ID of the chat for which the forecast is being retrieved
     * @param msgId  the ID of the message for which the forecast is being retrieved
     * @return a JSONArray containing the forecast data; if no forecast is found, an
     *         empty JSONArray is returned
     * @throws AppErrorCheckedException if there is an error accessing the database
     *                                  or parsing the JSON data
     */
    public static JSONObject getForecast(long chatId, long msgId) throws AppErrorCheckedException {
        String selectSQL = "SELECT forecast FROM forecasts WHERE chatId = ? AND msgId = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                PreparedStatement selectStmt = conn.prepareStatement(selectSQL)) {
            selectStmt.setLong(1, chatId);
            selectStmt.setLong(2, msgId);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                return new JSONObject(rs.getString("forecast"));

            } else {
                return new JSONObject();
            }
        } catch (SQLException | JSONException e) {
            logger.severe(e.toString());
            throw new AppErrorCheckedException("Runtime Error.");
        }
    }

    public static int deleteForecast(long chatId) throws AppErrorCheckedException {
        String selectSQL = "DELETE FROM forecasts WHERE chatId = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                PreparedStatement selectStmt = conn.prepareStatement(selectSQL)) {
            selectStmt.setLong(1, chatId);
            return selectStmt.executeUpdate();
            
        } catch (SQLException | JSONException e) {
            logger.severe(e.toString());
            throw new AppErrorCheckedException("Runtime Error.");
        }
    }

    /**
     * Inserts or updates a subscription in the database for the given chat ID.
     * 
     * This method first retrieves the city name from the geocoding API using the
     * provided
     * longitude and latitude. It then checks if the chat ID already exists in the
     * database.
     * If the chat ID does not exist, it inserts a new record. If the chat ID
     * exists, it updates
     * the existing record with the new information.
     * 
     * @param chatId   The chat ID to subscribe.
     * @param lon      The longitude of the location.
     * @param lat      The latitude of the location.
     * @param time     The time of the subscription.
     * @param language The language code for the local city name.
     * @throws AppErrorCheckedException If there is an error during the database
     *                                  operation or
     *                                  while retrieving the city name.
     */
    public static void insertSubscribe(long chatId, Double lon, Double lat, long time,
            String language) throws AppErrorCheckedException {

        // Get the city name from the geocoding API.
        JSONArray cityNamesArray = GeocodingApi.getCitiesNamesByCoordinatesArray(lon, lat);
        // Get the local city name.
        String localCityName;
        try {
            localCityName = cityNamesArray.getJSONObject(0).getJSONObject("local_names")
                    .optString(language, "");
            if (localCityName.isEmpty()) {
                localCityName = cityNamesArray.getJSONObject(0).getString("name");
            }
        } catch (JSONException e) {
            logger.severe(e.toString());
            throw new AppErrorCheckedException("Runtime Error");
        }
        // Check if the chatId is already in the database
        if (!ifChatIdInDb(chatId, "subscribes")) {
            String insertSQL = "INSERT INTO subscribes (chatId, cityName, lon, lat, time, created_at) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                    PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                insertStmt.setLong(1, chatId);
                insertStmt.setString(2, localCityName);
                insertStmt.setDouble(3, lon);
                insertStmt.setDouble(4, lat);
                insertStmt.setTimestamp(5, new Timestamp(time));
                insertStmt.setString(6, Instant.now().toString());
                insertStmt.executeUpdate();
            } catch (SQLException e2) {
                logger.severe(e2.toString());
                throw new AppErrorCheckedException("Runtime Error");
            }
        } else {
            String updateSQL = "UPDATE subscribes SET cityName = ? , lon = ? , lat = ? , time = ? , created_at = ?  WHERE chatId = ?";
            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                    PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
                updateStmt.setString(1, localCityName);
                updateStmt.setDouble(2, lon);
                updateStmt.setDouble(3, lat);
                updateStmt.setTimestamp(4, new Timestamp(time));
                updateStmt.setString(5, Instant.now().toString());
                updateStmt.setLong(6, chatId);
                updateStmt.executeUpdate();
            } catch (SQLException e) {
                logger.severe(e.toString());
                throw new AppErrorCheckedException("Runtime Error");
            }
        }
    }

    /**
     * Retrieves the subscription details for a given chat ID from the database.
     *
     * @param chatId the ID of the chat for which to retrieve subscription details
     * @return a JSONArray containing subscription details, where each JSONObject
     *         represents a subscription with the following keys:
     *         - "cityName": the name of the city (String)
     *         - "lon": the longitude of the city (double)
     *         - "lat": the latitude of the city (double)
     *         - "time": the timestamp of the subscription (Timestamp)
     * @throws AppErrorCheckedException if a database access error occurs
     */
    public static JSONArray getSubscribe(long chatId) throws AppErrorCheckedException {
        String selectSQL = "SELECT cityName, lon, lat, time FROM subscribes WHERE chatId = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                PreparedStatement selectStmt = conn.prepareStatement(selectSQL)) {
            selectStmt.setLong(1, chatId);
            ResultSet rs = selectStmt.executeQuery();
            JSONArray result = new JSONArray();
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("cityName", rs.getString("cityName"));
                obj.put("lon", rs.getDouble("lon"));
                obj.put("lat", rs.getDouble("lat"));
                obj.put("time", rs.getTimestamp("time"));
                result.put(obj);
            }
            return result;
        } catch (SQLException e) {
            logger.severe(e.toString());
            throw new AppErrorCheckedException("Runtime Error");
        }
    }

    /**
     * Retrieves the subscription details for a given chat ID and formats them into
     * a string.
     *
     * @param chatId The chat ID for which to retrieve subscription details.
     * @return A formatted string containing the city names and times of the
     *         subscriptions.
     * @throws AppErrorCheckedException If there is an error retrieving or
     *                                  processing the subscription data.
     */
    public static String getSubscribeString(long chatId) throws AppErrorCheckedException {

        StringBuilder subscription = new StringBuilder();
        JSONArray rs = getSubscribe(chatId);

        // If there are subscriptions.
        if (!rs.isEmpty()) {
            // For each row (row is JSONObject)
            for (int i = 0; i < rs.length(); i++) {
                try {
                    JSONObject obj = rs.getJSONObject(i);
                    // Append the city name and time.
                    subscription.append(String.format("%s\t%s%n", obj.getString("cityName"),
                            obj.getString("time")));
                } catch (JSONException e) {
                    logger.severe(e.toString());
                    throw new AppErrorCheckedException("Runtime Error");
                }
            }
        }
        return subscription.toString();
    }

    public static void insertIsFullForecast(long chatId, boolean value) throws AppErrorCheckedException {

        // Check if the chatId is already in the database
        if (!ifChatIdInDb(chatId, "fullForecast")) {
            String insertSQL = "INSERT INTO fullForecast (chatId, isFullForecast, created_at) VALUES (?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                    PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                insertStmt.setLong(1, chatId);
                insertStmt.setBoolean(2, value);
                insertStmt.setString(3, Instant.now().toString());
                insertStmt.executeUpdate();
            } catch (SQLException e2) {
                logger.severe(e2.toString());
                throw new AppErrorCheckedException("Runtime Error");
            }
        } else {
            String updateSQL = "UPDATE fullForecast SET isFullForecast = ? , created_at = ?  WHERE chatId = ?";
            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                    PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
                updateStmt.setBoolean(1, value);
                updateStmt.setString(2, Instant.now().toString());
                updateStmt.setLong(3, chatId);
                updateStmt.executeUpdate();
            } catch (SQLException e) {
                logger.severe(e.toString());
                throw new AppErrorCheckedException("Runtime Error");
            }
        }
    }

    public static boolean getisFullForecast(long chatId) throws AppErrorCheckedException {
        String selectSQL = "SELECT isFullForecast FROM fullForecast WHERE chatId = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                PreparedStatement selectStmt = conn.prepareStatement(selectSQL)) {
            selectStmt.setLong(1, chatId);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("isFullForecast");

            } else {
                // By default forecast is full.
                return true;
            }
        } catch (SQLException e) {
            logger.severe(e.toString());
            throw new AppErrorCheckedException("Runtime Error");
        }
    }
}
