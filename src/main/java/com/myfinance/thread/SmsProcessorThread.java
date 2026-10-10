package com.myfinance.thread;

import com.myfinance.dao.impl.SmsDAOImpl;
import com.myfinance.dao.interfaces.SmsDAO;
import com.myfinance.model.SmsQueueItem;
import com.myfinance.util.SmsParser;

import java.util.List;

/**
 * Background daemon thread for continuous SMS queue monitoring and re-evaluation.
 * Polling interval: 30 seconds.
 */
public class SmsProcessorThread implements Runnable {

    private final SmsDAO smsDAO;
    private static final long POLL_INTERVAL_MS = 30000; // 30 seconds

    /**
     * Default constructor initializing SmsDAO implementation.
     */
    public SmsProcessorThread() {
        this.smsDAO = new SmsDAOImpl();
    }

    /**
     * Dependency injection constructor for testing.
     *
     * @param smsDAO SMS data access object
     */
    public SmsProcessorThread(SmsDAO smsDAO) {
        this.smsDAO = smsDAO;
    }

    /**
     * Executes the background daemon worker loop, polling for pending SMS items every 30 seconds.
     */
    @Override
    public void run() {
        System.out.println("[SmsProcessorThread] Daemon worker started. Polling every 30s.");
        // Daemon loop: polls pending unconfirmed SMS transactions every 30 seconds
        while (!Thread.currentThread().isInterrupted()) {
            try {
                processSmsQueue();
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                System.out.println("[SmsProcessorThread] Interrupted. Stopping worker.");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("[SmsProcessorThread] Error during queue processing: " + e.getMessage());
            }
        }
    }

    /**
     * Synchronized processing of the SMS queue to avoid concurrent read/write inconsistencies.
     */
    public synchronized void processSmsQueue() {
        try {
            List<SmsQueueItem> pending = smsDAO.findPendingAll();
            if (pending != null && !pending.isEmpty()) {
                for (SmsQueueItem item : pending) {
                    // If amount or merchant was not parsed initially, re-parse with latest patterns
                    if (item.getParsedAmount() <= 0 || "Unknown Merchant".equals(item.getParsedMerchant())) {
                        SmsParser.ParsedResult res = SmsParser.parse(item.getRawSms());
                        if (res.isSuccess()) {
                            item.setParsedAmount(res.getAmount());
                            item.setParsedMerchant(res.getMerchant());
                            item.setUpiRef(res.getUpiRef());
                            // Update details in DB if needed
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[SmsProcessorThread] Notice: " + e.getMessage());
        }
    }
}
