package com.claims;

import com.claims.model.Claim;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe claim backlog supporting priority and per-policy FIFO.
 */
public class ClaimBacklog {
    private final int capacity;
    private final AuditLogger auditLogger;

    // Map: PolicyNumber -> PolicyQueue (FIFO for that policy)
    private final ConcurrentHashMap<String, PolicyQueue> policyQueues = new ConcurrentHashMap<>();
    // Global URGENT/FIFO queue for fair scheduling between policies
    private final LinkedBlockingQueue<Claim> urgentQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Claim> normalQueue = new LinkedBlockingQueue<>();

    private final ReentrantLock intakeLock = new ReentrantLock();
    private volatile boolean paused = false;

    public ClaimBacklog(int capacity, AuditLogger auditLogger) {
        this.capacity = capacity;
        this.auditLogger = auditLogger;
    }

    public boolean isPaused() { return paused; }

    public void pauseIntake() {
        intakeLock.lock();
        try {
            paused = true;
        } finally {
            intakeLock.unlock();
        }
    }

    public void resumeIntake() {
        intakeLock.lock();
        try {
            paused = false;
        } finally {
            intakeLock.unlock();
        }
    }

    /**
     * Ingest a claim into the backlog (blocking if full or paused).
     */
    public void put(Claim claim) throws InterruptedException {
        while (true) {
            if (isPaused() || size() >= capacity) {
                Thread.sleep(50);
                continue;
            }
            // Per-policy FIFO
            policyQueues.computeIfAbsent(claim.policyNumber, k -> new PolicyQueue());
            PolicyQueue pq = policyQueues.get(claim.policyNumber);
            pq.enqueue(claim);
            // Also add to global priority queue for scheduling
            if (claim.priorityFlag == Claim.Priority.URGENT)
                urgentQueue.put(claim);
            else
                normalQueue.put(claim);

            auditLogger.logEvent(claim, "INGESTED", claim.status.name(), claim.status.name(), claim.attempt);
            break;
        }
    }

    public int size() {
        int s = 0;
        for (PolicyQueue pq : policyQueues.values()) {
            s += pq.size();
        }
        return s;
    }

    /**
     * Get the next claim to process, respecting URGENT priority across policies.
     */
    public Claim pollNext(Set<String> lockedPolicies) {
        // Prefer urgent first, but only if policy is not locked
        Claim claim = pollQueue(urgentQueue, lockedPolicies);
        if (claim != null) return claim;
        return pollQueue(normalQueue, lockedPolicies);
    }

    private Claim pollQueue(LinkedBlockingQueue<Claim> queue, Set<String> lockedPolicies) {
        Iterator<Claim> it = queue.iterator();
        while (it.hasNext()) {
            Claim c = it.next();
            if (!lockedPolicies.contains(c.policyNumber)) {
                // Remove from both global and per-policy queue
                it.remove();
                PolicyQueue pq = policyQueues.get(c.policyNumber);
                pq.remove(c);
                return c;
            }
        }
        return null;
    }

    /**
     * Re-queue claim for retry, preserving per-policy order.
     */
    public void requeue(Claim claim) {
        // Always append to policy queue tail, and global queue according to priority
        policyQueues.computeIfAbsent(claim.policyNumber, k -> new PolicyQueue());
        policyQueues.get(claim.policyNumber).enqueue(claim);
        if (claim.priorityFlag == Claim.Priority.URGENT)
            urgentQueue.offer(claim);
        else
            normalQueue.offer(claim);
    }

    /**
     * Remove claim from all queues (after completion/failure).
     */
    public void remove(Claim claim) {
        PolicyQueue pq = policyQueues.get(claim.policyNumber);
        if (pq != null) pq.remove(claim);
        urgentQueue.remove(claim);
        normalQueue.remove(claim);
    }

    public static class PolicyQueue {
        private final LinkedList<Claim> queue = new LinkedList<>();

        public synchronized void enqueue(Claim claim) { queue.addLast(claim); }
        public synchronized void remove(Claim claim) { queue.remove(claim); }
        public synchronized int size() { return queue.size(); }
    }
}