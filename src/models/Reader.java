package models;

import java.time.LocalDate;

class Reader extends Person {
    private String readerId;
    private MembershipType membershipType;
    private LocalDate registrationDate;
    private int currentBorrows;
    private int totalBorrowed;
    private boolean isActive;

    public Reader(String id, String name, String email, String phone, MembershipType membershipType) {
        super(id, name, email, phone);
        this.readerId = id;
        this.membershipType = membershipType;
        this.registrationDate = LocalDate.now();
        this.currentBorrows = 0;
        this.totalBorrowed = 0;
        this.isActive = true;
    }

    @Override
    public String getInfo() {
        return String.format("ID: %s | Tên: %s | Loại: %s | Đang mượn: %d/%d | Tổng đã mượn: %d",
                readerId, name, membershipType, currentBorrows,
                membershipType.getBorrowLimit(), totalBorrowed);
    }

    public boolean canBorrow() {
        return isActive && currentBorrows < membershipType.getBorrowLimit();
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

    public MembershipType getMembershipType() { return membershipType; }
    public int getCurrentBorrows() { return currentBorrows; }
    public int getTotalBorrowed() { return totalBorrowed; }
    public void setMembershipType(MembershipType type) { this.membershipType = type; }
}