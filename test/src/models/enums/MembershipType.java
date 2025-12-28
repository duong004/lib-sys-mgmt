package models.enums;

public enum MembershipType {
    STANDARD(3, 0.0),
    PREMIUM(10, 0.15),
    STUDENT(5, 0.10),
    SENIOR(5, 0.20);

    private final int borrowLimit;
    private final double discount;

    MembershipType(int borrowLimit, double discount) {
        this.borrowLimit = borrowLimit;
        this.discount = discount;
    }

    public int getBorrowLimit() {
        return borrowLimit;
    }

    public double getDiscount() {
        return discount;
    }
}