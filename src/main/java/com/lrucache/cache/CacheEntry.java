package com.lrucache.cache;

import java.time.Instant;

/**
 * Wraps a cached value with metadata such as hit counts and timestamps.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class CacheEntry<K, V> {
    private final K key;
    private V value;
    private final Instant createdAt;
    private Instant lastAccessTime;
    private long hitCount;

    public CacheEntry(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        this.key = key;
        this.value = value;
        this.createdAt = Instant.now();
        this.lastAccessTime = this.createdAt;
        this.hitCount = 0;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        this.value = value;
        this.lastAccessTime = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastAccessTime() {
        return lastAccessTime;
    }

    public long getHitCount() {
        return hitCount;
    }

    /**
     * Records an access to this entry by updating the last access time and incrementing the hit count.
     */
    public void recordAccess() {
        this.lastAccessTime = Instant.now();
        this.hitCount++;
    }

    @Override
    public String toString() {
        return "CacheEntry{" +
                "key=" + key +
                ", value=" + value +
                ", createdAt=" + createdAt +
                ", lastAccessTime=" + lastAccessTime +
                ", hitCount=" + hitCount +
                '}';
    }
}
