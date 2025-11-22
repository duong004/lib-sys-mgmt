class BorrowRecord {
    private String recordId;
    private Reader reader;
    private Book book;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BorrowStatus status;
    private double fine;
    private int renewalCount;
    private static final int MAX_BORROW_DAYS = 14;

    public BorrowRecord(Reader reader, Book book) {
        this.recordId = generateRecordId();
        this.reader = reader;
        this.book = book;
        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(MAX_BORROW_DAYS);
        this.status = BorrowStatus.BORROWED;
        this.renewalCount = 0;
        this.fine = 0.0;
    }

    private String generateRecordId() {
        return "BR" + System.currentTimeMillis();
    }

    public double calculateFine() {
        if (returnDate == null) {
            returnDate = LocalDate.now();
        }
        long daysLate = ChronoUnit.DAYS.between(dueDate, returnDate);
        if (daysLate > 0) {
            fine = book.calculateLateFee((int) daysLate);
        }
        return fine;
    }

    public boolean isOverdue() {
        return LocalDate.now().isAfter(dueDate) && status == BorrowStatus.BORROWED;
    }

    public boolean extendDueDate(int days) {
        if (renewalCount < 2 && !isOverdue()) {
            dueDate = dueDate.plusDays(days);
            renewalCount++;
            return true;
        }
        return false;
    }

    public void markAsReturned() {
        this.returnDate = LocalDate.now();
        this.status = BorrowStatus.RETURNED;
        calculateFine();
    }

    public int getDaysLate() {
        LocalDate compareDate = returnDate != null ? returnDate : LocalDate.now();
        long days = ChronoUnit.DAYS.between(dueDate, compareDate);
        return days > 0 ? (int) days : 0;
    }

    public boolean canRenew() {
        return renewalCount < 2 && !isOverdue();
    }

    public String getInfo() {
        return String.format("Mã: %s | Độc giả: %s | Sách: %s | Mượn: %s | Hạn: %s | Trạng thái: %s",
                recordId, reader.getName(), book.getTitle(), borrowDate, dueDate, status.getDescription());
    }

    // Getters
    public String getRecordId() { return recordId; }
    public Reader getReader() { return reader; }
    public Book getBook() { return book; }
    public BorrowStatus getStatus() { return status; }
    public double getFine() { return fine; }
    public LocalDate getDueDate() { return dueDate; }
}