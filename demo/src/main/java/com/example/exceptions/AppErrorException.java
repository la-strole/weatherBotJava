package com.example.exceptions;

/**
 * Custom exception class for handling application-specific errors.
 * This class extends {@link RuntimeException} and provides multiple constructors
 * to create an exception instance with a default message, a custom message,
 * a custom message with a cause, or just a cause.
 * 
 * <p>Usage examples:</p>
 * <pre>
 * throw new AppErrorException();
 * throw new AppErrorException("Custom error message");
 * throw new AppErrorException("Custom error message", cause);
 * throw new AppErrorException(cause);
 * </pre>
 */
public class AppErrorException extends RuntimeException {
    // Constructor with no arguments
    public AppErrorException() {
        super("AppError occurred");
    }

    // Constructor that accepts a custom message
    public AppErrorException(String message) {
        super(message);
    }

    // Constructor that accepts a custom message and a cause
    public AppErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor that accepts a cause
    public AppErrorException(Throwable cause) {
        super(cause);
    }
}
