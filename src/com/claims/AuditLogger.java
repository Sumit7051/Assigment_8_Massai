package com.claims;

import com.claims.model.Claim;

import java.io.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

// Thread safe
public class AuditLogger {
    private final BufferedWriter writer;
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, List<String>> inMemoryHistory = new ConcurrentHashMap<>();

    public AuditLogger(Path logPath) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(logPath.toFile(), true));
    }

    public void logEvent(Claim claim, String threadName, String prevStatus, String newStatus, int attempt) {
        String line = String.format("%s,%s,%s,%s,%s,%s,%s,%d",
                LocalDateTime.now().toString(),
                claim.claimId,
                threadName,
                prevStatus,
                newStatus,
                claim.policyNumber,
                claim.priorityFlag.name(),
                attempt
        );
        lock.lock();
        try {
            writer.write(line);
            writer.newLine();
            writer.flush();
            inMemoryHistory.computeIfAbsent(claim.claimId, k -> new ArrayList<>()).add(line);
        } catch (IOException e) {
            System.err.println("Audit log error: " + e);
        } finally {
            lock.unlock();
        }
    }

    public Map<String, List<String>> getInMemoryHistory() {
        return inMemoryHistory;
    }

    public void close() {
        try { writer.close(); } catch (IOException ignored) {}
    }
}