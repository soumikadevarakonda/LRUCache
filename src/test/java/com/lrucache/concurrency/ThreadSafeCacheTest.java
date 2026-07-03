package com.lrucache.concurrency;

import com.lrucache.cache.LRUCache;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Concurrency tests to verify thread safety of ThreadSafeCache.
 */
public class ThreadSafeCacheTest {

    @Test
    void testConcurrentPutAndGet() throws InterruptedException {
        int capacity = 10;
        ThreadSafeCache<Integer, String> cache = new ThreadSafeCache<>(new LRUCache<>(capacity));

        int threadCount = 20;
        int operationsPerThread = 500;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < operationsPerThread; j++) {
                        int key = (threadId * operationsPerThread) + j;
                        cache.put(key, "Value-" + key);
                        cache.get(key);
                    }
                } catch (Exception e) {
                    fail("Concurrent operation failed with exception: " + e.getMessage());
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        finishLatch.await();
        executor.shutdown();

        // Under LRU eviction, capacity must be respected and not overflowed
        assertTrue(cache.size() <= capacity, "Cache size exceeded capacity: " + cache.size());
    }

    @Test
    void testConcurrentReadLockSafety() throws InterruptedException {
        // Test that multiple threads can query containsKey and size concurrently while others put
        int capacity = 5;
        ThreadSafeCache<Integer, String> cache = new ThreadSafeCache<>(new LRUCache<>(capacity));
        cache.put(1, "One");

        int writerCount = 2;
        int readerCount = 8;
        ExecutorService executor = Executors.newFixedThreadPool(writerCount + readerCount);
        CountDownLatch finishLatch = new CountDownLatch(writerCount + readerCount);

        // Readers do not modify LRU order because containsKey uses Read Lock
        for (int i = 0; i < readerCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 1000; j++) {
                        cache.containsKey(1);
                        cache.size();
                    }
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Writers write keys
        for (int i = 0; i < writerCount; i++) {
            final int writerId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 500; j++) {
                        cache.put(writerId * 1000 + j, "Value");
                    }
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        finishLatch.await();
        executor.shutdown();

        assertTrue(cache.size() <= capacity);
    }
}
