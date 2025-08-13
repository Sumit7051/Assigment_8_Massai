package com.claims;

import com.claims.model.Claim;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Watches for suspicious claims and triggers throttling.
 */
public class SuspiciousMonitor implements Runnable {
    private final int threshold;
    private final int windowSec;
    private final AuditLogger auditLogger;
    private final ClaimBacklog backlog;
    private final List<Claim> suspiciousClaims = Collections.synchronizedList(new ArrayList<>());
    private final Queue<Instant> suspiciousTimes = new LinkedList<>();
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    public SuspiciousMonitor(int threshold, int windowSec, AuditLogger auditLogger, ClaimBacklog backlog) {
        this.threshold = threshold;
        this.windowSec = windowSec;
        this.auditLogger = auditLogger;
        this.backlog = backlog;
    }

    public void stop() { stopped.set(true); }

    public void recordSuspicious(Claim claim) {
        suspiciousClaims.add(claim);
        suspiciousTimes.add(Instant.now());
        System.out.println("Suspicious claim detected: " + claim.claimId + " [" + claim.claimAmount + "]");
        auditLogger.logEvent(claim, "Monitor", claim.status.name(), "SUSPICIOUS", claim.attempt);
    }

    public List<Claim> getSuspiciousClaims() {
        return List.copyOf(suspiciousClaims);
    }

    @Override
    public void run() {
        while (!stopped.get()) {
            cleanupOldTimes();
            if (suspiciousTimes.size() >= threshold) {
                System.out.println("Throttling intake: " + suspiciousTimes.size() + " suspicious claims in window.");
                backlog.pauseIntake();
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                backlog.resumeIntake();
                System.out.println("Intake resumed after throttling.");
                suspiciousTimes.clear();
            }
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }
    }

    private void cleanupOldTimes() {
        Instant cutoff = Instant.now().minusSeconds(windowSec);
        while (!suspiciousTimes.isEmpty() && suspiciousTimes.peek().isBefore(cutoff)) {
            suspiciousTimes.poll();
        }
    }
}