package com.claims;

import java.util.concurrent.ConcurrentHashMap;


public class ClaimIdempotencyTracker {
    private final ConcurrentHashMap<String, Boolean> processedClaims = new ConcurrentHashMap<>();

    
    public boolean markProcessing(String claimId) {
        // Atomic put-if-absent
        return processedClaims.putIfAbsent(claimId, true) == null;
    }

    public int getProcessedCount() {
        return processedClaims.size();
    }
}
