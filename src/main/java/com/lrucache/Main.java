package com.lrucache;

import com.lrucache.cache.Cache;
import com.lrucache.cache.CacheManager;
import com.lrucache.cache.LRUCache;
import com.lrucache.concurrency.ThreadSafeCache;
import com.lrucache.service.SquareComputationService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main application class to demonstrate the Bounded LRU Cache.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   Bounded LRU Cache Console Demo        ");
        System.out.println("=========================================\n");

        // 1. Creating Cache...
        System.out.println("Creating Cache...");
        int capacity = 5;
        System.out.println("Capacity : " + capacity);
        System.out.println();

        // Instantiate the cache
        Cache<Integer, Integer> baseCache = new LRUCache<>(capacity);
        Cache<Integer, Integer> cache = new ThreadSafeCache<>(baseCache);

        // Register eviction listener to track evictions
        cache.setEvictionListener((key, value) -> {
            System.out.println("Capacity exceeded.");
            System.out.println("Evicted : " + key);
        });

        // Initialize Computation Service (squares values with 1-second delay)
        SquareComputationService squareService = new SquareComputationService();

        // Initialize Cache Manager
        CacheManager<Integer, Integer> manager = new CacheManager<>(cache, squareService);

        // 2. Demonstration sequence
        try {
            // Compute 25
            System.out.println("Computing 25...");
            manager.getOrCompute(25);
            System.out.println();

            // Compute 30
            System.out.println("Computing 30...");
            manager.getOrCompute(30);
            System.out.println();

            // Compute 25 (HIT)
            System.out.println("Computing 25...");
            manager.getOrCompute(25);
            System.out.println();

            // Adding more elements to trigger eviction
            System.out.println("Adding more elements...");
            
            System.out.println("Computing 10...");
            manager.getOrCompute(10);
            System.out.println();

            System.out.println("Computing 20...");
            manager.getOrCompute(20);
            System.out.println();

            System.out.println("Computing 40...");
            manager.getOrCompute(40);
            System.out.println();

            System.out.println("Computing 50...");
            manager.getOrCompute(50);
            System.out.println();

            // 3. Show current cache and statistics
            System.out.println("Current Cache :");
            System.out.println(cache.getContents());
            System.out.println();

            System.out.println("Statistics :");
            System.out.println(manager.getStatistics());
            System.out.println();

            // 4. Concurrency demonstration
            runConcurrencyTest();

        } catch (Exception e) {
            System.err.println("An unexpected error occurred during demo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Runs a multi-threaded high-throughput concurrency test using a zero-delay
     * computation lambda. This verifies thread-safety, eviction consistency,
     * and correctness of statistics under concurrent access.
     */
    private static void runConcurrencyTest() throws InterruptedException {
        System.out.println("\n=========================================");
        System.out.println("      CONCURRENCY & THREAD-SAFETY DEMO   ");
        System.out.println("=========================================\n");

        int threadCount = 10;
        int tasksPerThread = 1000;
        int concurrentCapacity = 3;

        System.out.println("Creating concurrent cache with capacity: " + concurrentCapacity);
        Cache<Integer, Integer> concurrentCache = new ThreadSafeCache<>(new LRUCache<>(concurrentCapacity));
        
        // Zero-delay calculation service for concurrency test (to run fast but intensive)
        CacheManager<Integer, Integer> manager = new CacheManager<>(concurrentCache, k -> k * k);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        System.out.printf("Spawning %d threads performing %d requests each...\n", threadCount, tasksPerThread);
        System.out.println("All threads will request values for keys in range [1..5] concurrently.");

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // wait for latch to start simultaneously
                    for (int j = 0; j < tasksPerThread; j++) {
                        // Key range 1..5 ensures hits, misses, and evictions occur
                        int key = (threadId + j) % 5 + 1;
                        manager.getOrCompute(key);
                    }
                } catch (Exception e) {
                    System.err.println("Thread execution error: " + e.getMessage());
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        long startTime = System.nanoTime();
        startLatch.countDown(); // release latch to start threads
        finishLatch.await();    // wait for threads to finish
        long endTime = System.nanoTime();

        executor.shutdown();

        System.out.println("\nConcurrent execution completed.");
        System.out.printf("Total duration: %.2f ms\n", (double) (endTime - startTime) / 1_000_000.0);
        System.out.println("Final Cache Size: " + concurrentCache.size() + " (Capacity: " + concurrentCapacity + ")");
        System.out.println("Final Cache Contents: " + concurrentCache.getContents());
        System.out.println("\nStatistics :");
        System.out.println(manager.getStatistics());
        System.out.println("=========================================");
    }
}
