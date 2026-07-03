package com.lrucache.exception;

/**
 * Base exception class for all Cache operations.
 */
public class CacheException extends RuntimeException {
    public CacheException(String message) {
        super(message);
    }

    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
