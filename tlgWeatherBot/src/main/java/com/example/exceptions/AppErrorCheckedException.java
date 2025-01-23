package com.example.exceptions;


/**
 * Custom checked exception class for handling application-specific errors.
 * This exception can be used to signal various error conditions in the application.
 * 
 * <p>There are four constructors available:</p>
 * <ul>
 *   <li>{@link #AppErrorCheckedException()} - Constructs a new exception with a default message.</li>
 *   <li>{@link #AppErrorCheckedException(String)} - Constructs a new exception with a specified detail message.</li>
 *   <li>{@link #AppErrorCheckedException(String, Throwable)} - Constructs a new exception with a specified detail message and cause.</li>
 *   <li>{@link #AppErrorCheckedException(Throwable)} - Constructs a new exception with a specified cause.</li>
 * </ul>
 */
public class AppErrorCheckedException extends Exception {
    // Constructor with no arguments
    public AppErrorCheckedException() {
        super("AppError occurred");
    }

    // Constructor that accepts a custom message
    public AppErrorCheckedException(String message) {
        super(message);
    }

    // Constructor that accepts a custom message and a cause
    public AppErrorCheckedException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor that accepts a cause
    public AppErrorCheckedException(Throwable cause) {
        super(cause);
    }
}
