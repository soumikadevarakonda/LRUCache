package com.lrucache.cache;

/**
 * Listener interface to receive cache eviction events.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
@FunctionalInterface
public interface EvictionListener<K, V> {
    /**
     * Called when an entry is evicted from the cache.
     *
     * @param key   the key of the evicted entry
     * @param value the value of the evicted entry
     */
    void onEviction(K key, V value);
}
