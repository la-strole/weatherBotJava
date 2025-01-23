package com.example;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * LinuxLogFormatter is a custom log formatter that formats log records in a specific
 * pattern suitable for Linux systems. The log message is formatted to include a timestamp,
 * log level, and the actual log message, each separated by a space or a separator.
 * 
 * The format is as follows:
 * <pre>
 * yyyy-MM-dd HH:mm:ss LEVEL: message
 * </pre>
 * 
 * Example:
 * <pre>
 * 2023-10-05 14:23:45 INFO: Application started
 * </pre>
 * 
 * The timestamp is formatted using the pattern "yyyy-MM-dd HH:mm:ss".
 */
public class LinuxLogFormatter extends Formatter {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public String format(final LogRecord logRecord) {
        final StringBuilder sb = new StringBuilder();
        sb.append(dateFormat.format(new Date(logRecord.getMillis()))); // Timestamp
        sb.append(" "); // Space between timestamp and log level
        sb.append(logRecord.getLevel().getName()); // Log level (e.g., INFO, WARNING)
        sb.append(": "); // Separator
        sb.append(logRecord.getMessage()); // Log message
        sb.append(System.lineSeparator()); // New line
        return sb.toString();
    }
}