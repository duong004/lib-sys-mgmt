package MVP;// MVP

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

// ENUMERATIONS
// MVP.MembershipType.java
enum MembershipType {
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

// MVP.BorrowStatus.java
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

// INTERFACES

// MVP.Searchable.java
interface Searchable {
    List<Book> searchByTitle(String title);
    List<Book> searchByAuthor(String author);
    Book searchByISBN(String isbn);
    List<Book> searchByCategory(String category);
}

// MVP.Reportable.java
interface Reportable {
    void generatePopularBooksReport();
    void generateActiveReadersReport();
    void generateOverdueReport();
    void generateMonthlyStatistics();
}

// ABSTRACT CLASSES

// MVP.Person.java
abstract class Person {
    protected String id;
    protected String name;
    protected String email;
    protected String phone;
    protected String address;
    protected LocalDate dateOfBirth;

    public Person(String id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public abstract String getInfo();

    public boolean validateEmail() {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public boolean validatePhone() {
        return phone != null && phone.matches("^[0-9]{10}$");
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
}

// MVP.Book.java
abstract class Book {
    protected String ISBN;
    protected String title;
    protected String author;
    protected String publisher;
    protected int publishYear;
    protected String category;
    protected int totalCopies;
    protected int availableCopies;
    protected double price;

    public Book(String ISBN, String title, String author, int totalCopies) {
        this.ISBN = ISBN;
        this.title = title;
        this.author = author;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
    }

    public abstract void displayDetails();

    public String getInfo() {
        return String.format("ISBN: %s | Tên: %s | Tác giả: %s | Có sẵn: %d/%d",
                ISBN, title, author, availableCopies, totalCopies);
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    public boolean borrowBook() {
        if (availableCopies > 0) {
            availableCopies--;
            return true;
        }
        return false;
    }

    public void returnBook() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
    }

    public double calculateLateFee(int daysLate) {
        return daysLate * 5000; // 5000 VND per day
    }

    // Getters
    public String getISBN() { return ISBN; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public int getAvailableCopies() { return availableCopies; }
    public void setCategory(String category) { this.category = category; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public void setPublishYear(int year) { this.publishYear = year; }
    public void setPrice(double price) { this.price = price; }
}

// CLASSES - BOOKS
// MVP.TextBook.java
class TextBook extends Book {
    private String subject;
    private int grade;
    private int edition;

    public TextBook(String ISBN, String title, String author, int totalCopies, String subject, int grade) {
        super(ISBN, title, author, totalCopies);
        this.subject = subject;
        this.grade = grade;
        this.category = "Sách giáo khoa";
    }

    @Override
    public void displayDetails() {
        System.out.println("=== SÁCH GIÁO KHOA ===");
        System.out.println(getInfo());
        System.out.println("Môn học: " + subject);
        System.out.println("Lớp: " + grade);
    }

    public String getSubject() { return subject; }
    public int getGrade() { return grade; }
}

// MVP.ReferenceBook.java
class ReferenceBook extends Book {
    private String topic;
    private boolean canBorrow;
    private String referenceNumber;

    public ReferenceBook(String ISBN, String title, String author, int totalCopies, String topic) {
        super(ISBN, title, author, totalCopies);
        this.topic = topic;
        this.canBorrow = true;
        this.category = "Sách tham khảo";
    }

    @Override
    public void displayDetails() {
        System.out.println("=== SÁCH THAM KHẢO ===");
        System.out.println(getInfo());
        System.out.println("Chủ đề: " + topic);
        System.out.println("Có thể mượn: " + (canBorrow ? "Có" : "Không"));
    }

    public boolean canBeBorrowed() { return canBorrow; }
    public void setCanBorrow(boolean canBorrow) { this.canBorrow = canBorrow; }
}

// MVP.Magazine.java
class Magazine extends Book {
    private int issueNumber;
    private LocalDate publishDate;
    private String frequency;

    public Magazine(String ISBN, String title, String author, int totalCopies, int issueNumber) {
        super(ISBN, title, author, totalCopies);
        this.issueNumber = issueNumber;
        this.publishDate = LocalDate.now();
        this.category = "Tạp chí";
    }

    @Override
    public void displayDetails() {
        System.out.println("=== TẠP CHÍ ===");
        System.out.println(getInfo());
        System.out.println("Số phát hành: " + issueNumber);
        System.out.println("Ngày xuất bản: " + publishDate);
    }

    public int getIssueNumber() { return issueNumber; }
    public boolean isLatestIssue() {
        return publishDate.isAfter(LocalDate.now().minusMonths(1));
    }
}

// CLASSES - PERSONS
// MVP.Reader.java
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

// MVP.Librarian.java
class Librarian extends Person {
    private String employeeId;
    private String position;
    private LocalDate hireDate;
    private double salary;
    private List<String> permissions;

    public Librarian(String id, String name, String email, String phone, String position) {
        super(id, name, email, phone);
        this.employeeId = id;
        this.position = position;
        this.hireDate = LocalDate.now();
        this.permissions = new ArrayList<>();
        initializePermissions();
    }

    private void initializePermissions() {
        permissions.add("ADD_BOOK");
        permissions.add("REMOVE_BOOK");
        permissions.add("REGISTER_READER");
        permissions.add("PROCESS_BORROW");
        permissions.add("GENERATE_REPORT");
    }

    @Override
    public String getInfo() {
        return String.format("Mã NV: %s | Tên: %s | Chức vụ: %s",
                employeeId, name, position);
    }

    public boolean hasPermission(String action) {
        return permissions.contains(action);
    }
}

// BORROW RECORD
// MVP.BorrowRecord.java
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

// LIBRARY CLASS
// MVP.Library.java
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
                    System.out.println("  Hạn mới: " + record.getDueDate());
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


// MAIN
public class LibraryManagementSystem {
    private static Library library;
    private static Scanner scanner;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        library = new Library("Thư viện Trung tâm", "123 Đường ABC, Hà Nội");

        // Thêm dữ liệu mẫu
        initializeSampleData();

        // Menu chính
        while (true) {
            displayMainMenu();
            int choice = getIntInput("Chọn chức năng: ");

            switch (choice) {
                case 1: bookManagementMenu(); break;
                case 2: readerManagementMenu(); break;
                case 3: borrowReturnMenu(); break;
                case 4: reportMenu(); break;
                case 5:
                    System.out.println("Cảm ơn đã sử dụng hệ thống!");
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    private static void displayMainMenu() {
        System.out.println("\n╔═══════════════════════════════╗");
        System.out.println("║   HỆ THỐNG QUẢN LÝ THƯ VIỆN   ║");
        System.out.println("╚═══════════════════════════════╝");
        System.out.println("1. Quản lý Sách");
        System.out.println("2. Quản lý Độc giả");
        System.out.println("3. Mượn/Trả sách");
        System.out.println("4. Báo cáo & Thống kê");
        System.out.println("5. Thoát");
        System.out.println("────────────────────────────────────────");
    }

    private static void bookManagementMenu() {
        while (true) {
            System.out.println("\n=== QUẢN LÝ SÁCH ===");
            System.out.println("1. Thêm sách mới");
            System.out.println("2. Xóa sách");
            System.out.println("3. Tìm kiếm sách");
            System.out.println("4. Hiển thị tất cả sách");
            System.out.println("5. Quay lại");

            int choice = getIntInput("Chọn: ");

            switch (choice) {
                case 1: addBook(); break;
                case 2: removeBook(); break;
                case 3: searchBook(); break;
                case 4: library.displayAllBooks(); break;
                case 5: return;
            }
        }
    }

    private static void readerManagementMenu() {
        while (true) {
            System.out.println("\n=== QUẢN LÝ ĐỘC GIẢ ===");
            System.out.println("1. Đăng ký độc giả mới");
            System.out.println("2. Xóa độc giả");
            System.out.println("3. Hiển thị tất cả độc giả");
            System.out.println("4. Xem lịch sử mượn");
            System.out.println("5. Quay lại");

            int choice = getIntInput("Chọn: ");

            switch (choice) {
                case 1: registerReader(); break;
                case 2: removeReader(); break;
                case 3: library.displayAllReaders(); break;
                case 4: viewBorrowHistory(); break;
                case 5: return;
            }
        }
    }

    private static void borrowReturnMenu() {
        while (true) {
            System.out.println("\n=== MƯỢN/TRẢ SÁCH ===");
            System.out.println("1. Mượn sách");
            System.out.println("2. Trả sách");
            System.out.println("3. Gia hạn sách");
            System.out.println("4. Quay lại");

            int choice = getIntInput("Chọn: ");

            switch (choice) {
                case 1: borrowBook(); break;
                case 2: returnBook(); break;
                case 3: extendBorrow(); break;
                case 4: return;
            }
        }
    }

    private static void reportMenu() {
        while (true) {
            System.out.println("\n=== BÁO CÁO & THỐNG KÊ ===");
            System.out.println("1. Top sách được mượn nhiều");
            System.out.println("2. Top độc giả tích cực");
            System.out.println("3. Danh sách sách quá hạn");
            System.out.println("4. Thống kê tháng");
            System.out.println("5. Quay lại");

            int choice = getIntInput("Chọn: ");

            switch (choice) {
                case 1: library.generatePopularBooksReport(); break;
                case 2: library.generateActiveReadersReport(); break;
                case 3: library.generateOverdueReport(); break;
                case 4: library.generateMonthlyStatistics(); break;
                case 5: return;
            }
        }
    }

    // BOOK OPERATIONS
    private static void addBook() {
        System.out.println("\n--- THÊM SÁCH MỚI ---");
        System.out.println("Loại sách:");
        System.out.println("1. Sách giáo khoa");
        System.out.println("2. Sách tham khảo");
        System.out.println("3. Tạp chí");

        int type = getIntInput("Chọn loại: ");

        String isbn = getStringInput("ISBN: ");
        String title = getStringInput("Tên sách: ");
        String author = getStringInput("Tác giả: ");
        int copies = getIntInput("Số lượng: ");

        Book book = null;

        switch (type) {
            case 1:
                String subject = getStringInput("Môn học: ");
                int grade = getIntInput("Lớp: ");
                book = new TextBook(isbn, title, author, copies, subject, grade);
                break;
            case 2:
                String topic = getStringInput("Chủ đề: ");
                book = new ReferenceBook(isbn, title, author, copies, topic);
                break;
            case 3:
                int issueNumber = getIntInput("Số phát hành: ");
                book = new Magazine(isbn, title, author, copies, issueNumber);
                break;
            default:
                System.out.println("Loại sách không hợp lệ!");
                return;
        }

        library.addBook(book);
    }

    private static void removeBook() {
        String isbn = getStringInput("Nhập ISBN sách cần xóa: ");
        library.removeBook(isbn);
    }

    private static void searchBook() {
        System.out.println("\n--- TÌM KIẾM SÁCH ---");
        System.out.println("1. Tìm theo tên");
        System.out.println("2. Tìm theo tác giả");
        System.out.println("3. Tìm theo ISBN");
        System.out.println("4. Tìm theo thể loại");

        int choice = getIntInput("Chọn: ");

        switch (choice) {
            case 1:
                String title = getStringInput("Nhập tên sách: ");
                displaySearchResults(library.searchByTitle(title));
                break;
            case 2:
                String author = getStringInput("Nhập tên tác giả: ");
                displaySearchResults(library.searchByAuthor(author));
                break;
            case 3:
                String isbn = getStringInput("Nhập ISBN: ");
                Book book = library.searchByISBN(isbn);
                if (book != null) {
                    book.displayDetails();
                } else {
                    System.out.println("Không tìm thấy sách.");
                }
                break;
            case 4:
                String category = getStringInput("Nhập thể loại: ");
                displaySearchResults(library.searchByCategory(category));
                break;
        }
    }

    private static void displaySearchResults(List<Book> results) {
        if (results.isEmpty()) {
            System.out.println("Không tìm thấy kết quả.");
            return;
        }
        System.out.println("\n--- KẾT QUẢ TÌM KIẾM (" + results.size() + ") ---");
        for (Book book : results) {
            System.out.println(book.getInfo());
        }
    }

    // READER OPERATIONS
    private static void registerReader() {
        System.out.println("\n--- ĐĂNG KÝ ĐỘC GIẢ MỚI ---");

        String id = getStringInput("Mã độc giả: ");
        String name = getStringInput("Họ tên: ");
        String email = getStringInput("Email: ");
        String phone = getStringInput("Số điện thoại: ");

        System.out.println("Loại thành viên:");
        System.out.println("1. STANDARD (3 sách)");
        System.out.println("2. PREMIUM (10 sách)");
        System.out.println("3. STUDENT (5 sách)");
        System.out.println("4. SENIOR (5 sách)");

        int typeChoice = getIntInput("Chọn: ");
        MembershipType type;

        switch (typeChoice) {
            case 1: type = MembershipType.STANDARD; break;
            case 2: type = MembershipType.PREMIUM; break;
            case 3: type = MembershipType.STUDENT; break;
            case 4: type = MembershipType.SENIOR; break;
            default:
                System.out.println("Lựa chọn không hợp lệ!");
                return;
        }

        Reader reader = new Reader(id, name, email, phone, type);
        library.registerReader(reader);
    }

    private static void removeReader() {
        String id = getStringInput("Nhập mã độc giả cần xóa: ");
        library.removeReader(id);
    }

    private static void viewBorrowHistory() {
        String readerId = getStringInput("Nhập mã độc giả: ");
        List<BorrowRecord> history = library.getBorrowHistory(readerId);

        if (history.isEmpty()) {
            System.out.println("Không có lịch sử mượn sách.");
            return;
        }

        System.out.println("\n--- LỊCH SỬ MƯỢN SÁCH ---");
        for (BorrowRecord record : history) {
            System.out.println(record.getInfo());
        }
    }

    // BORROW/RETURN OPERATIONS
    private static void borrowBook() {
        System.out.println("\n--- MƯỢN SÁCH ---");
        String readerId = getStringInput("Mã độc giả: ");
        String isbn = getStringInput("ISBN sách: ");

        library.borrowBook(readerId, isbn);
    }

    private static void returnBook() {
        System.out.println("\n--- TRẢ SÁCH ---");
        String recordId = getStringInput("Mã phiếu mượn: ");

        library.returnBook(recordId);
    }

    private static void extendBorrow() {
        System.out.println("\n--- GIA HẠN SÁCH ---");
        String recordId = getStringInput("Mã phiếu mượn: ");
        int days = getIntInput("Số ngày gia hạn: ");

        library.extendBorrow(recordId, days);
    }

    // UTILITY FUNCTION
    private static void initializeSampleData() {
        System.out.println("Đang khởi tạo dữ liệu mẫu...");

        // Thêm sách mẫu
        library.addBook(new TextBook("978-0-13-468599-1", "Clean Code", "Robert C. Martin", 5, "Programming", 12));
        library.addBook(new TextBook("978-0-13-468626-4", "Design Patterns", "Gang of Four", 3, "Software Engineering", 12));
        library.addBook(new ReferenceBook("978-0-13-468627-1", "The Pragmatic Programmer", "Andrew Hunt", 4, "Software Development"));
        library.addBook(new Magazine("978-0-13-468628-8", "Tech Monthly", "Various", 10, 202411));
        library.addBook(new TextBook("978-0-13-468629-5", "Introduction to Algorithms", "Thomas H. Cormen", 6, "Computer Science", 11));

        // Thêm độc giả mẫu
        library.registerReader(new Reader("R001", "Nguyễn Văn A", "vana@email.com", "0901234567", MembershipType.PREMIUM));
        library.registerReader(new Reader("R002", "Trần Thị B", "thib@email.com", "0902234567", MembershipType.STANDARD));
        library.registerReader(new Reader("R003", "Lê Văn C", "vanc@email.com", "0903234567", MembershipType.STUDENT));

        // Tạo một số giao dịch mượn mẫu
        library.borrowBook("R001", "978-0-13-468599-1");
        library.borrowBook("R002", "978-0-13-468626-4");
        library.borrowBook("R003", "978-0-13-468628-8");

        System.out.println(" Đã khởi tạo dữ liệu mẫu thành công!\n");
    }

    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Vui lòng nhập số hợp lệ!");
            }
        }
    }
}
