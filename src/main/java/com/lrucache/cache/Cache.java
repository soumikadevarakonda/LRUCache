package com.lrucache.cache;

import java.util.Map;

/**
 * Interface representing a generic Bounded Cache.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public interface Cache<K, V> {
    /**
     * Retrieves the value associated with the given key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or {@code null} if this cache contains no mapping for the key
     */
    V get(K key);

    /**
     * Associates the specified value with the specified key in this cache.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     */
    void put(K key, V value);

    /**
     * Returns {@code true} if this cache contains a mapping for the specified key.
     *
     * @param key key whose presence in this cache is to be tested
     * @return {@code true} if this cache contains a mapping for the specified key
     */
    boolean containsKey(K key);

    /**
     * Removes all of the mappings from this cache.
     */
    void clear();

    /**
     * Returns the number of key-value mappings in this cache.
     *
     * @return the number of key-value mappings in this cache
     */
    int size();

    /**
     * Returns the maximum capacity of this cache.
     *
     * @return the maximum capacity of this cache
     */
    int capacity();

    /**
     * Returns a copy of the current cache contents in its traversal order.
     *
     * @return a copy of current key-value mappings
     */
    Map<K, V> getContents();

    /**
     * Returns the total number of evictions that have occurred during the cache's lifecycle.
     *
     * @return eviction count
     */
    long getEvictionCount();

    /**
     * Registers an eviction listener to receive callbacks when entries are evicted.
     *
     * @param listener the listener to register
     */
    void setEvictionListener(EvictionListener<K, V> listener);
}
