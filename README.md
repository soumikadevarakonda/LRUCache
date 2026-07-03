# Bounded LRU Cache: Hackathon Showcase Dashboard

A generic, thread-safe, bounded **Least Recently Used (LRU) Cache** backend written in Java 21, coupled with an interactive web dashboard frontend styled with **Tailwind CSS v4** and Vanilla JavaScript.

This project is built to demonstrate the massive performance benefits of caching computationally-heavy operations, complete with live visual pipelines, statistics tracking, and concurrency safety.

---

## 🚀 Port Allocations
*   **Web Frontend**: Port `5999` (`http://localhost:5999`)
*   **Java REST Backend**: Port `6000` (`http://localhost:6000`)

---

## 📁 Directory Structure

The project is split cleanly into `/backend` and `/frontend` directories:

```text
LRUCache/
├── README.md                                    # Project instructions and documentation
├── backend/                                     # Complete Java Backend codebase
│   ├── pom.xml                                  # Maven project configuration (Java 21, JUnit 5)
│   └── src/
│       ├── main/java/com/lrucache/
│       │   ├── Main.java                        # Single-Threaded demonstration CLI
│       │   ├── StressTestDemo.java              # Multi-Threaded stress test CLI
│       │   ├── cache/                           # LRUCache, CacheEntry, CacheManager, EvictionListener
│       │   ├── concurrency/                     # ThreadSafeCache decorator (ReentrantReadWriteLock)
│       │   ├── exception/                       # Custom Cache exceptions
│       │   ├── service/                         # ComputationService interfaces and square services
│       │   ├── statistics/                      # CacheStatistics tracking hit/miss averages
│       │   └── util/                            # TimeUtil helper class
│       └── test/java/com/lrucache/
│           ├── cache/                           # LRUCacheTest functional checks
│           └── concurrency/                     # ThreadSafeCacheTest concurrency checks
│
└── frontend/                                    # Modern Frontend Web Dashboard
    ├── index.html                               # HTML5 layout using Tailwind CSS v4 Play CDN
    ├── api.js                                   # REST Fetch API interfaces & Client-side LRU Simulator
    ├── app.js                                   # Vanilla JS UI rendering and event loops
    ├── server.js                                # Dependency-free static node server (Port 5999)
    └── package.json                             # Start configuration (npm start runs server.js)
```

---

## 🛠️ Installation & Execution

### 1. Running the Web Frontend (Port 5999)
The frontend contains a dependency-free Node.js static web server. To start the dashboard:

1. Open your terminal and navigate to the `frontend/` directory:
   ```bash
   cd frontend
   ```
2. Start the local server:
   ```bash
   npm start
   ```
3. Open your browser and navigate to:
   ```text
   http://localhost:5999
   ```

*Note: By default, the dashboard boots in **Demo Mode (Local Cache)**, enabling you to test hits, misses, evictions, and activity logs immediately in the browser without having to start the Java server.*

---

### 2. Running the Java Backend (Port 6000)
The backend is a Maven-based project compiling on **Java 21**.

1. Navigate to the `backend/` directory:
   ```bash
   cd backend
   ```

2. **Run Single-Threaded CLI Demo**:
   Runs the structured sequence producing cache misses, cache hits, eviction limits, and performance summaries:
   ```bash
   mvn exec:java
   ```

3. **Run Concurrency Stress Test**:
   Launches 10 threads executing 20,000 requests concurrently to verify thread safety:
   ```bash
   # Build the classes first
   mvn compile
   # Execute StressTestDemo
   java -cp target/classes com.lrucache.StressTestDemo
   ```

4. **Run Automated Unit Tests**:
   Runs 9 automated unit tests verifying capacity constraints, LRU evictions, and parallel read/write locks:
   ```bash
   mvn test
   ```

---

## 🧠 Architectural Highlights

### The Concurrency Strategy
In an access-ordered `LinkedHashMap`, a read operation (`get()`) is structurally a write operation because it modifies the internal doubly-linked list to move the accessed node to the tail.
*   `ThreadSafeCache` resolves this by wrapping operations in a fair `ReentrantReadWriteLock`.
*   An exclusive **Write Lock** is acquired on `get()`, `put()`, and `clear()`.
*   A **Read Lock** is acquired on `containsKey()`, `size()`, and `capacity()`, allowing multiple threads to query checks concurrently without structural link corruption.

### Dual-Mode Frontend Web Architecture
*   **Demo Mode (Local Cache)**: Implements a complete LRU Cache simulation in Vanilla JS (`api.js`) utilizing JavaScript's `Map` insertion order. Toggling this mode lets you demonstrate the application immediately on any machine with zero network dependencies.
*   **Live Connection**: Integrates with the backend REST endpoints on **Port 6000** via Fetch API.

---

## 🎯 How to Demonstrate during a Hackathon

1.  **Open http://localhost:5999** with the dashboard in **Demo Mode (Local Cache)**.
2.  **Demonstrate Cache MISS**:
    *   Input `25` and click **Compute**.
    *   Observe a 1-second delay (simulating heavy database/network lookup).
    *   The Result Card will highlight a **Cache MISS** in orange, and the visual visualizer adds the card `25 -> 625` on the right (MRU).
3.  **Demonstrate Cache HIT**:
    *   Input `25` again and click **Compute**.
    *   The result returns **instantly (0ms)**.
    *   The Result Card highlights **Cache HIT** in green.
4.  **Demonstrate LRU Eviction**:
    *   Populate the cache with inputs `10`, `20`, `30`, `40` (exceeding capacity 5).
    *   Notice `25` is pushed left towards the **LRU** end.
    *   Access `25` again; watch it jump dynamically to the **MRU** end.
    *   Input a new value like `60`. The eldest item at the LRU end will be evicted.
    *   An eviction log alert (`[Evict] Key = ...`) is printed instantly in red in the **Activity Log**.
5.  **Show Performance Stats**:
    *   Highlight the **Performance Statistics** cards, pointing out the hit ratios and estimated time saved by avoiding redundant computation.
