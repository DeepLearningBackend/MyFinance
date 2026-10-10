package com.myfinance.model;

import java.time.LocalDateTime;

/**
 * SpendingPersonality entity representing calculated monthly financial behavior archetype
 * (Innovation: Spending Personality Engine).
 */
public class SpendingPersonality extends BaseEntity {

    private int userId;
    private String personalityType;
    private String primaryTrait;
    private String description;
    private int calculationMonth;
    private int calculationYear;

    public SpendingPersonality() {
        super();
    }

    public SpendingPersonality(int userId, String personalityType, String primaryTrait, String description,
                               int calculationMonth, int calculationYear) {
        super();
        this.userId = userId;
        this.personalityType = personalityType;
        this.primaryTrait = primaryTrait;
        this.description = description;
        this.calculationMonth = calculationMonth;
        this.calculationYear = calculationYear;
    }

    public SpendingPersonality(int id, int userId, String personalityType, String primaryTrait, String description,
                               int calculationMonth, int calculationYear, LocalDateTime createdAt) {
        super(id, createdAt);
        this.userId = userId;
        this.personalityType = personalityType;
        this.primaryTrait = primaryTrait;
        this.description = description;
        this.calculationMonth = calculationMonth;
        this.calculationYear = calculationYear;
    }

    @Override
    public boolean validate() {
        if (userId <= 0) {
            return false;
        }
        if (personalityType == null || personalityType.trim().isEmpty()) {
            return false;
        }
        if (calculationMonth < 1 || calculationMonth > 12) {
            return false;
        }
        if (calculationYear < 2000) {
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

    public String getPersonalityType() {
        return personalityType;
    }

    public void setPersonalityType(String personalityType) {
        this.personalityType = personalityType;
    }

    public String getPrimaryTrait() {
        return primaryTrait;
    }

    public void setPrimaryTrait(String primaryTrait) {
        this.primaryTrait = primaryTrait;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCalculationMonth() {
        return calculationMonth;
    }

    public void setCalculationMonth(int calculationMonth) {
        this.calculationMonth = calculationMonth;
    }

    public int getCalculationYear() {
        return calculationYear;
    }

    public void setCalculationYear(int calculationYear) {
        this.calculationYear = calculationYear;
    }

    @Override
    public String toString() {
        return "SpendingPersonality{" +
                "id=" + id +
                ", userId=" + userId +
                ", personalityType='" + personalityType + '\'' +
                ", primaryTrait='" + primaryTrait + '\'' +
                ", calculationMonth=" + calculationMonth +
                ", calculationYear=" + calculationYear +
                ", createdAt=" + createdAt +
                '}';
    }
}
