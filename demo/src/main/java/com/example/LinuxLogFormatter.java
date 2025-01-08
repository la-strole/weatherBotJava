package com.example;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LinuxLogFormatter extends Formatter {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(dateFormat.format(new Date(record.getMillis()))); // Timestamp
        sb.append(" "); // Space between timestamp and log level
        sb.append(record.getLevel().getName()); // Log level (e.g., INFO, WARNING)
        sb.append(": "); // Separator
        sb.append(record.getMessage()); // Log message
        sb.append(System.lineSeparator()); // New line
        return sb.toString();
    }
}