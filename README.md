# 🚀 Bounded LRU Cache – Performance Showcase Dashboard

A **generic, thread-safe, bounded Least Recently Used (LRU) Cache** implemented in **Java 21**, paired with a modern web dashboard built using **HTML, Tailwind CSS v4, and Vanilla JavaScript**.

This project demonstrates how caching significantly improves application performance by eliminating redundant computations. It provides real-time cache visualization, performance metrics, LRU eviction tracking, and concurrency support through an interactive dashboard.

---

# ✨ Features

### Backend

* Generic LRU Cache implementation using `LinkedHashMap`
* Access-order based eviction
* Configurable cache capacity
* Automatic LRU eviction
* Thread-safe implementation using `ReentrantReadWriteLock`
* Performance benchmarking
* Cache hit and miss statistics
* Eviction tracking
* Simulated expensive computations
* Structured logging
* Automated unit tests
* Multi-threaded stress testing

### Frontend

* Modern responsive dashboard
* Live cache visualization (LRU → MRU)
* Performance statistics
* Cache Hit / Miss indicators
* Activity log
* Performance comparison
* Future enhancement showcase
* Real-world application showcase

---

# 🛠️ Technology Stack

## Backend

* Java 21
* Maven
* LinkedHashMap
* JUnit 5

## Frontend

* HTML5
* Tailwind CSS v4
* Vanilla JavaScript
* Fetch API
* Node.js (Static Server)

---

# 📁 Project Structure

```text
LRUCache/

├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/lrucache/
│       │   ├── Main.java
│       │   ├── StressTestDemo.java
│       │   ├── cache/
│       │   ├── concurrency/
│       │   ├── exception/
│       │   ├── service/
│       │   ├── statistics/
│       │   └── util/
│       └── test/java/com/lrucache/
│
└── frontend/
    ├── index.html
    ├── app.js
    ├── api.js
    ├── server.js
    └── package.json
```

---

# 🌐 Default Ports

| Service          | Port     |
| ---------------- | -------- |
| Frontend         | **5999** |
| Backend REST API | **6000** |

---

# ▶️ Running the Project

## 1. Start the Frontend

```bash
cd frontend
npm install
npm start
```

Open:

```
http://localhost:5999
```

The frontend starts in **Demo Mode**, allowing the dashboard to function even without the backend.

---

## 2. Start the Backend

```bash
cd backend
mvn clean install
mvn exec:java
```

The backend will start on:

```
http://localhost:6000
```

---

## 3. Run the Stress Test

```bash
mvn compile

java -cp target/classes com.lrucache.StressTestDemo
```

This launches concurrent requests to validate thread safety and cache consistency.

---

## 4. Run Unit Tests

```bash
mvn test
```

The tests verify:

* LRU ordering
* Capacity enforcement
* Cache eviction
* Thread safety
* Statistics correctness

---

# 🏗️ Architecture

## Backend

The backend follows a modular architecture with clear separation of responsibilities.

```
Client Request
        │
        ▼
Computation Service
        │
        ▼
 Thread-Safe Cache
        │
        ▼
LinkedHashMap (Access Order)
        │
        ▼
Statistics Engine
```

The cache uses Java's `LinkedHashMap` in **access-order mode**, enabling efficient O(1) average-time cache operations while automatically maintaining LRU ordering.

---

# 🔒 Thread Safety

Since accessing an entry in an access-ordered `LinkedHashMap` modifies its internal ordering, even a `get()` operation performs a structural update.

To ensure correctness under concurrent access, the cache is protected using a **fair ReentrantReadWriteLock**.

* **Write Lock**

  * `get()`
  * `put()`
  * `clear()`

* **Read Lock**

  * `containsKey()`
  * `size()`
  * `capacity()`

This prevents race conditions while allowing safe concurrent read operations where appropriate.

---

# 📊 Dashboard Highlights

The frontend provides:

* Live cache visualization
* Cache Hit / Miss indicators
* Execution time tracking
* LRU → MRU visualization
* Performance statistics
* Activity log
* Future enhancement roadmap
* Real-world applications

---

# 🎯 Hackathon Demonstration Flow

### Step 1

Start the frontend and backend.

Open:

```
http://localhost:5999
```

---

### Step 2

Enter:

```
25
```

Click **Compute**.

The first request performs an expensive computation.

Result:

* Cache MISS
* ~1 second execution time

---

### Step 3

Enter:

```
25
```

again.

Result:

* Cache HIT
* Instant response

This demonstrates the performance improvement achieved through caching.

---

### Step 4

Continue adding unique values until the cache reaches capacity.

Observe:

* Automatic LRU eviction
* Cache ordering updates
* Activity log notifications

---

### Step 5

Review the dashboard statistics.

Observe:

* Hit Ratio
* Miss Ratio
* Total Requests
* Evictions
* Estimated Time Saved

---

# 🌍 Real-World Applications

This caching mechanism can be integrated into:

* AI inference systems
* Database query optimization
* Weather services
* Stock market platforms
* Recommendation engines
* Web browsers
* E-commerce applications
* Banking systems
* Backend microservices

---

# 🚀 Future Enhancements

Potential extensions include:

* Redis integration
* Distributed caching
* Cache expiration (TTL)
* Multiple eviction policies (LRU, LFU, FIFO)
* Persistent cache storage
* Cloud deployment
* REST authentication
* Analytics dashboard
* Microservice integration

---

# 📈 Why LRU?

LRU is one of the most widely adopted cache replacement strategies because it assumes that recently accessed data is more likely to be requested again.

It provides:

* Efficient memory utilization
* Automatic eviction of stale entries
* O(1) average lookup and insertion
* Excellent performance for workloads exhibiting temporal locality

---

# 👥 Team

**Team Name:** *Exception Handlers*
