package com.myfinance.model;

import java.time.LocalDateTime;

/**
 * SmsQueueItem entity representing pending and processed SMS transactions (Innovation: Real-Time SMS Sync).
 * Holds parsed amount, merchant, and UPI reference awaiting one-tap user confirmation.
 */
public class SmsQueueItem extends BaseEntity {

    public enum Status {
        PENDING,
        CONFIRMED,
        REJECTED
    }

    private int userId;
    private String rawSms;
    private double parsedAmount;
    private String parsedMerchant;
    private String upiRef;
    private Status status;

    public SmsQueueItem() {
        super();
        this.status = Status.PENDING;
    }

    public SmsQueueItem(int userId, String rawSms, double parsedAmount, String parsedMerchant, String upiRef) {
        super();
        this.userId = userId;
        this.rawSms = rawSms;
        this.parsedAmount = parsedAmount;
        this.parsedMerchant = parsedMerchant;
        this.upiRef = upiRef;
        this.status = Status.PENDING;
    }

    public SmsQueueItem(int id, int userId, String rawSms, double parsedAmount, String parsedMerchant,
                        String upiRef, Status status, LocalDateTime createdAt) {
        super(id, createdAt);
        this.userId = userId;
        this.rawSms = rawSms;
        this.parsedAmount = parsedAmount;
        this.parsedMerchant = parsedMerchant;
        this.upiRef = upiRef;
        this.status = status != null ? status : Status.PENDING;
    }

    @Override
    public boolean validate() {
        if (userId <= 0) {
            return false;
        }
        if (rawSms == null || rawSms.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getRawSms() {
        return rawSms;
    }

    public void setRawSms(String rawSms) {
        this.rawSms = rawSms;
    }

    public double getParsedAmount() {
        return parsedAmount;
    }

    public void setParsedAmount(double parsedAmount) {
        this.parsedAmount = parsedAmount;
    }

    public String getParsedMerchant() {
        return parsedMerchant;
    }

    public void setParsedMerchant(String parsedMerchant) {
        this.parsedMerchant = parsedMerchant;
    }

    public String getUpiRef() {
        return upiRef;
    }

    public void setUpiRef(String upiRef) {
        this.upiRef = upiRef;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SmsQueueItem{" +
                "id=" + id +
                ", userId=" + userId +
                ", parsedAmount=" + parsedAmount +
                ", parsedMerchant='" + parsedMerchant + '\'' +
                ", upiRef='" + upiRef + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
