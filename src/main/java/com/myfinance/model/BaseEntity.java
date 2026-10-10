package com.myfinance.model;

import java.time.LocalDateTime;

/**
 * Abstract base entity representing common properties across all domain models.
 * Provides inheritance and enforces entity validation.
 */
public abstract class BaseEntity {

    protected int id;
    protected LocalDateTime createdAt;

    public BaseEntity() {
        this.createdAt = LocalDateTime.now();
    }

    public BaseEntity(int id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    /**
     * Validates the domain entity's state before persistence.
     *
     * @return true if valid, false otherwise
     */
    public abstract boolean validate();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
