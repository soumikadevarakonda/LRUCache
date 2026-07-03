package com.lrucache.statistics;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/**
 * Thread-safe class tracking Cache statistics.
 */
public class CacheStatistics {
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    private final AtomicLong totalLookupTimeNanos = new AtomicLong(0);
    private final AtomicLong totalComputationTimeNanos = new AtomicLong(0);

    private final int capacity;
    private final LongSupplier currentSizeSupplier;
    private final LongSupplier evictionCountSupplier;

    /**
     * Constructs a CacheStatistics instance.
     *
     * @param capacity              the cache's maximum capacity
     * @param currentSizeSupplier   supplier providing the current cache size
     * @param evictionCountSupplier supplier providing the cache's eviction count
     */
    public CacheStatistics(int capacity, LongSupplier currentSizeSupplier, LongSupplier evictionCountSupplier) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        if (currentSizeSupplier == null || evictionCountSupplier == null) {
            throw new IllegalArgumentException("Suppliers cannot be null");
        }
        this.capacity = capacity;
        this.currentSizeSupplier = currentSizeSupplier;
        this.evictionCountSupplier = evictionCountSupplier;
    }

    /**
     * Records a cache hit, adding to hits and total lookup duration.
     *
     * @param lookupTimeNanos time taken for the lookup in nanoseconds
     */
    public void recordHit(long lookupTimeNanos) {
        hits.incrementAndGet();
        totalLookupTimeNanos.addAndGet(lookupTimeNanos);
    }

    /**
     * Records a cache miss, adding to misses and total lookup duration.
     *
     * @param lookupTimeNanos time taken for the lookup in nanoseconds
     */
    public void recordMiss(long lookupTimeNanos) {
        misses.incrementAndGet();
        totalLookupTimeNanos.addAndGet(lookupTimeNanos);
    }

    /**
     * Records computation duration.
     *
     * @param computationTimeNanos time taken for computation in nanoseconds
     */
    public void recordComputation(long computationTimeNanos) {
        totalComputationTimeNanos.addAndGet(computationTimeNanos);
    }

    public long getHits() {
        return hits.get();
    }

    public long getMisses() {
        return misses.get();
    }

    public long getTotalRequests() {
        return hits.get() + misses.get();
    }

    public double getHitRatio() {
        long total = getTotalRequests();
        return total == 0 ? 0.0 : (double) hits.get() / total;
    }

    public double getMissRatio() {
        long total = getTotalRequests();
        return total == 0 ? 0.0 : (double) misses.get() / total;
    }

    public long getEvictionCount() {
        return evictionCountSupplier.getAsLong();
    }

    public long getCurrentSize() {
        return currentSizeSupplier.getAsLong();
    }

    public int getMaximumCapacity() {
        return capacity;
    }

    public double getAverageLookupTimeMillis() {
        long total = getTotalRequests();
        return total == 0 ? 0.0 : (double) totalLookupTimeNanos.get() / total / 1_000_000.0;
    }

    public double getAverageComputationTimeMillis() {
        long missCount = misses.get();
        return missCount == 0 ? 0.0 : (double) totalComputationTimeNanos.get() / missCount / 1_000_000.0;
    }

    @Override
    public String toString() {
        long total = getTotalRequests();
        return String.format(
            "=========================================\n" +
            "            CACHE STATISTICS             \n" +
            "=========================================\n" +
            "  Maximum Capacity         : %d\n" +
            "  Current Cache Size       : %d\n" +
            "  Total Requests           : %d\n" +
            "  Cache Hits               : %d\n" +
            "  Cache Misses             : %d\n" +
            "  Hit Ratio                : %.2f%%\n" +
            "  Miss Ratio               : %.2f%%\n" +
            "  Number of Evictions      : %d\n" +
            "  Average Lookup Time      : %.4f ms\n" +
            "  Average Computation Time : %.2f ms\n" +
            "=========================================",
            capacity,
            getCurrentSize(),
            total,
            getHits(),
            getMisses(),
            getHitRatio() * 100,
            getMissRatio() * 100,
            getEvictionCount(),
            getAverageLookupTimeMillis(),
            getAverageComputationTimeMillis()
        );
    }
}
