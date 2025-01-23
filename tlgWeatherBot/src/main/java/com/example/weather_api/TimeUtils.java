package com.example.weather_api;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.exceptions.AppErrorCheckedException;

/**
 * Utility class for time-related operations.
 * <p>
 * This class provides methods to convert Unix timestamps to LocalDateTime objects
 * considering timezone offsets. It also ensures that the class cannot be instantiated.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * long unixTime = 1633072800L; // Example Unix timestamp
 * long timezoneOffset = 3600; // Example timezone offset in seconds
 * LocalDateTime dateTime = TimeUtils.unixToLocalDateTimeConverter(unixTime, timezoneOffset);
 * }
 * </pre>
 * </p>
 * <p>
 * Note: This class is not meant to be instantiated.
 * </p>
 * <p>
 * Methods:
 * <ul>
 * <li>{@link #unixToLocalDateTimeConverter(long, long)} - Converts a Unix timestamp to a LocalDateTime object considering the timezone offset.</li>
 * </ul>
 * </p>
 */
public class TimeUtils {

    private static final Logger logger = Logger.getLogger(TimeUtils.class.getName());

    private static final String RUNTIME_ERROR = "Runtime error.";
    /**
     * Converts a Unix timestamp to a LocalDateTime object considering the timezone
     * offset.
     *
     * @param unixTime The Unix timestamp to convert.
     * @param timezone The timezone offset in seconds.
     * @return The LocalDateTime object representing the converted timestamp.
     * @throws AppErrorCheckedException If an error occurs during the conversion.
     */
    public static LocalDateTime unixToLocalDateTimeConverter(final long unixTime, final long timezone)
            throws AppErrorCheckedException {
        try {
            final Instant instant = Instant.ofEpochSecond(unixTime + timezone);
            return LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));
        } catch (final DateTimeException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }

    // Private constructor to hide the implicit public one
    private TimeUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
