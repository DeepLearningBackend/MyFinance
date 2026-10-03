package com.myfinance.model;

public class User extends BaseEntity {
    private String name;
    private String email;
    private String password;
    private Role role;
    private Status status;

    public enum Role {
        USER, ADMIN
    }

    public enum Status {
        ACTIVE, INACTIVE, PENDING
    }

    public User() {
        this.role = Role.USER;
        this.status = Status.ACTIVE;
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

    @Override
    public boolean validate() {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return true;
    }
}
