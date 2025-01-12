package com.example.weather_api;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.exceptions.AppErrorCheckedException;

public class TimeUtils {

    // Private constructor to hide the implicit public one
    private TimeUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static final Logger logger  = Logger.getLogger(TimeUtils.class.getName());
    private static final String RUNTIME_ERROR = "Runtime error.";

    /**
     * Converts a Unix timestamp to a LocalDateTime object considering the timezone offset.
     *
     * @param unixTime The Unix timestamp to convert.
     * @param timezone The timezone offset in seconds.
     * @return The LocalDateTime object representing the converted timestamp.
     * @throws AppErrorCheckedException If an error occurs during the conversion.
     */
    public static LocalDateTime unixToLocalDateTimeConverter(long unixTime, long timezone)
            throws AppErrorCheckedException {
        try {
            Instant instant = Instant.ofEpochSecond(unixTime + timezone);
            return LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));
        } catch (DateTimeException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }
}
