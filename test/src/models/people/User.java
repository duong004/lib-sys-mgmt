package models.people;

import models.enums.UserRole;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

public class User {
    private Long userId;
    private String username;
    private String passwordHash;
    private UserRole role;
    private String fullName;
    private String email;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private String linkedEntityId; // Reader ID if role is READER

    // Constructor for new user (password will be hashed)
    public User(String username, String password, UserRole role, String fullName, String email) {
        this.username = username;
        this.passwordHash = hashPassword(password);
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor for loading from database
    public User(Long userId, String username, String passwordHash, UserRole role,
                String fullName, String email, boolean isActive, LocalDateTime createdAt,
                LocalDateTime lastLogin, String linkedEntityId) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.linkedEntityId = linkedEntityId;
    }

    /**
     * Hash password using SHA-256
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verify password
     */
    public boolean verifyPassword(String password) {
        return passwordHash.equals(hashPassword(password));
    }

    /**
     * Check if user has specific permission
     */
    public boolean hasPermission(String permission) {
        return role.hasPermission(permission);
    }

    /**
     * Update last login timestamp
     */
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public String getInfo() {
        return String.format("User: %s (%s) | Role: %s | Active: %s | Last login: %s",
                username, fullName, role.getDisplayName(), isActive ? "Yes" : "No",
                lastLogin != null ? lastLogin : "Never");
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public String getLinkedEntityId() { return linkedEntityId; }
    public void setLinkedEntityId(String linkedEntityId) { this.linkedEntityId = linkedEntityId; }
}
