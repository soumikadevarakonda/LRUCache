package com.lrucache.exception;

/**
 * Thrown when an invalid capacity is supplied to the Cache constructor.
 */
public class InvalidCapacityException extends CacheException {
    public InvalidCapacityException(String message) {
        super(message);
    }
}
