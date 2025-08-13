package com.claims;

import com.claims.model.Claim;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;


public class PolicySerialScheduler {
    private final ClaimBacklog backlog;
    private final ClaimIdempotencyTracker idempotencyTracker;
    private final AuditLogger auditLogger;
    private final SummaryReport summaryReport;
    private final Config config;

    // Track locks for each policy
    private final ConcurrentHashMap<String, ReentrantLock> policyLocks = new ConcurrentHashMap<>();
    // Set of currently active policies
    private final Set<String> lockedPolicies = ConcurrentHashMap.newKeySet();

    private final ExecutorService pool;
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final CountDownLatch finishLatch;

    public PolicySerialScheduler(ClaimBacklog backlog, ClaimIdempotencyTracker idempotencyTracker,
                                 AuditLogger auditLogger, SummaryReport summaryReport, Config config) {
        this.backlog = backlog;
        this.idempotencyTracker = idempotencyTracker;
        this.auditLogger = auditLogger;
        this.summaryReport = summaryReport;
        this.config = config;
        this.pool = Executors.newFixedThreadPool(config.getWorkerCount());
        this.finishLatch = new CountDownLatch(config.getWorkerCount());
    }

    public void startWorkers() {
        for (int i = 0; i < config.getWorkerCount(); i++) {
            pool.submit(new Worker("Worker-" + (i + 1)));
        }
    }

    public void shutdown() {
        shutdown.set(true);
        pool.shutdown();
    }

    public void awaitFinish() throws InterruptedException {
        finishLatch.await();
    }

    class Worker implements Runnable {
        private final String name;

        Worker(String name) { this.name = name; }

        @Override
        public void run() {
            try {
                while (!shutdown.get() || backlog.size() > 0) {
                    Claim claim = backlog.pollNext(lockedPolicies);
                    if (claim == null) {
                        Thread.sleep(25); // idle
                        continue;
                    }
                    // Per-policy locking
                    ReentrantLock lock = policyLocks.computeIfAbsent(claim.policyNumber, k -> new ReentrantLock());
                    // To avoid deadlock, always lock on sorted policy key if ever claiming multiple locks
                    lock.lock();
                    try {
                        lockedPolicies.add(claim.policyNumber);
                        processClaim(claim);
                    } finally {
                        lockedPolicies.remove(claim.policyNumber);
                        lock.unlock();
                    }
                }
            } catch (InterruptedException ignored) {
            } finally {
                finishLatch.countDown();
            }
        }

        private void processClaim(Claim claim) {
            // Idempotency: skip if already processed
            if (!idempotencyTracker.markProcessing(claim.claimId)) {
                auditLogger.logEvent(claim, name, claim.status.name(), "DUPLICATE", claim.attempt);
                return;
            }
            claim.attempt++;
            claim.status = Claim.Status.IN_PROGRESS;
            auditLogger.logEvent(claim, name, "NEW", "IN_PROGRESS", claim.attempt);

            // Simulate external check with timeout/retries
            boolean processed = false;
            for (int attempt = claim.attempt; attempt <= config.getRetryLimit(); attempt++) {
                ExternalCheck.Result result = ExternalCheck.check(claim, config.getExternalCheckTimeoutMs());
                if (result == ExternalCheck.Result.SUCCESS) {
                    claim.status = Claim.Status.APPROVED;
                    processed = true;
                    auditLogger.logEvent(claim, name, "IN_PROGRESS", "APPROVED", attempt);
                    break;
                } else if (result == ExternalCheck.Result.TRANSIENT_ERROR ||
                        result == ExternalCheck.Result.TIMEOUT) {
                    claim.status = Claim.Status.RETRYING;
                    auditLogger.logEvent(claim, name, "IN_PROGRESS", "RETRYING", attempt);
                    // Requeue for retry, preserving per-policy FIFO
                    backlog.requeue(claim);
                    return;
                } else if (result == ExternalCheck.Result.PERMANENT_ERROR) {
                    claim.status = Claim.Status.REJECTED;
                    processed = true;
                    auditLogger.logEvent(claim, name, "IN_PROGRESS", "REJECTED", attempt);
                    break;
                }
            }
            if (!processed) {
                claim.status = Claim.Status.REJECTED;
                auditLogger.logEvent(claim, name, "IN_PROGRESS", "REJECTED", claim.attempt);
            }
            backlog.remove(claim);
            summaryReport.record(claim);
        }
    }
}
