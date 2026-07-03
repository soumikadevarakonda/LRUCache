package com.lrucache.statistics;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/**
 * Thread-safe class tracking Cache statistics and performance metrics.
 */
public class CacheStatistics {
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    private final AtomicLong totalLookupTimeNanos = new AtomicLong(0);
    private final AtomicLong totalComputationTimeNanos = new AtomicLong(0);
    private final AtomicLong totalHitTimeNanos = new AtomicLong(0);
    private final AtomicLong totalMissTimeNanos = new AtomicLong(0);

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
     * Records a cache hit, adding to hits, total lookup duration, and total hit time.
     *
     * @param lookupTimeNanos time taken for the lookup in nanoseconds
     */
    public void recordHit(long lookupTimeNanos) {
        hits.incrementAndGet();
        totalLookupTimeNanos.addAndGet(lookupTimeNanos);
        totalHitTimeNanos.addAndGet(lookupTimeNanos);
    }

    /**
     * Records a cache miss, adding to misses, total lookup, computation, and total miss time.
     *
     * @param lookupTimeNanos      time taken for the initial lookup in nanoseconds
     * @param computationTimeNanos time taken for computation in nanoseconds
     */
    public void recordMiss(long lookupTimeNanos, long computationTimeNanos) {
        misses.incrementAndGet();
        totalLookupTimeNanos.addAndGet(lookupTimeNanos);
        totalComputationTimeNanos.addAndGet(computationTimeNanos);
        totalMissTimeNanos.addAndGet(lookupTimeNanos + computationTimeNanos);
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

    public double getAverageHitTimeMillis() {
        long hitCount = hits.get();
        return hitCount == 0 ? 0.0 : (double) totalHitTimeNanos.get() / hitCount / 1_000_000.0;
    }

    public double getAverageMissTimeMillis() {
        long missCount = misses.get();
        return missCount == 0 ? 0.0 : (double) totalMissTimeNanos.get() / missCount / 1_000_000.0;
    }

    public double getTotalExecutionTimeMillis() {
        return (double) (totalHitTimeNanos.get() + totalMissTimeNanos.get()) / 1_000_000.0;
    }

    public double getEstimatedTimeSavedMillis() {
        double avgMiss = getAverageMissTimeMillis();
        double avgHit = getAverageHitTimeMillis();
        long hitCount = hits.get();
        return Math.max(0.0, (avgMiss - avgHit) * hitCount);
    }

    public double getPerformanceImprovementPercentage() {
        double avgMiss = getAverageMissTimeMillis();
        double avgHit = getAverageHitTimeMillis();
        if (avgMiss == 0.0) return 0.0;
        return ((avgMiss - avgHit) / avgMiss) * 100.0;
    }

    public double getSpeedupFactor() {
        double avgMiss = getAverageMissTimeMillis();
        double avgHit = getAverageHitTimeMillis();
        if (avgHit == 0.0) return avgMiss > 0 ? Double.POSITIVE_INFINITY : 1.0;
        return avgMiss / avgHit;
    }

    @Override
    public String toString() {
        long total = getTotalRequests();
        return String.format(
            "==================================================\n" +
            "                CACHE STATISTICS                  \n" +
            "==================================================\n" +
            "  Maximum Capacity         : %d\n" +
            "  Current Cache Size       : %d\n" +
            "  Total Requests           : %d\n" +
            "  Cache Hits               : %d\n" +
            "  Cache Misses             : %d\n" +
            "  Hit Ratio                : %.2f%%\n" +
            "  Miss Ratio               : %.2f%%\n" +
            "  Number of Evictions      : %d\n" +
            "  Average Cache Lookup Time: %.4f ms\n" +
            "  Average Computation Time : %.2f ms\n" +
            "  Average Hit Time         : %.4f ms\n" +
            "  Average Miss Time        : %.2f ms\n" +
            "  Total Execution Time     : %.2f ms\n" +
            "  Estimated Time Saved     : %.2f ms\n" +
            "  Performance Improvement  : %.2f%% (%.1fx speedup)\n" +
            "==================================================",
            capacity,
            getCurrentSize(),
            total,
            getHits(),
            getMisses(),
            getHitRatio() * 100,
            getMissRatio() * 100,
            getEvictionCount(),
            getAverageLookupTimeMillis(),
            getAverageComputationTimeMillis(),
            getAverageHitTimeMillis(),
            getAverageMissTimeMillis(),
            getTotalExecutionTimeMillis(),
            getEstimatedTimeSavedMillis(),
            getPerformanceImprovementPercentage(),
            getSpeedupFactor()
        );
    }
}
