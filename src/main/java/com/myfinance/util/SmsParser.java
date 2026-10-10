package com.myfinance.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * High-precision Bank SMS Parser for Indian Banking transactions (SBI, HDFC, ICICI, Axis).
 * Extracts transaction amount, merchant / beneficiary name, and UPI reference number.
 */
public class SmsParser {

    // Matches Indian currency amounts prefixed with INR, Rs., ₹, or preceded by debit keywords
    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "(?i)(?:(?:INR|Rs\\.?|₹)\\s*([0-9]+(?:,[0-9]{2,3})*(?:\\.[0-9]{1,2})?))|" +
            "(?:debited\\s*(?:by|with)?\\s*(?:INR|Rs\\.?|₹)?\\s*([0-9]+(?:,[0-9]{2,3})*(?:\\.[0-9]{1,2})?))"
    );

    // Extracts 8-22 character alphanumeric UPI reference IDs or transaction numbers across banks
    private static final Pattern UPI_PATTERN = Pattern.compile(
            "(?i)(?:UPI\\s*(?:Ref(?:\\s*no)?|Txn(?:\\s*no)?|Reference|Transaction)|Ref(?:\\s*no)?|txn)\\s*[:\\s/-]+\\s*([A-Za-z0-9]{8,22})|" +
            "(?:UPI\\s*:\\s*)([A-Za-z0-9]{8,22})"
    );

    // Specific parser for ICICI format: "Info: UPI/<rrn>/<merchant>"
    private static final Pattern ICICI_INFO_PATTERN = Pattern.compile(
            "(?i)Info:\\s*UPI\\/([0-9A-Za-z]+)\\/([A-Za-z0-9\\s&'-]+?)(?:\\.|\\s+Avbl|$)"
    );

    // Matches merchant/payee names following prepositions (to, towards, for, at) before bank stop-words
    private static final Pattern MERCHANT_PATTERN = Pattern.compile(
            "(?i)(?:(?:transfer\\s+)?to|towards|for|at)\\s+([A-Za-z0-9\\s&'-]{2,32}?)(?=[.,]?(?:\\s+(?:on|via|by|UPI|Ref|txn|with|from|ending|Avbl|-)|$|\\.))"
    );

    private static final List<String> KNOWN_MERCHANTS = Arrays.asList(
            "Swiggy", "Zomato", "Uber", "Ola", "Blinkit", "Zepto", "Amazon", "Amazon Pay",
            "Flipkart", "Netflix", "Spotify", "Starbucks", "Chai Point", "Myntra", "BookMyShow"
    );

    /**
     * Value object representing the parsed attributes of a financial SMS transaction.
     */
    public static class ParsedResult {
        private final double amount;
        private final String merchant;
        private final String upiRef;
        private final boolean debit;
        private final boolean success;

        /**
         * Constructs a parsed SMS transaction result.
         *
         * @param amount transaction amount in rupees
         * @param merchant identified merchant or beneficiary name
         * @param upiRef UPI reference number or transaction ID
         * @param debit true if money was spent/debited, false if credited
         * @param success true if valid transaction amount was parsed
         */
        public ParsedResult(double amount, String merchant, String upiRef, boolean debit, boolean success) {
            this.amount = amount;
            this.merchant = merchant != null ? merchant.trim() : "Unknown Merchant";
            this.upiRef = upiRef != null ? upiRef.trim() : "";
            this.debit = debit;
            this.success = success;
        }

        /**
         * Returns parsed transaction amount in ₹.
         *
         * @return amount
         */
        public double getAmount() {
            return amount;
        }

        /**
         * Returns identified merchant or recipient name.
         *
         * @return merchant name
         */
        public String getMerchant() {
            return merchant;
        }

        /**
         * Returns extracted UPI reference ID if available.
         *
         * @return UPI reference string
         */
        public String getUpiRef() {
            return upiRef;
        }

        /**
         * Returns true if transaction represents an expense/debit.
         *
         * @return true for debits
         */
        public boolean isDebit() {
            return debit;
        }

        /**
         * Returns true if message was successfully parsed.
         *
         * @return true if amount parsed
         */
        public boolean isSuccess() {
            return success;
        }

        @Override
        public String toString() {
            return "ParsedResult{" +
                    "amount=" + amount +
                    ", merchant='" + merchant + '\'' +
                    ", upiRef='" + upiRef + '\'' +
                    ", debit=" + debit +
                    ", success=" + success +
                    '}';
        }
    }

    /**
     * Checks if a raw SMS string is empty, blank, or merely a placeholder template.
     *
     * @param raw message text
     * @return true if placeholder, blank, or null
     */
    public static boolean isPlaceholder(String raw) {
        if (raw == null) {
            return true;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return true;
        }
        String lower = trimmed.toLowerCase();
        return lower.equals("{sms_message}")
                || lower.equals("[sms_message]")
                || lower.equals("test message")
                || lower.equals("\"test message\"")
                || lower.equals("{message}")
                || lower.equals("[message]")
                || lower.equals("{text}")
                || lower.equals("[text]");
    }

    /**
     * Parses a raw SMS text into a structured {@link ParsedResult}.
     *
     * @param rawSms message string
     * @return parsed transaction details
     */
    public static ParsedResult parse(String rawSms) {
        if (isPlaceholder(rawSms)) {
            return new ParsedResult(0.0, "Unknown", "", false, false);
        }

        String lower = rawSms.toLowerCase();
        boolean hasCredit = lower.contains("credited") || lower.contains("received");
        boolean isDebit = !hasCredit && (lower.contains("debited") || lower.contains("sent") || 
                          lower.contains("paid") || lower.contains("spent") || 
                          lower.contains("withdrawn") || lower.contains("deducted"));

        // 1. Extract Amount
        double amount = 0.0;
        Matcher amountMatcher = AMOUNT_PATTERN.matcher(rawSms);
        if (amountMatcher.find()) {
            String val = amountMatcher.group(1);
            if (val == null || val.trim().isEmpty()) {
                val = amountMatcher.group(2);
            }
            if (val != null) {
                try {
                    amount = Double.parseDouble(val.replace(",", "").trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // 2. Extract UPI Reference
        String upiRef = "";
        Matcher iciciInfoMatcher = ICICI_INFO_PATTERN.matcher(rawSms);
        if (iciciInfoMatcher.find()) {
            upiRef = iciciInfoMatcher.group(1).trim();
        } else {
            Matcher upiMatcher = UPI_PATTERN.matcher(rawSms);
            if (upiMatcher.find()) {
                String ref = upiMatcher.group(1);
                if (ref == null || ref.trim().isEmpty()) {
                    ref = upiMatcher.group(2);
                }
                if (ref != null) {
                    upiRef = ref.trim();
                }
            }
        }

        // 3. Extract Merchant
        String merchant = null;
        if (iciciInfoMatcher.find(0)) {
            merchant = iciciInfoMatcher.group(2).trim();
        } else {
            Matcher merchantMatcher = MERCHANT_PATTERN.matcher(rawSms);
            if (merchantMatcher.find()) {
                String candidate = merchantMatcher.group(1).trim();
                // Avoid false positives like "A/C" or "HDFC"
                if (!candidate.equalsIgnoreCase("A/C") && !candidate.equalsIgnoreCase("account") 
                        && !candidate.equalsIgnoreCase("HDFC") && !candidate.equalsIgnoreCase("SBI")
                        && candidate.length() > 1) {
                    merchant = candidate;
                }
            }
        }

        // Fallback: check known merchants in text
        if (merchant == null || merchant.trim().isEmpty() || merchant.equalsIgnoreCase("transfer")) {
            for (String known : KNOWN_MERCHANTS) {
                if (lower.contains(known.toLowerCase())) {
                    merchant = known;
                    break;
                }
            }
        }

        if (merchant == null || merchant.trim().isEmpty()) {
            merchant = "Unknown Merchant";
        } else {
            merchant = sanitizeMerchant(merchant);
        }

        boolean success = amount > 0;
        return new ParsedResult(amount, merchant, upiRef, isDebit, success);
    }

    private static String sanitizeMerchant(String raw) {
        String clean = raw.replaceAll("[^A-Za-z0-9\\s&'-]", "").trim();
        if (clean.endsWith(" on") || clean.endsWith(" via")) {
            clean = clean.substring(0, clean.lastIndexOf(" ")).trim();
        }
        return clean.isEmpty() ? "Unknown Merchant" : clean;
    }
}
