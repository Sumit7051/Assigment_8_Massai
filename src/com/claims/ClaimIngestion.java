package com.claims;

import com.claims.model.Claim;

import java.io.*;
import java.nio.file.Path;
import java.time.LocalDateTime;


public class ClaimIngestion implements Runnable {
    private final Path csvPath;
    private final ClaimBacklog backlog;
    private final SuspiciousMonitor monitor;
    private final Config config;
    private volatile boolean stopped = false;

    public ClaimIngestion(Path csvPath, ClaimBacklog backlog, SuspiciousMonitor monitor, Config config) {
        this.csvPath = csvPath;
        this.backlog = backlog;
        this.monitor = monitor;
        this.config = config;
    }

    public void stop() {
        stopped = true;
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath.toFile()))) {
            String line;
            while (!stopped && (line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("ClaimID")) continue; // Skip header
                Claim claim = parseClaim(line);
                if (claim == null) continue;
                if (claim.isSuspicious()) {
                    monitor.recordSuspicious(claim);
                }
                backlog.put(claim);
            }
        } catch (Exception e) {
            System.err.println("Ingestion error: " + e);
        }
    }

    private Claim parseClaim(String line) {
        try {
            String[] parts = line.split(",", -1);
            if (parts.length != 6) return null;
            String id = parts[0], policy = parts[1], type = parts[3], pf = parts[5];
            int amt = Integer.parseInt(parts[2]);
            LocalDateTime ts = LocalDateTime.parse(parts[4]);
            Claim.Priority prio = pf.equalsIgnoreCase("URGENT") ? Claim.Priority.URGENT : Claim.Priority.NORMAL;
            return new Claim(id, policy, amt, type, ts, prio);
        } catch (Exception e) {
            System.err.println("Parse error: " + e);
            return null;
        }
    }
}
