package com.claims;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class Config{
    private final int workerCount;
    private final int backlogCapacity;
    private final int externalCheckTimeoutMs;
    private final int retryLimit;
    private final int suspiciousWindowSeconds;
    private final int suspiciousThreshold;
    private final Path claimsCsvPath;
    private final Path auditLogPath;
    private final Path summaryReportPath;

    public Config(int workerCount, int backlogCapacity, int externalCheckTimeoutMs, int retryLimit,
                  int suspiciousWindowSeconds, int suspiciousThreshold,
                  Path claimsCsvPath, Path auditLogPath, Path summaryReportPath) {
        this.workerCount = workerCount;
        this.backlogCapacity = backlogCapacity;
        this.externalCheckTimeoutMs = externalCheckTimeoutMs;
        this.retryLimit = retryLimit;
        this.suspiciousWindowSeconds = suspiciousWindowSeconds;
        this.suspiciousThreshold = suspiciousThreshold;
        this.claimsCsvPath = claimsCsvPath;
        this.auditLogPath = auditLogPath;
        this.summaryReportPath = summaryReportPath;
    }

    public static Config load(String[] args) throws IOException {
        Properties props = new Properties();
        if (args.length > 0) {
            try (FileInputStream fis = new FileInputStream(args[0])) {
                props.load(fis);
            }
        }
        int workerCount = Integer.parseInt(props.getProperty("workerCount", "8"));
        int backlogCapacity = Integer.parseInt(props.getProperty("backlogCapacity", "100"));
        int externalCheckTimeoutMs = Integer.parseInt(props.getProperty("externalCheckTimeoutMs", "1000"));
        int retryLimit = Integer.parseInt(props.getProperty("retryLimit", "3"));
        int suspiciousWindowSeconds = Integer.parseInt(props.getProperty("suspiciousWindowSeconds", "30"));
        int suspiciousThreshold = Integer.parseInt(props.getProperty("suspiciousThreshold", "5"));
        Path claimsCsvPath = Path.of(props.getProperty("claimsCsvPath", "claims.csv"));
        Path auditLogPath = Path.of(props.getProperty("auditLogPath", "audit.log"));
        Path summaryReportPath = Path.of(props.getProperty("summaryReportPath", "summary.txt"));

        return new Config(workerCount, backlogCapacity, externalCheckTimeoutMs, retryLimit,
                suspiciousWindowSeconds, suspiciousThreshold, claimsCsvPath, auditLogPath, summaryReportPath);
    }

    public int getWorkerCount() { return workerCount; }
    public int getBacklogCapacity() { return backlogCapacity; }
    public int getExternalCheckTimeoutMs() { return externalCheckTimeoutMs; }
    public int getRetryLimit() { return retryLimit; }
    public int getSuspiciousWindowSeconds() { return suspiciousWindowSeconds; }
    public int getSuspiciousThreshold() { return suspiciousThreshold; }
    public Path getClaimsCsvPath() { return claimsCsvPath; }
    public Path getAuditLogPath() { return auditLogPath; }
    public Path getSummaryReportPath() { return summaryReportPath; }
}