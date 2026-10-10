package com.myfinance.servlet;

import com.myfinance.dao.impl.SmsDAOImpl;
import com.myfinance.dao.impl.UserDAOImpl;
import com.myfinance.dao.interfaces.SmsDAO;
import com.myfinance.dao.interfaces.UserDAO;
import com.myfinance.model.Category;
import com.myfinance.model.Expense;
import com.myfinance.model.SmsQueueItem;
import com.myfinance.model.SpendGuardAlert;
import com.myfinance.model.User;
import com.myfinance.service.BudgetService;
import com.myfinance.service.ExpenseService;
import com.myfinance.util.ApiResponse;
import com.myfinance.util.CategoryMapper;
import com.myfinance.util.SessionUtil;
import com.myfinance.util.SmsParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Mobile Webhook & Live Sync API Endpoint for Indian Bank SMS transactions.
 * Endpoint: /api/sms-sync
 */
@WebServlet(name = "SmsApiServlet", urlPatterns = {"/api/sms-sync"})
public class SmsApiServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAOImpl();
    private final SmsDAO smsDAO = new SmsDAOImpl();
    private final ExpenseService expenseService = new ExpenseService();
    private final BudgetService budgetService = new BudgetService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        User user = authenticate(request);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(new JSONObject(ApiResponse.error("Unauthorized: Invalid or missing X-API-Token")).toString());
            return;
        }

        try {
            List<SmsQueueItem> pendingList = smsDAO.findPendingByUserId(user.getId());
            SmsQueueItem latest = smsDAO.findLatestByUserId(user.getId());

            boolean isLiveActive = false;
            String lastSyncedDisplay = "No SMS synced yet";

            if (latest != null && latest.getCreatedAt() != null) {
                // Compute sync recency relative to Asia/Kolkata local time
                java.time.ZoneId kolkataZone = java.time.ZoneId.of("Asia/Kolkata");
                LocalDateTime nowKolkata = LocalDateTime.now(kolkataZone);
                Duration diff = Duration.between(latest.getCreatedAt(), nowKolkata);
                long diffSeconds = diff.getSeconds();

                if (diffSeconds >= -60 && diff.toHours() < 24) {
                    isLiveActive = true;
                    if (diff.toMinutes() < 1) {
                        lastSyncedDisplay = "Just now";
                    } else if (diff.toMinutes() < 60) {
                        lastSyncedDisplay = diff.toMinutes() + "m ago";
                    } else {
                        lastSyncedDisplay = diff.toHours() + "h ago";
                    }
                } else {
                    lastSyncedDisplay = latest.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM, hh:mm a"));
                }
            }

            JSONObject resJson = new JSONObject();
            resJson.put("success", true);
            resJson.put("liveSyncActive", isLiveActive);
            resJson.put("lastSyncedText", lastSyncedDisplay);

            DateTimeFormatter cardTimeFmt = DateTimeFormatter.ofPattern("dd MMM, hh:mm a");
            JSONArray itemsArr = new JSONArray();
            for (SmsQueueItem item : pendingList) {
                JSONObject obj = new JSONObject();
                obj.put("id", item.getId());
                obj.put("amount", item.getParsedAmount());
                obj.put("merchant", item.getParsedMerchant());
                obj.put("upiRef", item.getUpiRef());
                obj.put("rawSms", item.getRawSms());
                obj.put("status", item.getStatus().name());
                obj.put("createdAt", item.getCreatedAt() != null ? item.getCreatedAt().toString() : "");
                obj.put("timeDisplay", item.getCreatedAt() != null ? item.getCreatedAt().format(cardTimeFmt) : "");
                itemsArr.put(obj);
            }
            resJson.put("pendingItems", itemsArr);
            out.print(resJson.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(new JSONObject(ApiResponse.error("Server error: " + e.getMessage())).toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        User user = authenticate(request);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(new JSONObject(ApiResponse.error("Unauthorized: Invalid or missing X-API-Token")).toString());
            return;
        }

        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            action = request.getHeader("X-Action");
        }

        try {
            // Action: CONFIRM an SMS item and convert to Expense with source = 'SMS_SYNC'
            if ("confirm".equalsIgnoreCase(action)) {
                handleConfirmSms(request, response, user, out);
                return;
            }

            // Action: REJECT an SMS item
            if ("reject".equalsIgnoreCase(action)) {
                handleRejectSms(request, response, user, out);
                return;
            }

            // Default: Ingest new SMS
            handleIngestSms(request, response, user, out);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(new JSONObject(ApiResponse.error("Failed to process request: " + e.getMessage())).toString());
        }
    }

    private void handleIngestSms(HttpServletRequest request, HttpServletResponse response, User user, PrintWriter out) throws Exception {
        String rawBody = readRequestBody(request);
        String smsText = null;

        if (rawBody != null && !rawBody.trim().isEmpty()) {
            String trimmed = rawBody.trim();
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                try {
                    JSONObject json = new JSONObject(trimmed);
                    if (json.has("sms")) {
                        smsText = json.getString("sms");
                    } else if (json.has("message")) {
                        smsText = json.getString("message");
                    } else if (json.has("text")) {
                        smsText = json.getString("text");
                    }
                } catch (Exception ignored) {
                }
            }
            if (smsText == null || smsText.trim().isEmpty()) {
                smsText = trimmed;
            }
        }

        if (smsText == null || smsText.trim().isEmpty() || SmsParser.isPlaceholder(smsText)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JSONObject errResp = new JSONObject();
            errResp.put("success", false);
            errResp.put("saved", false);
            errResp.put("reason", "SMS content is empty, blank, or a placeholder");
            errResp.put("message", "SMS content cannot be empty or a placeholder");
            out.print(errResp.toString());
            return;
        }

        // Parse SMS
        SmsParser.ParsedResult parsed = SmsParser.parse(smsText);

        // If the parser marks the message as a credit (isDebit false), don't queue it as an expense:
        // return HTTP 200 with {"success":true,"saved":false,"reason":"credit ignored"}
        if (!parsed.isDebit()) {
            response.setStatus(HttpServletResponse.SC_OK);
            JSONObject creditResp = new JSONObject();
            creditResp.put("success", true);
            creditResp.put("saved", false);
            creditResp.put("reason", "credit ignored");
            out.print(creditResp.toString());
            return;
        }

        // Never save a row whose parsed amount is missing or 0
        if (!parsed.isSuccess() || parsed.getAmount() <= 0.0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JSONObject noAmountResp = new JSONObject();
            noAmountResp.put("success", false);
            noAmountResp.put("saved", false);
            noAmountResp.put("reason", "Unable to parse a valid non-zero transaction amount");
            noAmountResp.put("message", "Unable to parse a valid non-zero transaction amount");
            out.print(noAmountResp.toString());
            return;
        }

        SmsQueueItem item = new SmsQueueItem(
                user.getId(),
                smsText,
                parsed.getAmount(),
                parsed.getMerchant(),
                parsed.getUpiRef()
        );
        item.setStatus(SmsQueueItem.Status.PENDING);

        boolean saved = smsDAO.insert(item);
        if (!saved) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject dbErr = new JSONObject();
            dbErr.put("success", false);
            dbErr.put("saved", false);
            dbErr.put("reason", "Failed to enqueue SMS record");
            out.print(dbErr.toString());
            return;
        }

        JSONObject data = new JSONObject();
        data.put("id", item.getId());
        data.put("amount", item.getParsedAmount());
        data.put("merchant", item.getParsedMerchant());
        data.put("upiRef", item.getUpiRef());
        data.put("status", item.getStatus().name());

        JSONObject resp = new JSONObject();
        resp.put("success", true);
        resp.put("saved", true);
        resp.put("message", "SMS ingested and queued successfully");
        resp.put("data", data);

        response.setStatus(HttpServletResponse.SC_OK);
        out.print(resp.toString());
    }

    private void handleConfirmSms(HttpServletRequest request, HttpServletResponse response, User user, PrintWriter out) throws Exception {
        String smsIdStr = request.getParameter("id");
        String forceStr = request.getParameter("force");
        boolean force = "true".equalsIgnoreCase(forceStr);
        String jsonCategory = null;

        if (smsIdStr == null || smsIdStr.isEmpty()) {
            String rawBody = readRequestBody(request);
            if (rawBody != null && rawBody.startsWith("{")) {
                JSONObject json = new JSONObject(rawBody);
                if (json.has("id")) smsIdStr = String.valueOf(json.get("id"));
                if (json.has("force")) force = json.optBoolean("force", false) || "true".equalsIgnoreCase(json.optString("force"));
                if (json.has("category")) jsonCategory = json.optString("category");
            }
        }

        if (smsIdStr == null || smsIdStr.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(new JSONObject(ApiResponse.error("Missing SMS item id")).toString());
            return;
        }

        int smsId = Integer.parseInt(smsIdStr);
        SmsQueueItem item = smsDAO.findById(smsId);
        if (item == null || item.getUserId() != user.getId()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print(new JSONObject(ApiResponse.error("SMS transaction not found or access denied")).toString());
            return;
        }

        if (item.getStatus() != SmsQueueItem.Status.PENDING) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(new JSONObject(ApiResponse.error("SMS transaction already " + item.getStatus())).toString());
            return;
        }

        // Determine category via CategoryMapper or custom param, and normalize to canonical name
        String category = request.getParameter("category");
        if (category == null || category.trim().isEmpty() || Category.OTHER.equals(category)) {
            if (jsonCategory != null && !jsonCategory.trim().isEmpty() && !Category.OTHER.equals(jsonCategory)) {
                category = jsonCategory;
            } else {
                category = CategoryMapper.suggest(item.getParsedMerchant());
            }
        }
        category = CategoryMapper.normalize(category);

        double amount = item.getParsedAmount();
        String title = item.getParsedMerchant() != null && !item.getParsedMerchant().isEmpty() 
                ? item.getParsedMerchant() : "Mobile SMS Debit";

        // Spend Guard check (Innovation 1: Golden rule 6: check BEFORE the INSERT)
        if (!force) {
            SpendGuardAlert alert = budgetService.checkBeforeExpense(user.getId(), category, amount, LocalDate.now());
            if (alert != null) {
                JSONObject resp = new JSONObject();
                resp.put("success", true);
                resp.put("needsConfirmation", true);
                resp.put("alert", alert.toJSON());
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(resp.toString());
                return;
            }
        }

        // Build Expense with source = SMS_SYNC
        Expense expense = new Expense(
                user.getId(),
                title,
                amount,
                category,
                LocalDate.now(),
                "SMS_SYNC",
                item.getUpiRef(),
                "Auto-synced via bank SMS: " + (item.getUpiRef() != null ? item.getUpiRef() : "")
        );

        ApiResponse<Expense> expResult = expenseService.addExpense(expense);
        if (!expResult.isSuccess()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(new JSONObject(ApiResponse.error("Failed to add expense: " + expResult.getMessage())).toString());
            return;
        }

        // Update SMS queue status to CONFIRMED
        smsDAO.updateStatus(smsId, SmsQueueItem.Status.CONFIRMED);

        JSONObject resp = new JSONObject();
        resp.put("success", true);
        resp.put("message", "Transaction confirmed and recorded as SMS_SYNC expense!");
        JSONObject expData = new JSONObject();
        expData.put("id", expense.getId());
        expData.put("title", expense.getTitle());
        expData.put("amount", expense.getAmount());
        expData.put("category", expense.getCategory());
        expData.put("source", expense.getSource());
        expData.put("upiRef", expense.getUpiRef());
        resp.put("data", expData);

        out.print(resp.toString());
    }

    private void handleRejectSms(HttpServletRequest request, HttpServletResponse response, User user, PrintWriter out) throws Exception {
        String smsIdStr = request.getParameter("id");
        if (smsIdStr == null || smsIdStr.isEmpty()) {
            String rawBody = readRequestBody(request);
            if (rawBody != null && rawBody.startsWith("{")) {
                JSONObject json = new JSONObject(rawBody);
                if (json.has("id")) smsIdStr = String.valueOf(json.get("id"));
            }
        }

        if (smsIdStr == null || smsIdStr.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(new JSONObject(ApiResponse.error("Missing SMS item id")).toString());
            return;
        }

        int smsId = Integer.parseInt(smsIdStr);
        SmsQueueItem item = smsDAO.findById(smsId);
        if (item == null || item.getUserId() != user.getId()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print(new JSONObject(ApiResponse.error("SMS transaction not found")).toString());
            return;
        }

        smsDAO.updateStatus(smsId, SmsQueueItem.Status.REJECTED);

        JSONObject resp = new JSONObject();
        resp.put("success", true);
        resp.put("message", "SMS transaction dismissed");
        out.print(resp.toString());
    }

    private User authenticate(HttpServletRequest request) {
        // Priority 1: X-API-Token header
        String token = request.getHeader("X-API-Token");
        if (token == null || token.trim().isEmpty()) {
            token = request.getHeader("x-api-token");
        }
        if (token != null && !token.trim().isEmpty()) {
            try {
                return userDAO.findByApiToken(token.trim());
            } catch (Exception e) {
                return null;
            }
        }

        // Priority 2: Query parameter token
        String paramToken = request.getParameter("token");
        if (paramToken != null && !paramToken.trim().isEmpty()) {
            try {
                return userDAO.findByApiToken(paramToken.trim());
            } catch (Exception e) {
                return null;
            }
        }

        // Priority 3: Web Session fallback (for frontend Quick Confirm / Paste box)
        Integer sessionUserId = SessionUtil.getUserId(request);
        if (sessionUserId != null && sessionUserId > 0) {
            try {
                return userDAO.findById(sessionUserId);
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString().trim();
    }
}
