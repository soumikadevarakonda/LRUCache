package com.lrucache.service;

/**
 * Service interface for defining slow/expensive operations to be cached.
 *
 * @param <K> key type
 * @param <V> value type
 */
@FunctionalInterface
public interface ComputationService<K, V> {
    /**
     * Computes a value associated with the given key.
     *
     * @param key the key to compute for
     * @return the computed value
     * @throws Exception if computation fails
     */
    V compute(K key) throws Exception;
}
