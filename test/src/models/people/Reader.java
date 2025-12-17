package models.people;

import models.enums.MembershipType;

import java.time.LocalDate;

public class Reader extends Person {
    private String readerId;
    private MembershipType membershipType;
    private LocalDate registrationDate;
    private int currentBorrows;
    private int totalBorrowed;
    private boolean isActive;
    private String status; // "ACTIVE", "INACTIVE", "SUSPENDED"

    // Constructor WITHOUT id - for creating new readers
    public Reader(String name, String email, String phone, MembershipType membershipType) {
        super(null, name, email, phone); // id will be set later by DAO
        this.membershipType = membershipType;
        this.registrationDate = LocalDate.now();
        this.currentBorrows = 0;
        this.totalBorrowed = 0;
        this.isActive = true;
        this.status = "ACTIVE"; // Default status
    }

    // Constructor WITH id - for loading from database
    public Reader(String id, String name, String email, String phone, MembershipType membershipType) {
        super(id, name, email, phone);
        this.readerId = id;
        this.membershipType = membershipType;
        this.registrationDate = LocalDate.now();
        this.currentBorrows = 0;
        this.totalBorrowed = 0;
        this.isActive = true;
        this.status = "ACTIVE";
    }

    @Override
    public String getInfo() {
        return String.format("ID: %s | Tên: %s | Loại: %s | Đang mượn: %d/%d | Tổng đã mượn: %d | Trạng thái: %s",
                readerId != null ? readerId : id, name, membershipType, currentBorrows,
                membershipType.getBorrowLimit(), totalBorrowed, status);
    }

    public boolean canBorrow() {
        return "ACTIVE".equals(status) && isActive && currentBorrows < membershipType.getBorrowLimit();
    }

    public void incrementBorrowCount() {
        currentBorrows++;
        totalBorrowed++;
    }

    public void decrementBorrowCount() {
        if (currentBorrows > 0) {
            currentBorrows--;
        }
    }

    public int getActivityScore() {
        return totalBorrowed;
    }

    // Getters and Setters
    public String getReaderId() { return readerId != null ? readerId : id; }
    public void setReaderId(String readerId) {
        this.readerId = readerId;
        this.id = readerId; // Keep parent id in sync
    }

    public MembershipType getMembershipType() { return membershipType; }
    public int getCurrentBorrows() { return currentBorrows; }
    public int getTotalBorrowed() { return totalBorrowed; }
    public void setMembershipType(MembershipType type) { this.membershipType = type; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public boolean isActive() { return isActive; }
    public void setRegistrationDate(LocalDate date) { this.registrationDate = date; }
    public void setCurrentBorrows(int currentBorrows) { this.currentBorrows = currentBorrows; }
    public void setTotalBorrowed(int totalBorrowed) { this.totalBorrowed = totalBorrowed; }
    public void setActive(boolean active) { this.isActive = active; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        // Sync with isActive for backward compatibility
        this.isActive = "ACTIVE".equals(status);
    }
}