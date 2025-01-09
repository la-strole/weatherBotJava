package com.example;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.Set;
import com.example.exceptions.AppErrorCheckedException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * The DataValidation class provides methods to validate various data inputs
 * such as language codes,
 * city names, longitude, and latitude.
 * 
 * <p>
 * This class contains a static map of supported languages and provides methods
 * to check if a
 * language is supported, if a city name is valid, and if longitude and latitude
 * values are within
 * valid ranges.
 * </p>
 * 
 * <p>
 * Supported languages are stored in a static map with language codes as keys
 * and language names as
 * values.
 * </p>
 * 
 * <p>
 * Methods:
 * </p>
 * <ul>
 * <li>{@link #isCityNameValid(String)} - Validates a city name based on allowed
 * characters and
 * length.</li>
 * <li>{@link #isLongitudeValid(Double)} - Validates if a longitude value is
 * within the range of
 * -180 to 180.</li>
 * <li>{@link #isLatitudeValid(Double)} - Validates if a latitude value is
 * within the range of -90
 * to 90.</li>
 * </ul>
 */
public class DataValidation {
    private static final Logger logger = Logger.getLogger(DataValidation.class.getName());

    public static ResourceBundle getMessages(String language) {
        Locale locale = Locale.forLanguageTag(language);
        ResourceBundle messages;
        try {
            messages = ResourceBundle.getBundle("messages", locale);
        } catch (MissingResourceException e) {
            messages = ResourceBundle.getBundle("messages", Locale.ENGLISH); // Fallback to English
        }
        return messages;
    }

    public static String getStringFromResourceBoundle(ResourceBundle messages, String string)
            throws AppErrorCheckedException {
        try {
            return messages.getString(string);
        } catch (NullPointerException | MissingResourceException | ClassCastException e) {
            logger.severe("getString\t" + e);
            throw new AppErrorCheckedException("Runtime Error.");
        }
    }

    /**
     * Formats a given LocalDateTime object to a string in UTC time format (HH:mm).
     *
     * @param dateTimeObject the LocalDateTime object to be formatted
     * @return a string representing the formatted time in HH:mm format
     * @throws AppErrorCheckedException if there is an error during formatting
     */
    public static String utcTimeFormatter(LocalDateTime dateTimeObject)
            throws AppErrorCheckedException {
        try {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return dateTimeObject.format(formatter);
        } catch (DateTimeException | IllegalArgumentException e) {
            logger.severe(String.format("dateTime = %s, Error:%s",
                    dateTimeObject.toString(), e));
            throw new AppErrorCheckedException("Runtime Error.");
        }
    }

    /**
     * Formats a given LocalDateTime object to a string in the format "dd.MM.yyyy".
     *
     * @param dateTimeObject the LocalDateTime object to be formatted
     * @return a string representation of the date in the format "dd.MM.yyyy"
     * @throws AppErrorCheckedException if there is an error during formatting
     */
    public static String utcDateFormatter(LocalDateTime dateTimeObject)
            throws AppErrorCheckedException {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            return dateTimeObject.format(formatter);
        } catch (DateTimeException | IllegalArgumentException e) {
            logger.severe(String.format("dateTime = %s, Error:%s",
                    dateTimeObject.toString(), e));
            throw new AppErrorCheckedException(
                    "Runtime Error.");
        }
    }

    /**
     * Converts a Unix timestamp to a LocalDateTime object in UTC.
     *
     * @param unixTimestamp the Unix timestamp to convert
     * @return the LocalDateTime object representing the given Unix timestamp in UTC
     * @throws AppErrorCheckedException if there is an error during the conversion
     */
    public static LocalDateTime getDateTimeObjectFromUnixTimestamp(long unixTimestamp)
            throws AppErrorCheckedException {

        try {
            Instant instant = Instant.ofEpochSecond(unixTimestamp);
            return LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));
        } catch (DateTimeException e) {
            logger.severe(String.format("unixTimestamp = %d, Error:%s",
                    unixTimestamp, e));
            throw new AppErrorCheckedException("Runtime Error.");
        }
    }

    public static boolean isCityNameValid(String cityName) {
        return (cityName.matches("^[\\p{L}[\\s'-]]+$") && cityName.length() <= 25);
    }

    public static boolean isLongitudeValid(Double lon) {
        return (lon >= -180 && lon <= 180);
    }

    public static boolean isLatitudeValid(Double lat) {
        return (lat >= -90 && lat <= 90);
    }

    public static boolean isTableNameValid(String tableName) {
        Set<String> tableNames = Set.of("multipleCities", "forecasts", "subscribes", "fullForecast");
        return (tableNames.contains(tableName));
    }
}
