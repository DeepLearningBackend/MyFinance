package com.myfinance.model;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * User entity representing registered platform users and administrators.
 * Includes profile financial baselines (income, cash in hand) and unique API token for mobile SMS sync.
 */
public class User extends BaseEntity {

    public enum Role {
        USER,
        ADMIN
    }

    public enum Status {
        ACTIVE,
        INACTIVE,
        PENDING
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    private String name;
    private String email;
    private String password;
    private Role role;
    private Status status;
    private double monthlyIncome;
    private double cashInHand;
    private String apiToken;

    public User() {
        super();
        this.role = Role.USER;
        this.status = Status.ACTIVE;
        this.monthlyIncome = 0.0;
        this.cashInHand = 0.0;
        this.apiToken = generateApiToken();
    }

    public User(String name, String email, String password, Role role, Status status) {
        super();
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role != null ? role : Role.USER;
        this.status = status != null ? status : Status.ACTIVE;
        this.monthlyIncome = 0.0;
        this.cashInHand = 0.0;
        this.apiToken = generateApiToken();
    }

    public User(int id, String name, String email, String password, Role role, Status status,
                double monthlyIncome, double cashInHand, String apiToken, LocalDateTime createdAt) {
        super(id, createdAt);
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role != null ? role : Role.USER;
        this.status = status != null ? status : Status.ACTIVE;
        this.monthlyIncome = monthlyIncome;
        this.cashInHand = cashInHand;
        this.apiToken = apiToken != null ? apiToken : generateApiToken();
    }

    /**
     * Generates a unique 36-character prefixed API token for mobile webhook integration.
     *
     * @return a secure random token string starting with "myf_"
     */
    public static String generateApiToken() {
        return "myf_" + UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public boolean validate() {
        if (name == null || name.trim().isEmpty() || name.trim().length() > 100) {
            return false;
        }
        if (email == null || email.trim().isEmpty() || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public double getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(double monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public double getCashInHand() {
        return cashInHand;
    }

    public void setCashInHand(double cashInHand) {
        this.cashInHand = cashInHand;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public boolean isAdmin() {
        return Role.ADMIN.equals(this.role);
    }

    public boolean isActive() {
        return Status.ACTIVE.equals(this.status);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", status=" + status +
                ", monthlyIncome=" + monthlyIncome +
                ", cashInHand=" + cashInHand +
                ", apiToken='" + apiToken + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
