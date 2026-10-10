package com.myfinance.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SmsParser Unit Tests for Multi-Bank Format Parsing")
class SmsParserTest {

    @Test
    @DisplayName("Test SBI UPI Debit SMS")
    void testSbiSms() {
        String sms = "Your A/C ending 1234 debited by INR 450.00 on 09-Oct-26 via UPI transfer to Swiggy. UPI Ref: 123456789012 - SBI";
        SmsParser.ParsedResult res = SmsParser.parse(sms);

        assertTrue(res.isSuccess());
        assertTrue(res.isDebit());
        assertEquals(450.00, res.getAmount(), 0.001);
        assertEquals("Swiggy", res.getMerchant());
        assertEquals("123456789012", res.getUpiRef());
    }

    @Test
    @DisplayName("Test HDFC Debit SMS")
    void testHdfcSms() {
        String sms = "Alert: Rs 780.00 debited from HDFC Bank A/C ending 5678 on 09-OCT-26 towards UBER. UPI: 112233445566.";
        SmsParser.ParsedResult res = SmsParser.parse(sms);

        assertTrue(res.isSuccess());
        assertTrue(res.isDebit());
        assertEquals(780.00, res.getAmount(), 0.001);
        assertEquals("UBER", res.getMerchant());
        assertEquals("112233445566", res.getUpiRef());
    }

    @Test
    @DisplayName("Test ICICI Info UPI Debit SMS")
    void testIciciSms() {
        String sms = "ICICI Bank Acct XX123 debited with INR 2,400.00 on 09-Oct-26. Info: UPI/334455667788/Amazon Pay. Avbl Bal INR 15,200.";
        SmsParser.ParsedResult res = SmsParser.parse(sms);

        assertTrue(res.isSuccess());
        assertTrue(res.isDebit());
        assertEquals(2400.00, res.getAmount(), 0.001);
        assertEquals("Amazon Pay", res.getMerchant());
        assertEquals("334455667788", res.getUpiRef());
    }

    @Test
    @DisplayName("Test Axis Bank Debit SMS")
    void testAxisSms() {
        String sms = "Axis Bank: INR 620.00 debited from A/c no. XX789 on 09-10-26 to Starbucks. UPI Ref 556677889900.";
        SmsParser.ParsedResult res = SmsParser.parse(sms);

        assertTrue(res.isSuccess());
        assertTrue(res.isDebit());
        assertEquals(620.00, res.getAmount(), 0.001);
        assertEquals("Starbucks", res.getMerchant());
        assertEquals("556677889900", res.getUpiRef());
    }

    @Test
    @DisplayName("Test Sent Rs Quick Merchant SMS (Blinkit)")
    void testBlinkitSms() {
        String sms = "Sent Rs. 350.00 from HDFC Bank A/C 1234 to Blinkit on 09-10-26 via UPI txn 998877665544.";
        SmsParser.ParsedResult res = SmsParser.parse(sms);

        assertTrue(res.isSuccess());
        assertTrue(res.isDebit());
        assertEquals(350.00, res.getAmount(), 0.001);
        assertEquals("Blinkit", res.getMerchant());
        assertEquals("998877665544", res.getUpiRef());
    }

    @Test
    @DisplayName("Test Placeholder and Blank SMS Rejection")
    void testPlaceholderSms() {
        assertTrue(SmsParser.isPlaceholder(""));
        assertTrue(SmsParser.isPlaceholder("   "));
        assertTrue(SmsParser.isPlaceholder(null));
        assertTrue(SmsParser.isPlaceholder("{sms_message}"));
        assertTrue(SmsParser.isPlaceholder("[sms_message]"));
        assertTrue(SmsParser.isPlaceholder("test message"));
        assertTrue(SmsParser.isPlaceholder("\"test message\""));

        SmsParser.ParsedResult res = SmsParser.parse("{sms_message}");
        assertFalse(res.isSuccess());
        assertEquals(0.0, res.getAmount(), 0.001);
        assertFalse(res.isDebit());
    }

    @Test
    @DisplayName("Test Credit SMS is not marked as debit")
    void testCreditSms() {
        String sms = "Rs.500.00 is Credited to A/c ...3131 by UPI Ref:CR/628349475-UPGB";
        SmsParser.ParsedResult res = SmsParser.parse(sms);

        assertFalse(res.isDebit(), "Credit transaction must not be marked as debit");
        assertEquals(500.00, res.getAmount(), 0.001);
        assertTrue(res.isSuccess());
    }

    @Test
    @DisplayName("Test Normal Debit SMS")
    void testNormalDebitSms() {
        String sms = "Rs 1500.00 debited from account **4321 on 10-Oct-26 towards Amazon. UPI Ref: 987654321012.";
        SmsParser.ParsedResult res = SmsParser.parse(sms);

        assertTrue(res.isSuccess());
        assertTrue(res.isDebit());
        assertEquals(1500.00, res.getAmount(), 0.001);
        assertEquals("Amazon", res.getMerchant());
        assertEquals("987654321012", res.getUpiRef());
    }
}
