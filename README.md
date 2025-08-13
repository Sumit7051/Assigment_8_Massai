# Insurance Claim Recovery System

## Overview

This is a robust, concurrent recovery system for processing insurance claims from a backlog after an outage. The design guarantees:
- Per-policy serial processing (claims for the same policy are handled in submission order, never concurrently)
- Priority for urgent claims (global urgent-first, with starvation avoidance)
- Idempotent, once-only processing per claim ID (even with retries/duplicates)
- External check with bounded retries & timeouts
- Real-time fraud monitoring & automatic throttling
- Atomic audit logging (no interleaved lines)
- Graceful shutdown with accurate summary report

## Design Decisions

### Per-Policy Ordering

- **Approach:** Each policy has its own FIFO queue and a dedicated policy lock. Workers acquire per-policy locks before processing a claim, ensuring claims of the same policy are processed serially and in order.
- **Safety:** Locks are always acquired in a consistent order (single key per claim), preventing deadlock. No global lock is used, so workers can concurrently process claims for different policies.

### Deadlock Avoidance

- **Strategy:** Only one policy lock is acquired per claim at a time. There is no lock chaining, and locks are always acquired for the relevant policy before processing. If future enhancements require multiple locks, they should be acquired in sorted order.
- **Pitfall Detection:** If policy locks are acquired in varying orders across code paths, deadlocks are possible, but the current code always locks on one policy at a time.

### Priority Preemption & Starvation Avoidance

- **Global Queues:** Two global queues (urgent and normal) are maintained. Workers always prefer urgent claims unless the policy is busy.
- **Starvation Avoidance:** Normal claims are not starved; if no urgent claim for a free policy is available, normal claims proceed.

### Idempotency

- **Tracker:** A thread-safe set tracks processed ClaimIDs. Only the first encounter is processed; duplicates are marked and ignored.

### Retry, Timeout, & Order Preservation

- **Retry:** If an external check fails transiently or times out, the claim is re-queued at the tail of its policy queue, preserving order.
- **Timeout:** External checks are run in a separate thread with a timeout. Permanent failures mark the claim as rejected.

### Fraud Detection & Throttling

- **Monitor:** A background thread tracks suspicious claims in a sliding window. If the threshold is exceeded, intake is paused for 2 seconds.
- **Pause/Resume:** Pause is managed via a lock and flag; no deadlocks or data loss occur. Intake resumes automatically.

### Logging

- **Atomicity:** Audit logs are appended using a thread-safe lock, one line per event.
- **Console Output:** Suspicious claims are printed immediately.

### Graceful Shutdown

- **Signal Handling:** On shutdown, intake stops, workers finish in-flight claims, then summary and audit logs are written.

### Configuration

- All tunable parameters (worker count, backlog, timeouts, retry limit, fraud window/threshold) are in `config.properties`.

## Performance

See `performance.txt` for timing comparison. With 8 workers, 500 claims complete much faster than single-threaded baseline.

## How to Run

1. Place your `claims.csv` (at least 200 lines, with duplicates and various priorities) in the working directory.
2. Optionally edit `config.properties` for desired configuration.
3. Build and run:
   ```
   javac -d out src/main/java/com/claims/**/*.java
   java -cp out com.claims.Main config.properties
   ```
4. Outputs:
    - `audit.log` (all claim events, atomic)
    - `summary.txt` (final report)

## Trade-offs

- Per-policy locking is scalable and avoids global serialization.
- Priority preemption is fair but does not guarantee urgent claim latency if the policy is busy.
- Idempotency is handled in-memory for simplicity; a persistent solution may be needed in production.

## Package Structure

- `com.claims.model`: Claim classes
- `com.claims`: Core logic, concurrency, logging, monitoring

## Pitfalls Avoided

- No deadlocks: locks acquired consistently, only per-policy
- No global lock: concurrency is maximized
- No double-processing: idempotency tracker
- No starvation: urgent/normal queues, fair scheduling

## Files Provided

- `src/main/java/com/claims/...` (source code)
- `config.properties` (configuration)
- `claims.csv` (sample dataset)
- `audit.log` (sample run output)
- `summary.txt` (sample run output)
- `performance.txt` (timing comparison)

## Contact

Author: [Your Name]