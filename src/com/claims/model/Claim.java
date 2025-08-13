package com.claims.model;

import java.time.LocalDateTime;

public class Claim {
    public enum Priority { URGENT, NORMAL }
    public enum Status { NEW, APPROVED, ESCALATED, REJECTED, IN_PROGRESS, FAILED, RETRYING, DUPLICATE }

    public final String claimId;
    public final String policyNumber;
    public final int claimAmount;
    public final String claimType;
    public final LocalDateTime timestamp;
    public final Priority priorityFlag;
    public volatile Status status;
    public int attempt;

    public Claim(String claimId, String policyNumber, int claimAmount, String claimType,
                 LocalDateTime timestamp, Priority priorityFlag) {
        this.claimId = claimId;
        this.policyNumber = policyNumber;
        this.claimAmount = claimAmount;
        this.claimType = claimType;
        this.timestamp = timestamp;
        this.priorityFlag = priorityFlag;
        this.status = Status.NEW;
        this.attempt = 0;
    }

    public boolean isSuspicious() {
        return claimType.equals("Accident") && claimAmount >= 400000;
    }
}