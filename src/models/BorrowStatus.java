package models;

enum BorrowStatus {
    BORROWED("Đang mượn"),
    RETURNED("Đã trả"),
    OVERDUE("Quá hạn"),
    LOST("Mất sách");

    private final String description;

    BorrowStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}