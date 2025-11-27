package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Library implements Searchable, Reportable {
    private String libraryName;
    private String address;
    private List<Book> books;
    private List<Reader> readers;
    private List<Librarian> librarians;
    private List<BorrowRecord> borrowRecords;
    private int maxBorrowDays;
    private double finePerDay;

    public Library(String name, String address) {
        this.libraryName = name;
        this.address = address;
        this.books = new ArrayList<>();
        this.readers = new ArrayList<>();
        this.librarians = new ArrayList<>();
        this.borrowRecords = new ArrayList<>();
        this.maxBorrowDays = 14;
        this.finePerDay = 5000;
    }

    // BOOK MANAGEMENT
    public void addBook(Book book) {
        books.add(book);
        System.out.println(" Đã thêm sách: " + book.getTitle());
    }

    public boolean removeBook(String ISBN) {
        Book book = searchByISBN(ISBN);
        if (book != null && book.getAvailableCopies() == book.totalCopies) {
            books.remove(book);
            System.out.println(" Đã xóa sách: " + book.getTitle());
            return true;
        }
        System.out.println(" Không thể xóa sách (đang được mượn hoặc không tồn tại)");
        return false;
    }

    public void updateBook(Book book) {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getISBN().equals(book.getISBN())) {
                books.set(i, book);
                System.out.println(" Đã cập nhật sách: " + book.getTitle());
                return;
            }
        }
        System.out.println(" Không tìm thấy sách");
    }

    public void displayAllBooks() {
        System.out.println("\n=== DANH SÁCH SÁCH TRONG THƯ VIỆN ===");
        if (books.isEmpty()) {
            System.out.println("Chưa có sách nào.");
            return;
        }
        for (Book book : books) {
            System.out.println(book.getInfo());
        }
    }

    // SEARCH OPERATIONS
    @Override
    public List<Book> searchByTitle(String title) {
        List<Book> results = new ArrayList<>();
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(title.toLowerCase())) {
                results.add(book);
            }
        }
        return results;
    }

    @Override
    public List<Book> searchByAuthor(String author) {
        List<Book> results = new ArrayList<>();
        for (Book book : books) {
            if (book.getAuthor().toLowerCase().contains(author.toLowerCase())) {
                results.add(book);
            }
        }
        return results;
    }

    @Override
    public Book searchByISBN(String isbn) {
        for (Book book : books) {
            if (book.getISBN().equals(isbn)) {
                return book;
            }
        }
        return null;
    }

    @Override
    public List<Book> searchByCategory(String category) {
        List<Book> results = new ArrayList<>();
        for (Book book : books) {
            if (book.getCategory() != null &&
                    book.getCategory().toLowerCase().contains(category.toLowerCase())) {
                results.add(book);
            }
        }
        return results;
    }

    // READER MANAGEMENT
    public void registerReader(Reader reader) {
        readers.add(reader);
        System.out.println(" Đã đăng ký độc giả: " + reader.getName());
    }

    public boolean removeReader(String readerId) {
        Reader reader = findReaderById(readerId);
        if (reader != null && reader.getCurrentBorrows() == 0) {
            readers.remove(reader);
            System.out.println(" Đã xóa độc giả: " + reader.getName());
            return true;
        }
        System.out.println(" Không thể xóa (độc giả đang mượn sách hoặc không tồn tại)");
        return false;
    }

    public Reader findReaderById(String readerId) {
        for (Reader reader : readers) {
            if (reader.getId().equals(readerId)) {
                return reader;
            }
        }
        return null;
    }

    public void displayAllReaders() {
        System.out.println("\n=== DANH SÁCH ĐỘC GIẢ ===");
        if (readers.isEmpty()) {
            System.out.println("Chưa có độc giả nào.");
            return;
        }
        for (Reader reader : readers) {
            System.out.println(reader.getInfo());
        }
    }

    // BORROW/RETURN OPERATIONS
    public BorrowRecord borrowBook(String readerId, String ISBN) {
        Reader reader = findReaderById(readerId);
        Book book = searchByISBN(ISBN);

        if (reader == null) {
            System.out.println(" Không tìm thấy độc giả");
            return null;
        }

        if (book == null) {
            System.out.println(" Không tìm thấy sách");
            return null;
        }

        if (!reader.canBorrow()) {
            System.out.println(" Độc giả đã đạt giới hạn mượn sách");
            return null;
        }

        if (!book.isAvailable()) {
            System.out.println(" Sách không có sẵn");
            return null;
        }

        if (book.borrowBook()) {
            reader.incrementBorrowCount();
            BorrowRecord record = new BorrowRecord(reader, book);
            borrowRecords.add(record);
            System.out.println(" Mượn sách thành công!");
            System.out.println("  Hạn trả: " + record.getDueDate());
            return record;
        }

        return null;
    }

    public void returnBook(String recordId) {
        for (BorrowRecord record : borrowRecords) {
            if (record.getRecordId().equals(recordId) &&
                    record.getStatus() == BorrowStatus.BORROWED) {
                record.markAsReturned();
                record.getBook().returnBook();
                record.getReader().decrementBorrowCount();

                System.out.println(" Trả sách thành công!");
                if (record.getFine() > 0) {
                    System.out.println("  Phí phạt: " + String.format("%,.0f VND", record.getFine()));
                }
                return;
            }
        }
        System.out.println(" Không tìm thấy phiếu mượn");
    }

    public boolean extendBorrow(String recordId, int days) {
        for (BorrowRecord record : borrowRecords) {
            if (record.getRecordId().equals(recordId)) {
                if (record.extendDueDate(days)) {
                    System.out.println(" Đã gia hạn thêm " + days + " ngày");
                    System.out.println(" Hạn mới: " + record.getDueDate());
                    return true;
                } else {
                    System.out.println(" Không thể gia hạn (quá hạn hoặc đã gia hạn tối đa)");
                    return false;
                }
            }
        }
        System.out.println(" Không tìm thấy phiếu mượn");
        return false;
    }

    public List<BorrowRecord> getBorrowHistory(String readerId) {
        List<BorrowRecord> history = new ArrayList<>();
        for (BorrowRecord record : borrowRecords) {
            if (record.getReader().getId().equals(readerId)) {
                history.add(record);
            }
        }
        return history;
    }

    public List<BorrowRecord> getOverdueRecords() {
        List<BorrowRecord> overdue = new ArrayList<>();
        for (BorrowRecord record : borrowRecords) {
            if (record.isOverdue()) {
                overdue.add(record);
            }
        }
        return overdue;
    }

    // REPORT GENERATION
    @Override
    public void generatePopularBooksReport() {
        System.out.println("\n=== TOP 5 SÁCH ĐƯỢC MƯỢN NHIỀU NHẤT ===");
        Map<String, Integer> bookBorrowCount = new HashMap<>();

        for (BorrowRecord record : borrowRecords) {
            String isbn = record.getBook().getISBN();
            bookBorrowCount.put(isbn, bookBorrowCount.getOrDefault(isbn, 0) + 1);
        }

        bookBorrowCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> {
                    Book book = searchByISBN(entry.getKey());
                    System.out.println(book.getTitle() + " - " + entry.getValue() + " lần");
                });
    }

    @Override
    public void generateActiveReadersReport() {
        System.out.println("\n=== TOP 5 ĐỘC GIẢ TÍCH CỰC NHẤT ===");
        readers.stream()
                .sorted((r1, r2) -> Integer.compare(r2.getTotalBorrowed(), r1.getTotalBorrowed()))
                .limit(5)
                .forEach(reader -> {
                    System.out.println(reader.getName() + " - " + reader.getTotalBorrowed() + " lần");
                });
    }

    @Override
    public void generateOverdueReport() {
        System.out.println("\n=== DANH SÁCH SÁCH QUÁ HẠN ===");
        List<BorrowRecord> overdueList = getOverdueRecords();
        if (overdueList.isEmpty()) {
            System.out.println("Không có sách quá hạn.");
            return;
        }
        for (BorrowRecord record : overdueList) {
            System.out.println(record.getInfo() + " - Trễ: " + record.getDaysLate() + " ngày");
        }
    }

    @Override
    public void generateMonthlyStatistics() {
        System.out.println("\n=== THỐNG KÊ THÁNG NÀY ===");
        System.out.println("Tổng số sách: " + books.size());
        System.out.println("Tổng số độc giả: " + readers.size());
        System.out.println("Số lần mượn: " + borrowRecords.size());

        long borrowing = borrowRecords.stream()
                .filter(r -> r.getStatus() == BorrowStatus.BORROWED)
                .count();
        System.out.println("Đang được mượn: " + borrowing);
        System.out.println("Sách quá hạn: " + getOverdueRecords().size());
    }
}