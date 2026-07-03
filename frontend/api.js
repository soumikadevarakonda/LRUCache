// api.js

const BACKEND_PORT = 6000;
const BASE_URL = `http://localhost:${BACKEND_PORT}/api/cache`;

// Client-Side LRU Simulator (Demo Mode / Offline Fallback)
class LocalLRUCacheSimulator {
    constructor(capacity = 5) {
        this.capacity = capacity;
        this.cache = new Map(); // JS Map preserves insertion order
        this.evictionListener = null;
        
        // Statistics
        this.requests = 0;
        this.hits = 0;
        this.misses = 0;
        this.evictions = 0;
        this.totalLookupTimeMs = 0;
        this.totalComputationTimeMs = 0;
        this.totalHitTimeMs = 0;
        this.totalMissTimeMs = 0;
    }

    setEvictionListener(listener) {
        this.evictionListener = listener;
    }

    get(key) {
        if (this.cache.has(key)) {
            const value = this.cache.get(key);
            // Move to tail (MRU)
            this.cache.delete(key);
            this.cache.set(key, value);
            return value;
        }
        return null;
    }

    put(key, value) {
        if (this.cache.has(key)) {
            this.cache.delete(key);
        } else if (this.cache.size >= this.capacity) {
            // Evict eldest (first entry in Map iterator)
            const eldestKey = this.cache.keys().next().value;
            const eldestValue = this.cache.get(eldestKey);
            this.cache.delete(eldestKey);
            this.evictions++;
            if (this.evictionListener) {
                this.evictionListener(eldestKey, eldestValue);
            }
        }
        this.cache.set(key, value);
    }

    containsKey(key) {
        return this.cache.has(key);
    }

    clear() {
        this.cache.clear();
    }

    resetStats() {
        this.requests = 0;
        this.hits = 0;
        this.misses = 0;
        this.evictions = 0;
        this.totalLookupTimeMs = 0;
        this.totalComputationTimeMs = 0;
        this.totalHitTimeMs = 0;
        this.totalMissTimeMs = 0;
    }

    async compute(key) {
        this.requests++;
        const lookupStart = performance.now();
        const cachedValue = this.get(key);
        const lookupEnd = performance.now();
        const lookupTime = lookupEnd - lookupStart;

        if (cachedValue !== null) {
            this.hits++;
            this.totalLookupTimeMs += lookupTime;
            this.totalHitTimeMs += lookupTime;
            return {
                input: key,
                result: cachedValue,
                status: 'HIT',
                executionTimeMs: parseFloat(lookupTime.toFixed(2))
            };
        }

        this.misses++;
        this.totalLookupTimeMs += lookupTime;
        
        const compStart = performance.now();
        // Simulate heavy computation delay (1000ms)
        await new Promise(resolve => setTimeout(resolve, 1000));
        const result = key * key;
        const compEnd = performance.now();
        const compTime = compEnd - compStart;
        
        this.totalComputationTimeMs += compTime;
        this.totalMissTimeMs += (lookupTime + compTime);
        
        this.put(key, result);
        
        return {
            input: key,
            result: result,
            status: 'MISS',
            executionTimeMs: parseFloat((lookupTime + compTime).toFixed(2))
        };
    }

    getContents() {
        // Return contents as an array of [key, value] pairs (LRU to MRU order)
        return Array.from(this.cache.entries()).map(([key, value]) => ({ key, value }));
    }

    getStatistics() {
        const total = this.requests;
        const hitRatio = total === 0 ? 0 : (this.hits / total) * 100;
        const avgLookup = total === 0 ? 0 : this.totalLookupTimeMs / total;
        const avgComp = this.misses === 0 ? 0 : this.totalComputationTimeMs / this.misses;
        const avgHit = this.hits === 0 ? 0 : this.totalHitTimeMs / this.hits;
        const avgMiss = this.misses === 0 ? 0 : this.totalMissTimeMs / this.misses;

        return {
            capacity: this.capacity,
            currentSize: this.cache.size,
            totalRequests: total,
            hits: this.hits,
            misses: this.misses,
            hitRatio: parseFloat(hitRatio.toFixed(1)),
            missRatio: parseFloat((total === 0 ? 0 : 100 - hitRatio).toFixed(1)),
            evictions: this.evictions,
            averageLookupTimeMs: parseFloat(avgLookup.toFixed(3)),
            averageComputationTimeMs: parseFloat(avgComp.toFixed(1)),
            averageHitTimeMs: parseFloat(avgHit.toFixed(3)),
            averageMissTimeMs: parseFloat(avgMiss.toFixed(1)),
            totalExecutionTimeMs: parseFloat((this.totalHitTimeMs + this.totalMissTimeMs).toFixed(1)),
            estimatedTimeSavedMs: parseFloat((Math.max(0, avgMiss - avgHit) * this.hits).toFixed(1))
        };
    }
}

// Global local cache instance
const localCache = new LocalLRUCacheSimulator(5);

// API Service Interface (Handles Mode routing: Demo Mode vs Live Backend Connection)
const ApiService = {
    mode: 'demo', // 'demo' or 'live'

    setMode(mode) {
        this.mode = mode;
    },

    getMode() {
        return this.mode;
    },

    setEvictionListener(listener) {
        localCache.setEvictionListener(listener);
    },

    // Check if the live backend is reachable
    async checkConnection() {
        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 1200); // 1.2s timeout
            
            const response = await fetch(`${BASE_URL}/status`, { 
                method: 'GET',
                signal: controller.signal 
            });
            clearTimeout(timeoutId);
            return response.ok;
        } catch (e) {
            return false;
        }
    },

    // Compute value
    async compute(number) {
        if (this.mode === 'demo') {
            return await localCache.compute(number);
        }

        // Live connection
        const response = await fetch(`${BASE_URL}/compute`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ number: parseInt(number) })
        });
        if (!response.ok) {
            throw new Error(`Failed to compute value: ${response.statusText}`);
        }
        return await response.json();
    },

    // Get cache contents
    async getContents() {
        if (this.mode === 'demo') {
            return localCache.getContents();
        }

        const response = await fetch(`${BASE_URL}/contents`);
        if (!response.ok) {
            throw new Error(`Failed to get cache contents: ${response.statusText}`);
        }
        return await response.json(); // Returns [{key, value}]
    },

    // Get statistics
    async getStatistics() {
        if (this.mode === 'demo') {
            return localCache.getStatistics();
        }

        const response = await fetch(`${BASE_URL}/statistics`);
        if (!response.ok) {
            throw new Error(`Failed to get statistics: ${response.statusText}`);
        }
        return await response.json();
    },

    // Clear cache
    async clear() {
        if (this.mode === 'demo') {
            localCache.clear();
            return;
        }

        const response = await fetch(`${BASE_URL}/clear`, { method: 'POST' });
        if (!response.ok) {
            throw new Error(`Failed to clear cache: ${response.statusText}`);
        }
    },

    // Reset statistics
    async resetStats() {
        if (this.mode === 'demo') {
            localCache.resetStats();
            return;
        }

        const response = await fetch(`${BASE_URL}/reset-statistics`, { method: 'POST' });
        if (!response.ok) {
            throw new Error(`Failed to reset statistics: ${response.statusText}`);
        }
    }
};

window.ApiService = ApiService;
