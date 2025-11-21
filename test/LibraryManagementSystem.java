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

