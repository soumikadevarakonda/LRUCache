package com.lrucache.concurrency;

import com.lrucache.cache.Cache;
import com.lrucache.cache.EvictionListener;
import com.lrucache.exception.NullKeyException;
import com.lrucache.exception.NullValueException;

import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe decorator for a Bounded Cache using ReentrantReadWriteLock.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class ThreadSafeCache<K, V> implements Cache<K, V> {
    private final Cache<K, V> delegate;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true); // fair lock
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    /**
     * Constructs a ThreadSafeCache decorating the provided Cache instance.
     *
     * @param delegate the non-thread-safe Cache to delegate to
     * @throws IllegalArgumentException if delegate is null
     */
    public ThreadSafeCache(Cache<K, V> delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("Delegate cache cannot be null");
        }
        this.delegate = delegate;
    }

    @Override
    public V get(K key) {
        if (key == null) {
            throw new NullKeyException("Key cannot be null");
        }
        // In access-ordered LinkedHashMap, get() moves elements to the tail, which is a structural write.
        // Therefore, we must acquire a Write Lock to prevent concurrency corruption.
        writeLock.lock();
        try {
            return delegate.get(key);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void put(K key, V value) {
        if (key == null) {
            throw new NullKeyException("Key cannot be null");
        }
        if (value == null) {
            throw new NullValueException("Value cannot be null");
        }
        writeLock.lock();
        try {
            delegate.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean containsKey(K key) {
        if (key == null) {
            throw new NullKeyException("Key cannot be null");
        }
        // containsKey is read-only and does not modify the access order of LinkedHashMap.
        readLock.lock();
        try {
            return delegate.containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            delegate.clear();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public int size() {
        readLock.lock();
        try {
            return delegate.size();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int capacity() {
        // Capacity is final/immutable, but delegates call for consistency.
        return delegate.capacity();
    }

    @Override
    public Map<K, V> getContents() {
        readLock.lock();
        try {
            return delegate.getContents();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public long getEvictionCount() {
        readLock.lock();
        try {
            return delegate.getEvictionCount();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void setEvictionListener(EvictionListener<K, V> listener) {
        writeLock.lock();
        try {
            delegate.setEvictionListener(listener);
        } finally {
            writeLock.unlock();
        }
    }
}
