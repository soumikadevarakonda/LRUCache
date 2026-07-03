package com.lrucache.exception;

/**
 * Thrown when a null key is supplied to the Cache.
 */
public class NullKeyException extends CacheException {
    public NullKeyException(String message) {
        super(message);
    }
}
