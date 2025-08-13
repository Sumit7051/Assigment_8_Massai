package com.claims;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe idempotency tracker for processed claims.
 */
public class ClaimIdempotencyTracker {
    private final ConcurrentHashMap<String, Boolean> processedClaims = new ConcurrentHashMap<>();

    /**
     * Mark claim as processing if not already processed.
     * Returns true if claim can be processed, false if duplicate.
     */
    public boolean markProcessing(String claimId) {
        // Atomic put-if-absent
        return processedClaims.putIfAbsent(claimId, true) == null;
    }

    public int getProcessedCount() {
        return processedClaims.size();
    }
}