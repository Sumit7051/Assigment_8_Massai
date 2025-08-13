package com.claims;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Config config = Config.load(args);
        AuditLogger auditLogger = new AuditLogger(config.getAuditLogPath());
        SummaryReport summaryReport = new SummaryReport(config.getSummaryReportPath());

        ClaimBacklog backlog = new ClaimBacklog(config.getBacklogCapacity(), auditLogger);
        ClaimIdempotencyTracker idempotencyTracker = new ClaimIdempotencyTracker();

        SuspiciousMonitor suspiciousMonitor = new SuspiciousMonitor(
                config.getSuspiciousThreshold(),
                config.getSuspiciousWindowSeconds(),
                auditLogger,
                backlog
        );

        ClaimIngestion ingestion = new ClaimIngestion(
                config.getClaimsCsvPath(),
                backlog,
                suspiciousMonitor,
                config
        );

        PolicySerialScheduler scheduler = new PolicySerialScheduler(
                backlog,
                idempotencyTracker,
                auditLogger,
                summaryReport,
                config
        );

        Thread monitorThread = new Thread(suspiciousMonitor, "SuspiciousMonitor");
        monitorThread.start();

        Thread ingestionThread = new Thread(ingestion, "ClaimIngestion");
        ingestionThread.start();

        scheduler.startWorkers();

        // Graceful shutdown: listen for SIGINT, SIGTERM
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown signal received. Initiating graceful shutdown...");
            ingestion.stop();
            scheduler.shutdown();
            suspiciousMonitor.stop();
        }));

        // Wait for ingestion to finish
        ingestionThread.join();
        // Wait for scheduler to finish all claims
        scheduler.awaitFinish();

        // Stop monitor
        suspiciousMonitor.stop();
        monitorThread.join();

        // Final summary
        summaryReport.write(auditLogger.getInMemoryHistory(), suspiciousMonitor.getSuspiciousClaims());
        auditLogger.close();

        System.out.println("Processing complete. See summary.txt and audit.log.");
    }
}