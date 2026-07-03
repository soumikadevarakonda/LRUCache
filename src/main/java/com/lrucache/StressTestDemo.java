package com.lrucache;

import com.lrucache.cache.Cache;
import com.lrucache.cache.CacheManager;
import com.lrucache.cache.LRUCache;
import com.lrucache.concurrency.ThreadSafeCache;
import com.lrucache.statistics.CacheStatistics;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Separate demonstration class implementing a high-concurrency stress test
 * using ExecutorService to verify cache correctness, thread safety, and
 * statistics integrity under heavy parallel access.
 */
public class StressTestDemo {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("       CONCURRENT CACHE STRESS TEST DEMO          ");
        System.out.println("==================================================\n");

        int capacity = 5;
        int threadCount = 10;
        int operationsPerThread = 2000;
        int keyRange = 10; // Access keys 1 to 10 to guarantee contention and evictions

        System.out.println("[INFO] Creating thread-safe cache with capacity: " + capacity);
        Cache<Integer, Integer> baseCache = new LRUCache<>(capacity);
        Cache<Integer, Integer> cache = new ThreadSafeCache<>(baseCache);

        // Silent eviction listener for stress test to avoid flooding console logs
        cache.setEvictionListener((key, value) -> {});

        // CacheManager using a fast zero-delay computation service (k -> k * k) with loggingEnabled = false
        CacheManager<Integer, Integer> manager = new CacheManager<>(cache, k -> k * k, false);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        System.out.printf("[INFO] Launching %d concurrent threads executing %d operations each...\n", 
                threadCount, operationsPerThread);
        System.out.printf("[INFO] Threads will compete for keys in range [1..%d]\n\n", keyRange);

        long startTime = System.nanoTime();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // wait for start signal
                    for (int j = 0; j < operationsPerThread; j++) {
                        // Spread requests across keys to trigger hits, misses, and evictions
                        int key = (threadId + j) % keyRange + 1;
                        manager.getOrCompute(key);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("[ERROR] Thread interrupted: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("[ERROR] Cache operation failed: " + e.getMessage());
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Release the start signal to execute threads concurrently
        startLatch.countDown();

        try {
            finishLatch.await(); // Wait for all threads to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[ERROR] Stress test interrupted: " + e.getMessage());
        }

        long endTime = System.nanoTime();
        executor.shutdown();

        double durationMillis = (double) (endTime - startTime) / 1_000_000.0;

        System.out.println("==================================================");
        System.out.println("             STRESS TEST COMPLETE                 ");
        System.out.println("==================================================");
        System.out.printf("  Total Threads           : %d\n", threadCount);
        System.out.printf("  Total Operations Run    : %d\n", threadCount * operationsPerThread);
        System.out.printf("  Total Duration          : %.2f ms\n", durationMillis);
        System.out.printf("  Final Cache Size        : %d (Capacity: %d)\n", cache.size(), capacity);
        System.out.println("  Final Cache Contents    : " + cache.getContents());
        System.out.println("==================================================\n");

        System.out.println("Final Statistics Report:");
        CacheStatistics stats = manager.getStatistics();
        System.out.println(stats);
        System.out.println();

        System.out.println("==================================================");
        System.out.println("            CONCURRENCY VERIFICATION              ");
        System.out.println("==================================================");
        // Verify size limit integrity
        if (cache.size() <= capacity) {
            System.out.println("  [PASS] Cache capacity limit respected successfully.");
        } else {
            System.out.println("  [FAIL] Cache capacity exceeded!");
        }

        // Verify thread safety consistency: Total operations = hits + misses
        long totalOperations = threadCount * operationsPerThread;
        if (stats.getTotalRequests() == totalOperations) {
            System.out.println("  [PASS] All requests successfully recorded (no race conditions on statistics).");
        } else {
            System.out.printf("  [FAIL] Expected %d requests but statistics recorded %d\n", 
                    totalOperations, stats.getTotalRequests());
        }
        System.out.println("==================================================");
    }
}
