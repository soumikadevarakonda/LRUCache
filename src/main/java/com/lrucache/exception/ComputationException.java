package com.lrucache.exception;

/**
 * Thrown when the computation service fails to generate a value for a cache key.
 */
public class ComputationException extends CacheException {
    public ComputationException(String message) {
        super(message);
    }

    public ComputationException(String message, Throwable cause) {
        super(message, cause);
    }
}
