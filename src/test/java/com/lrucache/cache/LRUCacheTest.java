package com.lrucache.cache;

import com.lrucache.exception.InvalidCapacityException;
import com.lrucache.exception.NullKeyException;
import com.lrucache.exception.NullValueException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the LRUCache implementation.
 */
public class LRUCacheTest {
    private LRUCache<Integer, String> cache;

    @BeforeEach
    void setUp() {
        cache = new LRUCache<>(3);
    }

    @Test
    void testBasicOperations() {
        cache.put(1, "One");
        cache.put(2, "Two");

        assertEquals(2, cache.size());
        assertEquals("One", cache.get(1));
        assertEquals("Two", cache.get(2));
        assertTrue(cache.containsKey(1));
        assertFalse(cache.containsKey(3));
    }

    @Test
    void testCapacityValidation() {
        assertThrows(InvalidCapacityException.class, () -> new LRUCache<>(0));
        assertThrows(InvalidCapacityException.class, () -> new LRUCache<>(-5));
    }

    @Test
    void testNullValidation() {
        assertThrows(NullKeyException.class, () -> cache.put(null, "Value"));
        assertThrows(NullKeyException.class, () -> cache.get(null));
        assertThrows(NullKeyException.class, () -> cache.containsKey(null));
        assertThrows(NullValueException.class, () -> cache.put(1, null));
    }

    @Test
    void testLRUEviction() {
        cache.put(1, "One");
        cache.put(2, "Two");
        cache.put(3, "Three");

        // Access 1 to make it most recently used. Current order: 2, 3, 1
        assertEquals("One", cache.get(1));

        // Putting 4 should evict 2 (eldest)
        cache.put(4, "Four");

        assertFalse(cache.containsKey(2));
        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
        assertEquals(1, cache.getEvictionCount());
    }

    @Test
    void testEvictionListener() {
        final Integer[] evictedKey = new java.lang.Integer[1];
        final String[] evictedVal = new String[1];
        cache.setEvictionListener((k, v) -> {
            evictedKey[0] = k;
            evictedVal[0] = v;
        });

        cache.put(1, "One");
        cache.put(2, "Two");
        cache.put(3, "Three");
        cache.put(4, "Four"); // evicts 1

        assertEquals(1, evictedKey[0]);
        assertEquals("One", evictedVal[0]);
        assertEquals(1, cache.getEvictionCount());
    }

    @Test
    void testClear() {
        cache.put(1, "One");
        cache.put(2, "Two");
        cache.clear();

        assertEquals(0, cache.size());
        assertNull(cache.get(1));
    }

    @Test
    void testGetContentsCopy() {
        cache.put(1, "One");
        cache.put(2, "Two");

        Map<Integer, String> contents = cache.getContents();
        assertEquals(2, contents.size());
        assertEquals("One", contents.get(1));

        // Ensure modifications to the map copy do not affect cache
        contents.put(3, "Three");
        assertEquals(2, cache.size());
        assertFalse(cache.containsKey(3));
    }
}
