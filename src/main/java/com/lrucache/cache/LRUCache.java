package com.lrucache.cache;

import com.lrucache.exception.InvalidCapacityException;
import com.lrucache.exception.NullKeyException;
import com.lrucache.exception.NullValueException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Basic Bounded LRU Cache implementation using access-ordered LinkedHashMap.
 * This class is NOT thread-safe and is designed to be decorated for concurrency.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class LRUCache<K, V> implements Cache<K, V> {
    private final int capacity;
    private final Map<K, CacheEntry<K, V>> map;
    private long evictionCount = 0;
    private EvictionListener<K, V> evictionListener;

    /**
     * Constructs an LRUCache with the specified maximum capacity.
     *
     * @param capacity the maximum number of entries allowed in the cache
     * @throws InvalidCapacityException if capacity <= 0
     */
    public LRUCache(int capacity) {
        if (capacity <= 0) {
            throw new InvalidCapacityException("Capacity must be greater than 0. Got: " + capacity);
        }
        this.capacity = capacity;
        // loadFactor 0.75f, accessOrder = true (enables LRU eviction)
        this.map = new LinkedHashMap<K, CacheEntry<K, V>>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, CacheEntry<K, V>> eldest) {
                boolean shouldEvict = size() > LRUCache.this.capacity;
                if (shouldEvict) {
                    evictionCount++;
                    if (evictionListener != null) {
                        evictionListener.onEviction(eldest.getKey(), eldest.getValue().getValue());
                    }
                }
                return shouldEvict;
            }
        };
    }

    /**
     * Registers an eviction listener to receive callbacks when entries are evicted.
     *
     * @param evictionListener the listener to register
     */
    public void setEvictionListener(EvictionListener<K, V> evictionListener) {
        this.evictionListener = evictionListener;
    }

    @Override
    public V get(K key) {
        if (key == null) {
            throw new NullKeyException("Key cannot be null");
        }
        CacheEntry<K, V> entry = map.get(key);
        if (entry != null) {
            entry.recordAccess();
            return entry.getValue();
        }
        return null;
    }

    @Override
    public void put(K key, V value) {
        if (key == null) {
            throw new NullKeyException("Key cannot be null");
        }
        if (value == null) {
            throw new NullValueException("Value cannot be null");
        }
        CacheEntry<K, V> entry = map.get(key);
        if (entry != null) {
            entry.setValue(value);
            entry.recordAccess();
        } else {
            map.put(key, new CacheEntry<>(key, value));
        }
    }

    @Override
    public boolean containsKey(K key) {
        if (key == null) {
            throw new NullKeyException("Key cannot be null");
        }
        return map.containsKey(key);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public Map<K, V> getContents() {
        // Return a copy showing insertion/access order
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getValue(),
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
    }

    @Override
    public long getEvictionCount() {
        return evictionCount;
    }
}
