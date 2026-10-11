# URL Shortener — Low-Level Design (LLD) & Machine Coding

A thread-safe, extensible Object-Oriented URL Shortener service written in pure Java. Designed for high read throughput, collision-free auto-generation, custom alias reservations, and access analytics with zero framework bloat.

---

## 1. Requirements & Scope

### Functional Requirements
- **URL Shortening:** Encodes long destination URLs into a compact 7-character Base62 short code ($\approx 3.5 \times 10^{12}$ unique possibilities).
- **Custom Aliases:** Supports custom slugs (4–16 characters, alphanumeric plus `_` and `-`), rejecting reserved keywords (`api`, `health`, `metrics`, `admin`, `static`) and existing duplicates.
- **Configurable TTL / Expiration:** Supports custom expiration timestamps or defaults to 2 years. Expired links are rejected upon resolution.
- **Redirection & Resolution:** $O(1)$ fast resolution from short code back to destination URL.
- **Access Tracking:** Automatically records total click count and last-accessed timestamp on resolution.
- **Validation & Security:** Rejects non-HTTP/HTTPS URLs, malformed syntax, and self-referential redirect loops.
- **Revocation:** Allows link creators to revoke active links.

### Non-Functional Requirements
- **Consistency & Uniqueness:** Strict uniqueness guarantee on short codes under concurrent write attempts.
- **Extensibility:** Swappable code-generation algorithms decoupled via the Strategy Pattern.
- **Thread Safety:** In-memory persistence engineered with concurrency primitives (`ConcurrentHashMap`, atomic operations) to avoid lost updates or race conditions.
- **Zero Framework Overhead:** Pure Java with standard library only—no Spring Boot or Maven/Gradle required.

---

## 2. Architecture & Class Design

The project follows a clean layered architecture adhering to SOLID principles:

```
[Client / Driver (Main)]
          │
          ▼
[UrlShortenerService] (Orchestrator)
    ├── [UrlValidator] (Strategy) ──► HttpUrlValidator
    ├── [CodeGenerationStrategy] (Strategy) ──► Base62CounterStrategy ──► [SequenceGenerator]
    └── [UrlRepository] (Data Access) ──► InMemoryUrlRepository (Thread-safe)
```

### Role-Based Component Breakdown

| Role | Class / Interface | Responsibility |
| :--- | :--- | :--- |
| **Entity** | `ShortUrl` | Manages link state (`ACTIVE`, `EXPIRED`, `REVOKED`), expiration logic, and thread-safe access counters. |
| **Enum** | `UrlStatus` | Lifecycle states: `ACTIVE`, `EXPIRED`, `REVOKED`. |
| **Strategy** | `CodeGenerationStrategy` | Interface for slug generation algorithms. |
| **Strategy Impl** | `Base62CounterStrategy` | Bijectively maps monotonic 64-bit sequence IDs into 7-character Base62 strings. |
| **Strategy** | `UrlValidator` | Interface for destination URL syntax and protocol verification. |
| **Strategy Impl** | `HttpUrlValidator` | Enforces HTTP/HTTPS protocols and prevents self-loop redirects. |
| **Repository** | `UrlRepository` | Interface for persistence and lookup. |
| **Repository Impl** | `InMemoryUrlRepository` | Backed by `ConcurrentHashMap` with atomic `putIfAbsent()` simulating a database `UNIQUE` constraint. |
| **Service** | `UrlShortenerService` | Coordinates validation, code generation, conflict detection, expiration, and lookup. |
| **DTOs** | `CreateUrlRequest`, `ShortUrlResponse`, `UrlStatsResponse` | Strongly-typed POJOs for input and output data. |
| **Exceptions** | `UrlShortenerException` hierarchy | Specific exceptions for `UrlNotFoundException`, `UrlExpiredException`, `InvalidUrlException`, `InvalidAliasException`, `AliasConflictException`. |

---

## 3. Key Engineering Highlights

### 1. Bijective Base62 vs. Truncated Hashing
* **Why not hashing (MD5 / SHA-256):** Truncating a hash to 7 characters introduces the Birthday Paradox, causing collisions as volume scales and requiring expensive retry loops.
* **Bijective Base62 Approach:** We map an auto-incrementing 64-bit sequence ID directly to Base62 (`[0-9a-zA-Z]`). Because the mapping is bijective (1-to-1), collisions are mathematically impossible during auto-generation.

### 2. Race Condition Prevention on Custom Aliases
* When two clients attempt to claim the custom alias `short.ly/summer-sale` simultaneously:
  1. Both may pass the initial existence check.
  2. The persistence layer enforces an atomic `storage.putIfAbsent(shortCode, entity)`.
  3. The first insert succeeds; the second insert fails atomically, throwing a `DuplicateKeyException` which the service translates into an `AliasConflictException`.

### 3. Java Standards
* **Explicit typing:** No `var` keyword; all types explicitly declared.
* **Standard POJOs:** Encapsulated classes with private `final` fields, explicit constructors, and standard getter methods.
* **Safe `Optional` Usage:** No `Optional` fields or parameter types; `Optional.ofNullable(...)` is strictly reserved for accessor return types.

---

## 4. Project Structure

```
url-shortener/
├── .gitignore
├── README.md
└── src/
    └── com/
        └── example/
            └── urlshortener/
                ├── Main.java                          # Executable demo driver
                ├── model/
                │   ├── ShortUrl.java                  # Core domain entity
                │   └── UrlStatus.java                 # Lifecycle state enum
                ├── dto/
                │   ├── CreateUrlRequest.java          # Creation DTO
                │   ├── ShortUrlResponse.java          # Short URL response DTO
                │   └── UrlStatsResponse.java          # Analytics response DTO
                ├── exception/
                │   ├── UrlShortenerException.java     # Base unchecked exception
                │   ├── UrlNotFoundException.java      # 404 Not Found equivalent
                │   ├── UrlExpiredException.java       # 410 Gone equivalent
                │   ├── InvalidUrlException.java       # 400 Bad Request URL syntax
                │   ├── InvalidAliasException.java     # 400 Bad Request alias syntax
                │   ├── AliasConflictException.java    # 409 Conflict duplicate alias
                │   └── DuplicateKeyException.java     # Persistence-level collision
                ├── strategy/
                │   ├── SequenceGenerator.java         # Sequence generator interface
                │   ├── AtomicSequenceGenerator.java   # Monotonic counter implementation
                │   ├── CodeGenerationStrategy.java    # Strategy interface
                │   └── Base62CounterStrategy.java     # Bijective Base62 algorithm
                ├── validator/
                │   ├── UrlValidator.java              # Validator interface
                │   └── HttpUrlValidator.java          # Protocol & self-loop check
                ├── repository/
                │   ├── UrlRepository.java             # Repository abstraction
                │   └── InMemoryUrlRepository.java     # Thread-safe in-memory store
                └── service/
                    └── UrlShortenerService.java       # Business orchestrator
```

---

## 5. How to Run

### Option A: From Terminal
Compile all source files and run `Main`:

```bash
# From the project root (url-shortener)
javac $(find src -name "*.java") -d out
java -cp out com.example.urlshortener.Main
```

### Option B: In IntelliJ IDEA
1. Open IntelliJ IDEA and open the `url-shortener` directory.
2. Mark `src` as the **Sources Root** (Right-click `src` $\rightarrow$ **Mark Directory as** $\rightarrow$ **Sources Root**).
3. Open `src/com/example/urlshortener/Main.java` and click **Run**.

---

## 6. Sample Output

```text
=== 1. Happy Path: Auto-generated Short URL ===
Original:  https://en.wikipedia.org/wiki/System_design
Short URL: https://short.ly/000FXsl
Code:      000FXsl

=== 2. Happy Path: Custom Alias ===
Custom Short URL: https://short.ly/google-guava

=== 3. Resolution & Access Tracking ===
Redirecting to: https://github.com/google/guava
Total Clicks:  2
Last Accessed: 2026-10-11T11:25:19.240527Z
Status:        ACTIVE

=== 4. Edge Cases: Validation and Conflicts ===
Caught expected invalid protocol error: Invalid URL protocol. Only HTTP/HTTPS supported: ftp://invalid-url.com
Caught expected duplicate alias conflict: Alias already in use: google-guava
```
