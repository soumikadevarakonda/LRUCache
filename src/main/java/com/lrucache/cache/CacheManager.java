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
    private boolean loggingEnabled = true;

    /**
     * Constructs a CacheManager with logging enabled by default.
     *
     * @param cache              the Cache implementation to manage
     * @param computationService the service responsible for computing missing values
     */
    public CacheManager(Cache<K, V> cache, ComputationService<K, V> computationService) {
        this(cache, computationService, true);
    }

    /**
     * Constructs a CacheManager with configurable logging.
     *
     * @param cache              the Cache implementation to manage
     * @param computationService the service responsible for computing missing values
     * @param loggingEnabled     true to print logs, request blocks, and snapshots to console
     */
    public CacheManager(Cache<K, V> cache, ComputationService<K, V> computationService, boolean loggingEnabled) {
        if (cache == null) {
            throw new IllegalArgumentException("Cache cannot be null");
        }
        if (computationService == null) {
            throw new IllegalArgumentException("Computation service cannot be null");
        }
        this.cache = cache;
        this.computationService = computationService;
        this.loggingEnabled = loggingEnabled;
        this.statistics = new CacheStatistics(
            cache.capacity(),
            () -> (long) cache.size(),
            cache::getEvictionCount
        );

        // Register default lightweight eviction logger (respects loggingEnabled flag)
        this.cache.setEvictionListener((key, value) -> {
            if (this.loggingEnabled) {
                System.out.println("[INFO] Evicted: Key = " + key);
            }
        });
    }

    /**
     * Configures whether console logging, request blocks, and snapshots are printed.
     *
     * @param loggingEnabled true to enable logging, false to disable
     */
    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    /**
     * Returns whether console logging is enabled.
     *
     * @return true if logging is enabled
     */
    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    /**
     * Attempts to fetch a value from the cache. If it does not exist, computes it,
     * saves it to the cache, logs structured info, and prints the cache snapshot.
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
            
            if (loggingEnabled) {
                System.out.println("[INFO] Cache HIT. Returning cached value.");
                
                // Print structured cache request block
                System.out.println("==================================================");
                System.out.println("CACHE REQUEST");
                System.out.println("==================================================");
                System.out.println("Input          : " + key);
                System.out.println("Status         : CACHE HIT");
                System.out.println("Result         : " + value);
                System.out.printf("Execution Time : %.2f ms\n", TimeUtil.nanosToMillis(lookupDuration));
                System.out.println("==================================================");
                
                printCacheSnapshot();
            }
            return value;
        }

        if (loggingEnabled) {
            System.out.println("[INFO] Cache MISS. Computing result... ");
        }

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

        if (value == null) {
            throw new ComputationException("Computation service returned null for key: " + key);
        }

        // Store value back into the cache (this may trigger eviction listener logs)
        cache.put(key, value);
        
        if (loggingEnabled) {
            System.out.println("[INFO] Stored in cache.");
        }

        // Record metrics
        statistics.recordMiss(lookupDuration, compDuration);

        if (loggingEnabled) {
            // Print structured cache request block
            System.out.println("==================================================");
            System.out.println("CACHE REQUEST");
            System.out.println("==================================================");
            System.out.println("Input          : " + key);
            System.out.println("Status         : CACHE MISS");
            System.out.println("Result         : " + value);
            System.out.printf("Execution Time : %.2f ms\n", TimeUtil.nanosToMillis(lookupDuration + compDuration));
            System.out.println("==================================================");

            printCacheSnapshot();
        }
        return value;
    }

    /**
     * Prints a visual snapshot of the current cache contents ordered from LRU to MRU.
     */
    private void printCacheSnapshot() {
        System.out.println("Current Cache");
        System.out.println("LRU");
        cache.getContents().forEach((k, v) -> {
            System.out.println(k + " -> " + v);
        });
        System.out.println("MRU");
        System.out.println("==================================================\n");
    }

    public Cache<K, V> getCache() {
        return cache;
    }

    public CacheStatistics getStatistics() {
        return statistics;
    }
}
