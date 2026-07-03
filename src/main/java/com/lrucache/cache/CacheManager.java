package com.lrucache.cache;

import com.lrucache.service.ComputationService;
import com.lrucache.statistics.CacheStatistics;
import com.lrucache.util.TimeUtil;
import com.lrucache.exception.ComputationException;

/**
 * High-level Cache Manager that orchestrates standard Cache get/put operations,
 * executes background computations on cache misses, and records cache statistics.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class CacheManager<K, V> {
    private final Cache<K, V> cache;
    private final ComputationService<K, V> computationService;
    private final CacheStatistics statistics;

    /**
     * Constructs a CacheManager.
     *
     * @param cache              the Cache implementation to manage
     * @param computationService the service responsible for computing missing values
     */
    public CacheManager(Cache<K, V> cache, ComputationService<K, V> computationService) {
        if (cache == null) {
            throw new IllegalArgumentException("Cache cannot be null");
        }
        if (computationService == null) {
            throw new IllegalArgumentException("Computation service cannot be null");
        }
        this.cache = cache;
        this.computationService = computationService;
        this.statistics = new CacheStatistics(
            cache.capacity(),
            () -> (long) cache.size(),
            cache::getEvictionCount
        );
    }

    /**
     * Attempts to fetch a value from the cache. If it does not exist, computes it
     * using the computation service, saves it to the cache, and returns it.
     * Tracks execution durations and logs progress.
     *
     * @param key the key to retrieve
     * @return the cached or computed value
     * @throws ComputationException if the computation fails
     */
    public V getOrCompute(K key) throws ComputationException {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        long lookupStart = System.nanoTime();
        V value = cache.get(key);
        long lookupEnd = System.nanoTime();
        long lookupDuration = lookupEnd - lookupStart;

        if (value != null) {
            statistics.recordHit(lookupDuration);
            System.out.println("Cache HIT");
            System.out.println("Result : " + value);
            System.out.printf("Execution Time : %.2f ms\n", TimeUtil.nanosToMillis(lookupDuration));
            return value;
        }

        statistics.recordMiss(lookupDuration);
        System.out.println("Cache MISS");

        long compStart = System.nanoTime();
        try {
            value = computationService.compute(key);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ComputationException("Computation interrupted for key: " + key, e);
        } catch (Exception e) {
            throw new ComputationException("Failed to compute value for key: " + key, e);
        }
        long compEnd = System.nanoTime();
        long compDuration = compEnd - compStart;

        statistics.recordComputation(compDuration);

        if (value == null) {
            throw new ComputationException("Computation service returned null for key: " + key);
        }

        cache.put(key, value);
        System.out.println("Result : " + value);
        System.out.printf("Execution Time : %.2f ms\n", TimeUtil.nanosToMillis(compDuration + lookupDuration));

        return value;
    }

    public Cache<K, V> getCache() {
        return cache;
    }

    public CacheStatistics getStatistics() {
        return statistics;
    }
}
