package com.lrucache;

import com.lrucache.cache.Cache;
import com.lrucache.cache.CacheManager;
import com.lrucache.cache.LRUCache;
import com.lrucache.concurrency.ThreadSafeCache;
import com.lrucache.service.SquareComputationService;
import com.lrucache.statistics.CacheStatistics;

/**
 * Main class executing the single-threaded demonstration scenario for the Bounded LRU Cache.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("        LRU CACHE DEMONSTRATION SCENARIO          ");
        System.out.println("==================================================\n");

        try {
            int capacity = 5;
            Cache<Integer, Integer> baseCache = new LRUCache<>(capacity);
            Cache<Integer, Integer> cache = new ThreadSafeCache<>(baseCache);
            SquareComputationService squareService = new SquareComputationService();
            CacheManager<Integer, Integer> manager = new CacheManager<>(cache, squareService);

            // --- PHASE 1: Populate Cache ---
            System.out.println("--- PHASE 1: Populate Cache (Capacity = 5) ---");
            manager.getOrCompute(10);
            manager.getOrCompute(20);
            manager.getOrCompute(30);
            manager.getOrCompute(40);
            manager.getOrCompute(50);

            // --- PHASE 2: Trigger Cache Hits (Order Changes) ---
            System.out.println("--- PHASE 2: Trigger Cache Hits & Order Changes ---");
            System.out.println("Accessing 20 (Should HIT and move 20 to MRU)...");
            manager.getOrCompute(20);
            
            System.out.println("Accessing 10 (Should HIT and move 10 to MRU)...");
            manager.getOrCompute(10);

            // --- PHASE 3: Trigger Eviction (Capacity Exceeded) ---
            System.out.println("--- PHASE 3: Trigger Eviction (Capacity Exceeded) ---");
            System.out.println("Computing 60... (Cache is full, should evict 30 which is now the eldest)...");
            manager.getOrCompute(60);

            // --- FINAL STATISTICS ---
            System.out.println("--- DEMONSTRATION COMPLETE: STATISTICS ---");
            CacheStatistics stats = manager.getStatistics();
            System.out.println(stats);
            System.out.println();

            System.out.println("==================================================");
            System.out.println("             PERFORMANCE SUMMARY                  ");
            System.out.println("==================================================");
            System.out.printf("  Average Miss Time       : %.2f ms\n", stats.getAverageMissTimeMillis());
            System.out.printf("  Average Hit Time        : %.4f ms\n", stats.getAverageHitTimeMillis());
            System.out.printf("  Performance Improvement : %.2f%% (%.1fx faster)\n", 
                stats.getPerformanceImprovementPercentage(), stats.getSpeedupFactor());
            System.out.println("==================================================\n");

        } catch (IllegalArgumentException e) {
            System.err.println("[ERROR] Invalid argument: " + e.getMessage());
        } catch (com.lrucache.exception.CacheException e) {
            System.err.println("[ERROR] Cache operation failed: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected failure: " + e.getMessage());
        }
    }
}
