package com.example;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;


/**
 * LinuxLogFormatter is a custom log formatter for formatting log records in a specific format.
 * It extends the Formatter class and overrides the format method to provide a custom log format.
 * The log format includes a timestamp, log level, source class name, source method name, and the log message.
 * 
 * The format is as follows:
 * [timestamp] [log level]: [source class name]:[source method name]: [log message]
 * 
 * Example:
 * 2023-10-05 14:23:45 INFO: com.example.MyClass:myMethod: This is a log message
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
        String className = logRecord.getSourceClassName();
        String methodName = logRecord.getSourceMethodName();
        if (className != null) {
            sb.append(className + ":");
        }
        if (methodName != null) {
            sb.append(methodName + ":");
        }
        sb.append(logRecord.getMessage()); // Log message
        sb.append(System.lineSeparator()); // New line
        return sb.toString();
    }
}