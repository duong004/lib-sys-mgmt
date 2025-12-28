package models;

import java.time.LocalDateTime;

public class BookInventoryLog {
    private Long logId;
    private String isbn;
    private int quantityChange;
    private int totalCopiesAfter;
    private String actionType; // "ADD_NEW", "INCREASE_STOCK", "DECREASE_STOCK"
    private String performedBy; // Librarian ID
    private LocalDateTime timestamp;
    private String notes;

    // Constructor for new log entry
    public BookInventoryLog(String isbn, int quantityChange, int totalCopiesAfter,
                            String actionType, String performedBy, String notes) {
        this.isbn = isbn;
        this.quantityChange = quantityChange;
        this.totalCopiesAfter = totalCopiesAfter;
        this.actionType = actionType;
        this.performedBy = performedBy;
        this.timestamp = LocalDateTime.now();
        this.notes = notes;
    }

    // Constructor for loading from database
    public BookInventoryLog(Long logId, String isbn, int quantityChange, int totalCopiesAfter,
                            String actionType, String performedBy, LocalDateTime timestamp, String notes) {
        this.logId = logId;
        this.isbn = isbn;
        this.quantityChange = quantityChange;
        this.totalCopiesAfter = totalCopiesAfter;
        this.actionType = actionType;
        this.performedBy = performedBy;
        this.timestamp = timestamp;
        this.notes = notes;
    }

    public String getInfo() {
        String changeStr = quantityChange > 0 ? "+" + quantityChange : String.valueOf(quantityChange);
        return String.format("[%s] ISBN: %s | Thay đổi: %s | Tổng sau: %d | Người thực hiện: %s | Lý do: %s",
                timestamp, isbn, changeStr, totalCopiesAfter, performedBy, notes);
    }

    // Getters and Setters
    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public int getQuantityChange() { return quantityChange; }
    public void setQuantityChange(int quantityChange) { this.quantityChange = quantityChange; }

    public int getTotalCopiesAfter() { return totalCopiesAfter; }
    public void setTotalCopiesAfter(int totalCopiesAfter) { this.totalCopiesAfter = totalCopiesAfter; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}