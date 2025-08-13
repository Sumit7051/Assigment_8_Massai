package com.claims;

import com.claims.model.Claim;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class SummaryReport {
    private final Path summaryPath;
    private final Set<String> uniqueClaims = ConcurrentHashMap.newKeySet();
    private final AtomicInteger approved = new AtomicInteger(0);
    private final AtomicInteger escalated = new AtomicInteger(0);
    private final AtomicInteger rejected = new AtomicInteger(0);
    private final AtomicLong totalPaid = new AtomicLong(0);
    private final AtomicInteger totalAttempts = new AtomicInteger(0);

    public SummaryReport(Path summaryPath) {
        this.summaryPath = summaryPath;
    }

    public void record(Claim claim) {
        uniqueClaims.add(claim.claimId);
        totalAttempts.addAndGet(claim.attempt);
        switch (claim.status) {
            case APPROVED:
                approved.incrementAndGet();
                totalPaid.addAndGet(claim.claimAmount);
                break;
            case ESCALATED: escalated.incrementAndGet(); break;
            case REJECTED: rejected.incrementAndGet(); break;
        }
    }

    public void write(Map<String, List<String>> history, List<Claim> suspiciousClaims) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(summaryPath.toFile()))) {
            bw.write("Total unique claims processed: " + uniqueClaims.size());
            bw.newLine();
            bw.write("Approved: " + approved.get());
            bw.newLine();
            bw.write("Escalated: " + escalated.get());
            bw.newLine();
            bw.write("Rejected: " + rejected.get());
            bw.newLine();
            bw.write("Suspicious claims detected: " + suspiciousClaims.size());
            bw.newLine();
            bw.write("Total amount paid: " + totalPaid.get());
            bw.newLine();
            double avgAttempts = uniqueClaims.isEmpty() ? 0 : ((double) totalAttempts.get()) / uniqueClaims.size();
            bw.write(String.format("Average processing attempts per claim: %.2f", avgAttempts));
            bw.newLine();
            // Optionally, add more stats if needed
        } catch (IOException e) {
            System.err.println("Summary write error: " + e);
        }
    }
}
