package com.example;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.exceptions.AppErrorCheckedException;

/**
 * The DataValidation class provides utility methods for validating various types of data,
 * such as city names, geographic coordinates, and table names. It also includes methods
 * for retrieving localized messages from resource bundles.
 * 
 * <p>This class is designed to be used as a utility class and should not be instantiated.
 * The constructor is private to prevent instantiation.
 * 
 * <p>Methods included:
 * <ul>
 *   <li>{@link #getMessages(String)} - Retrieves a ResourceBundle containing localized messages based on the specified language.</li>
 *   <li>{@link #getStringFromResourceBoundle(ResourceBundle, String)} - Retrieves a string from the provided ResourceBundle.</li>
 *   <li>{@link #isCityNameValid(String)} - Validates a city name based on specific criteria.</li>
 *   <li>{@link #isLongitudeValid(Double)} - Validates a longitude value.</li>
 *   <li>{@link #isLatitudeValid(Double)} - Validates a latitude value.</li>
 *   <li>{@link #isTableNameValid(String)} - Validates a table name against a predefined set of valid table names.</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>
 * {@code
 * boolean isValidCity = DataValidation.isCityNameValid("New York");
 * boolean isValidLongitude = DataValidation.isLongitudeValid(-74.0060);
 * boolean isValidLatitude = DataValidation.isLatitudeValid(40.7128);
 * boolean isValidTable = DataValidation.isTableNameValid("forecasts");
 * ResourceBundle messages = DataValidation.getMessages("en");
 * String welcomeMessage = DataValidation.getStringFromResourceBoundle(messages, "welcome");
 * }
 * </pre>
 * 
 * <p>Note: This class uses a logger to log severe errors and throws a custom checked exception
 * {@link AppErrorCheckedException} for error handling.
 * 
 * @see java.util.ResourceBundle
 * @see java.util.Locale
 * @see java.util.logging.Logger
 * @see AppErrorCheckedException
 */
public class DataValidation {
    private static final Logger logger = Logger.getLogger(DataValidation.class.getName());
    private static final String RUNTIME_ERROR = "Runtime Error.";

    /**
     * Retrieves a ResourceBundle containing localized messages based on the
     * specified language.
     * If the specified language is not available, it falls back to English.
     *
     * @param language the language tag (e.g., "en", "fr", "es") to determine the
     *                 locale for the messages.
     * @return a ResourceBundle containing the localized messages for the specified
     *         language, or English if the language is not available.
     */
    public static ResourceBundle getMessages(final String language) {
        final Locale locale = Locale.forLanguageTag(language);
        ResourceBundle messages;
        try {
            messages = ResourceBundle.getBundle("messages", locale);
        } catch (final MissingResourceException e) {
            messages = ResourceBundle.getBundle("messages", Locale.ENGLISH); // Fallback to English
        }
        return messages;
    }

    /**
     * Retrieves a string from the provided ResourceBundle.
     *
     * @param messages the ResourceBundle containing the desired string
     * @param string   the key for the desired string in the ResourceBundle
     * @return the string associated with the specified key in the ResourceBundle
     * @throws AppErrorCheckedException if the key is not found, the value is not a
     *                                  string, or any other error occurs
     */
    public static String getStringFromResourceBoundle(final ResourceBundle messages, final String string)
            throws AppErrorCheckedException {
        try {
            return messages.getString(string);
        } catch (NullPointerException | MissingResourceException | ClassCastException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }

    public static boolean isCityNameValid(final String cityName) {
        return (cityName.matches("^[\\p{L}[\\s'-]]+$") && cityName.length() <= 25);
    }

    public static boolean isLongitudeValid(final Double lon) {
        return (lon >= -180 && lon <= 180);
    }

    public static boolean isLatitudeValid(final Double lat) {
        return (lat >= -90 && lat <= 90);
    }

    public static boolean isTableNameValid(final String tableName) {
        final Set<String> tableNames = Set.of("multipleCities", "forecasts", "subscribes", "fullForecast");
        return (tableNames.contains(tableName));
    }

    private DataValidation() {
        throw new IllegalStateException("Utility class");
    }
}
