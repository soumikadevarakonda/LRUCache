package com.lrucache.exception;

/**
 * Thrown when a null value is supplied to the Cache.
 */
public class NullValueException extends CacheException {
    public NullValueException(String message) {
        super(message);
    }
}
