import database.config.DatabaseConfig;
import database.dao.UserDAO;
import database.impl.LibraryService;
import database.impl.UserDAOImpl;
import models.books.*;
import models.people.*;
import models.BorrowRecord;
import models.enums.MembershipType;
import models.enums.UserRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static LibraryService library;
    private static Scanner scanner;
    private static User currentUser; // Currently logged-in user
    private static UserDAO userDAO;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        userDAO = new UserDAOImpl();

        // Test database connection first
        System.out.println("========================================");
        System.out.println("   HỆ THỐNG QUẢN LÝ THƯ VIỆN");
        System.out.println("========================================");
        System.out.println("\nĐang kiểm tra kết nối database...");

        if (!DatabaseConfig.testConnection()) {
            System.err.println("\n Không thể kết nối database!");
            System.err.println("Vui lòng kiểm tra:");
            System.err.println("1. PostgreSQL đã chạy chưa");
            System.err.println("2. Database 'library_db' đã tạo chưa");
            System.err.println("3. Thông tin trong config.properties đúng chưa");
            scanner.close();
            return;
        }

        System.out.println(" Kết nối database thành công!\n");

        // Initialize library service
        library = new LibraryService("Thư viện Trung tâm", "123 Đường ABC, Hà Nội");

        // Login required
        if (!login()) {
            System.out.println("\n Đăng nhập thất bại! Thoát chương trình.");
            scanner.close();
            return;
        }

        // Show menu based on role
        showMenuByRole();

        System.out.println("\n========================================");
        System.out.println("  Cảm ơn đã sử dụng hệ thống!");
        System.out.println("========================================");
        scanner.close();
    }


    // ========== LOGIN & AUTHENTICATION ==========

    private static boolean login() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║          ĐĂNG NHẬP HỆ THỐNG          ║");
        System.out.println("╚══════════════════════════════════════╝\n");

        int attempts = 0;
        int maxAttempts = 3;

        while (attempts < maxAttempts) {
            String username = getStringInput("Tên đăng nhập: ");
            String password = getPasswordInput("Mật khẩu: ");

            try {
                currentUser = userDAO.authenticate(username, password);

                if (currentUser != null) {
                    // Update last login
                    userDAO.updateLastLogin(username);
                    currentUser.updateLastLogin();

                    System.out.println("\n Đăng nhập thành công!");
                    System.out.println("Xin chào, " + currentUser.getFullName() + " (" + currentUser.getRole().getDisplayName() + ")");
                    System.out.println(" Hệ thống đã sẵn sàng!\n");
                    return true;
                } else {
                    attempts++;
                    System.out.println(" Sai tên đăng nhập hoặc mật khẩu!");
                    System.out.println("Còn " + (maxAttempts - attempts) + " lần thử.\n");
                }

            } catch (Exception e) {
                System.err.println(" Lỗi đăng nhập: " + e.getMessage());
                attempts++;
            }
        }

        return false;
    }

    private static String getPasswordInput(String prompt) {
        // For security, should use Console.readPassword() in real app
        // But Scanner works for this demo
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static void showMenuByRole() {
        switch (currentUser.getRole()) {
            case ADMIN:
                adminMenu();
                break;
            case LIBRARIAN:
                librarianMenu();
                break;
            case READER:
                readerMenu();
                break;
        }
    }

    // ========== ADMIN MENU ==========

    private static void adminMenu() {
        while (true) {
            System.out.println("\n╔══════════════════════════════════╗");
            System.out.println("║        MENU QUẢN TRỊ VIÊN        ║");
            System.out.println("╚══════════════════════════════════╝");
            System.out.println("1. Quản lý Sách");
            System.out.println("2. Quản lý Độc giả");
            System.out.println("3. Mượn/Trả sách");
            System.out.println("4. Báo cáo & Thống kê");
            System.out.println("5. Quản lý Người dùng");
            System.out.println("6. Đăng xuất");
            System.out.println("────────────────────────────────────────");

            int choice = getIntInput("Chọn chức năng: ");

            switch (choice) {
                case 1: bookManagementMenu(); break;
                case 2: readerManagementMenu(); break;
                case 3: borrowReturnMenu(); break;
                case 4: reportMenu(); break;
                case 5: userManagementMenu(); break;
                case 6: return;
                default: System.out.println(" Lựa chọn không hợp lệ!");
            }
        }
    }

    // ========== LIBRARIAN MENU ==========

    private static void librarianMenu() {
        while (true) {
            System.out.println("\n╔══════════════════════════════╗");
            System.out.println("║         MENU THỦ THƯ         ║");
            System.out.println("╚══════════════════════════════╝");
            System.out.println("1. Quản lý Sách");
            System.out.println("2. Quản lý Độc giả");
            System.out.println("3. Mượn/Trả sách");
            System.out.println("4. Báo cáo & Thống kê");
            System.out.println("5. Đăng xuất");
            System.out.println("────────────────────────────────────────");

            int choice = getIntInput("Chọn chức năng: ");

            switch (choice) {
                case 1: bookManagementMenu(); break;
                case 2: readerManagementMenu(); break;
                case 3: borrowReturnMenu(); break;
                case 4: reportMenu(); break;
                case 5: return;
                default: System.out.println(" Lựa chọn không hợp lệ!");
            }
        }
    }

    // ========== READER MENU ==========

    private static void readerMenu() {
        while (true) {
            System.out.println("\n╔════════════════════════════════╗");
            System.out.println("║          MENU ĐỘC GIẢ          ║");
            System.out.println("╚════════════════════════════════╝");
            System.out.println("1. Tìm kiếm sách");
            System.out.println("2. Xem sách đang mượn");
            System.out.println("3. Xem lịch sử mượn");
            System.out.println("4. Gia hạn sách");
            System.out.println("5. Đăng xuất");
            System.out.println("────────────────────────────────────────");

            int choice = getIntInput("Chọn chức năng: ");

            switch (choice) {
                case 1: searchBook(); break;
                case 2: viewMyBorrowedBooks(); break;
                case 3: viewMyBorrowHistory(); break;
                case 4: renewMyBook(); break;
                case 5: return;
                default: System.out.println(" Lựa chọn không hợp lệ!");
            }
        }
    }

    // ========== USER MANAGEMENT MENU (ADMIN ONLY) ==========

    private static void userManagementMenu() {
        if (!checkPermission("MANAGE_USERS")) return;

        while (true) {
            System.out.println("\n╔════════════════════════════════╗");
            System.out.println("║       QUẢN LÝ NGƯỜI DÙNG       ║");
            System.out.println("╚════════════════════════════════╝");
            System.out.println("1. Tạo tài khoản mới");
            System.out.println("2. Xem danh sách người dùng");
            System.out.println("3. Khóa/Mở khóa tài khoản");
            System.out.println("4. Đổi mật khẩu người dùng");
            System.out.println("5. Quay lại");
            System.out.println("────────────────────────────────────────");

            int choice = getIntInput("Chọn: ");

            switch (choice) {
                case 1: createUser(); break;
                case 2: listUsers(); break;
                case 3: toggleUserStatus(); break;
                case 4: changeUserPassword(); break;
                case 5: return;
                default: System.out.println(" Lựa chọn không hợp lệ!");
            }
        }
    }

    private static boolean checkPermission(String permission) {
        if (!currentUser.hasPermission(permission)) {
            System.out.println(" Bạn không có quyền thực hiện chức năng này!");
            return false;
        }
        return true;
    }

    private static void displayMainMenu() {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║     HỆ THỐNG QUẢN LÝ THƯ VIỆN     ║");
        System.out.println("╚═══════════════════════════════════╝");
        System.out.println("1. Quản lý Sách");
        System.out.println("2. Quản lý Độc giả");
        System.out.println("3. Mượn/Trả sách");
        System.out.println("4. Báo cáo & Thống kê");
        System.out.println("5. Thoát");
        System.out.println("────────────────────────────────────────");
    }

    // ========== BOOK MANAGEMENT MENU ==========

    private static void bookManagementMenu() {
        while (true) {
            System.out.println("\n╔════════════════════════════════╗");
            System.out.println("║          QUẢN LÝ SÁCH          ║");
            System.out.println("╚════════════════════════════════╝");
            System.out.println("1. Thêm sách mới");
            System.out.println("2. Xóa sách");
            System.out.println("3. Tìm kiếm sách");
            System.out.println("4. Hiển thị tất cả sách");
            System.out.println("5. Xem lịch sử nhập sách");
            System.out.println("6. Quay lại");
            System.out.println("────────────────────────────────────────");

            int choice = getIntInput("Chọn: ");

            switch (choice) {
                case 1: addBook(); break;
                case 2: removeBook(); break;
                case 3: searchBook(); break;
                case 4: library.displayAllBooks(); break;
                case 5: viewInventoryLogs(); break;
                case 6: return;
                default: System.out.println(" Lựa chọn không hợp lệ!");
            }
        }
    }

    private static void addBook() {
        if (!checkPermission("MANAGE_BOOKS")) return;

        System.out.println("\n═══════════════════════════════════");
        System.out.println("         NHẬP SÁCH VÀO KHO         ");
        System.out.println("═══════════════════════════════════");
        System.out.println("Bước 1: Kiểm tra ISBN\n");

        String isbn = getStringInput("ISBN: ");
        int quantity = getIntInput("Số lượng cần thêm: ");

        if (quantity <= 0) {
            System.out.println(" Số lượng phải lớn hơn 0!");
            return;
        }

        // Try to update existing book
        System.out.println("\n Đang kiểm tra ISBN trong hệ thống...");
        Book existingBook = library.addOrUpdateBookInventory(isbn, quantity, currentUser.getLinkedEntityId());

        if (existingBook != null) {
            // Book exists - quantity updated successfully
            System.out.println(" Cập nhật thành công!");
            existingBook.displayDetails();
            return;
        }

        // ISBN doesn't exist - need full information
        System.out.println("\n ISBN chưa tồn tại trong hệ thống.");
        System.out.println("Vui lòng nhập thông tin sách:\n");

        // Get book type
        System.out.println("Loại sách:");
        System.out.println("1. Sách giáo khoa");
        System.out.println("2. Sách tham khảo");
        System.out.println("3. Tạp chí");

        int type = getIntInput("Chọn loại: ");

        // Basic info
        String title = getStringInput("Tên sách: ");
        String author = getStringInput("Tác giả: ");

        Book book = null;

        switch (type) {
            case 1:
                String subject = getStringInput("Môn học: ");
                int grade = getIntInput("Lớp: ");
                book = new TextBook(isbn, title, author, quantity, subject, grade);
                break;
            case 2:
                String topic = getStringInput("Chủ đề: ");
                book = new ReferenceBook(isbn, title, author, quantity, topic);
                break;
            case 3:
                int issueNumber = getIntInput("Số phát hành: ");
                book = new Magazine(isbn, title, author, quantity, issueNumber);
                break;
            default:
                System.out.println(" Loại sách không hợp lệ!");
                return;
        }

        // Optional info
        System.out.println("\n--- Thông tin bổ sung (có thể bỏ qua) ---");
        String publisher = getStringInput("Nhà xuất bản (Enter để bỏ qua): ");
        if (!publisher.isEmpty()) {
            book.setPublisher(publisher);
        }

        int year = getIntInput("Năm xuất bản (0 để bỏ qua): ");
        if (year > 0) {
            book.setPublishYear(year);
        }

        double price = getDoubleInput("Giá (0 để bỏ qua): ");
        if (price > 0) {
            book.setPrice(price);
        }

        // Save new book
        library.addBook(book, currentUser.getLinkedEntityId());
        System.out.println("\n Đã thêm sách mới với ISBN: " + isbn);
        book.displayDetails();
    }

    private static void removeBook() {
        if (!checkPermission("MANAGE_BOOKS")) return;

        System.out.println("\n══════════════════════════════");
        System.out.println("           XÓA SÁCH           ");
        System.out.println("══════════════════════════════");
        String isbn = getStringInput("Nhập ISBN sách cần xóa: ");
        library.removeBook(isbn);
    }

    private static void searchBook() {
        System.out.println("\n═════════════════════════════════");
        System.out.println("          TÌM KIẾM SÁCH          ");
        System.out.println("═════════════════════════════════");
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
                    System.out.println("\n Tìm thấy sách:");
                    book.displayDetails();
                } else {
                    System.out.println(" Không tìm thấy sách.");
                }
                break;
            case 4:
                String category = getStringInput("Nhập thể loại: ");
                displaySearchResults(library.searchByCategory(category));
                break;
            default:
                System.out.println(" Lựa chọn không hợp lệ!");
        }
    }

    private static void displaySearchResults(List<Book> results) {
        if (results.isEmpty()) {
            System.out.println(" Không tìm thấy kết quả.");
            return;
        }
        System.out.println("\n KẾT QUẢ TÌM KIẾM (" + results.size() + " kết quả)");
        System.out.println("═══════════════════════════════════════");
        for (Book book : results) {
            System.out.println(book.getInfo());
        }
    }

    // ========== READER MANAGEMENT MENU ==========

    private static void readerManagementMenu() {
        while (true) {
            System.out.println("\n╔═════════════════════════════════╗");
            System.out.println("║         QUẢN LÝ ĐỘC GIẢ         ║");
            System.out.println("╚═════════════════════════════════╝");
            System.out.println("1. Đăng ký độc giả mới");
            System.out.println("2. Xóa độc giả");
            System.out.println("3. Hiển thị tất cả độc giả");
            System.out.println("4. Xem lịch sử mượn");
            System.out.println("5. Quay lại");
            System.out.println("────────────────────────────────────────");

            int choice = getIntInput("Chọn: ");

            switch (choice) {
                case 1: registerReader(); break;
                case 2: removeReader(); break;
                case 3: library.displayAllReaders(); break;
                case 4: viewBorrowHistory(); break;
                case 5: return;
                default: System.out.println(" Lựa chọn không hợp lệ!");
            }
        }
    }

    private static void registerReader() {
        if (!checkPermission("MANAGE_READERS")) return;

        System.out.println("\n═════════════════════════════════");
        System.out.println("       ĐĂNG KÝ ĐỘC GIẢ MỚI       ");
        System.out.println("═════════════════════════════════");
        System.out.println("(Mã độc giả sẽ được tự động tạo)\n");

        String name = getStringInput("Họ tên: ");
        String email = getStringInput("Email: ");
        String phone = getStringInput("Số điện thoại: ");

        // Check if email already exists as username
        try {
            if (userDAO.exists(email)) {
                System.out.println(" Email này đã được dùng làm tài khoản!");
                System.out.print("Bạn có muốn chỉ tạo Reader không tạo User? (y/n): ");
                String choice = scanner.nextLine().trim().toLowerCase();
                if (!choice.equals("y")) {
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println(" Không thể kiểm tra email: " + e.getMessage());
        }

        System.out.println("\nLoại thành viên:");
        System.out.println("1. STANDARD (3 sách, 0% giảm giá)");
        System.out.println("2. PREMIUM (10 sách, 15% giảm giá)");
        System.out.println("3. STUDENT (5 sách, 10% giảm giá)");
        System.out.println("4. SENIOR (5 sách, 20% giảm giá)");

        int typeChoice = getIntInput("Chọn: ");
        MembershipType type;

        switch (typeChoice) {
            case 1: type = MembershipType.STANDARD; break;
            case 2: type = MembershipType.PREMIUM; break;
            case 3: type = MembershipType.STUDENT; break;
            case 4: type = MembershipType.SENIOR; break;
            default:
                System.out.println(" Lựa chọn không hợp lệ!");
                return;
        }

        // Create reader WITHOUT id - will be auto-generated
        Reader reader = new Reader(name, email, phone, type);

        // Validate email and phone
        if (!reader.validateEmail()) {
            System.out.println(" Cảnh báo: Email không hợp lệ!");
            System.out.print("Tiếp tục? (y/n): ");
            if (!scanner.nextLine().trim().toLowerCase().equals("y")) {
                return;
            }
        }
        if (!reader.validatePhone()) {
            System.out.println(" Cảnh báo: Số điện thoại không hợp lệ!");
        }

        // Save reader to database
        //library.registerReader(reader);
        String readerId = reader.getId();
        System.out.println(" Mã độc giả: " + readerId);

        // AUTO-CREATE USER ACCOUNT
        System.out.println("\n Tạo tài khoản đăng nhập cho độc giả...");
        System.out.println("   Username: " + email);

        String defaultPassword = "reader123"; // Default password
        System.out.println("   Mật khẩu mặc định: " + defaultPassword);
        System.out.print("\nBạn có muốn đặt mật khẩu khác? (y/n): ");
        String changePass = scanner.nextLine().trim().toLowerCase();

        String password = defaultPassword;
        if (changePass.equals("y")) {
            password = getPasswordInput("Mật khẩu mới: ");
        }

        try {
            // Create user account with email as username
            if (library.registerReaderWithAccount(reader, password)) {
                System.out.println("\n Đăng ký thành công!");
                System.out.println(" Username: " + email);
                System.out.println(" Mã độc giả: " + reader.getId());
            } else {
                System.out.println(" Đăng ký thất bại!");
            }

        } catch (Exception e) {
            System.err.println(" Đã tạo Reader nhưng không thể tạo User account: " + e.getMessage());
            System.out.println("   Bạn có thể tạo User account sau qua menu Quản lý Người dùng.");
        }
    }

    private static void removeReader() {
        if (!checkPermission("MANAGE_READERS")) return;

        System.out.println("\n═════════════════════════════════");
        System.out.println("           XÓA ĐỘC GIẢ           ");
        System.out.println("═════════════════════════════════");
        String id = getStringInput("Nhập mã độc giả cần xóa: ");
        library.removeReader(id);
    }

    private static void viewBorrowHistory() {
        System.out.println("\n═════════════════════════════════");
        System.out.println("        LỊCH SỬ MƯỢN SÁCH        ");
        System.out.println("═════════════════════════════════");
        String readerId = getStringInput("Nhập mã độc giả: ");
        viewBorrowHistoryForReader(readerId);
    }

    // ========== BORROW/RETURN MENU ==========

    private static void borrowReturnMenu() {
        while (true) {
            System.out.println("\n╔═════════════════════════════════╗");
            System.out.println("║          MƯỢN/TRẢ SÁCH          ║");
            System.out.println("╚═════════════════════════════════╝");
            System.out.println("1. Mượn sách");
            System.out.println("2. Trả sách");
            System.out.println("3. Gia hạn sách");
            System.out.println("4. Quay lại");
            System.out.println("────────────────────────────────────────");

            int choice = getIntInput("Chọn: ");

            switch (choice) {
                case 1: borrowBook(); break;
                case 2: returnBook(); break;
                case 3: extendBorrow(); break;
                case 4: return;
                default: System.out.println(" Lựa chọn không hợp lệ!");
            }
        }
    }

    private static void borrowBook() {
        System.out.println("\n═══════════════════════════════");
        System.out.println("           MƯỢN SÁCH           ");
        System.out.println("═══════════════════════════════");
        String readerId = getStringInput("Mã độc giả: ");
        String isbn = getStringInput("ISBN sách: ");

        library.borrowBook(readerId, isbn);
    }

    private static void returnBook() {
        System.out.println("\n══════════════════════════════");
        System.out.println("           TRẢ SÁCH           ");
        System.out.println("══════════════════════════════");
        String recordId = getStringInput("Mã phiếu mượn: ");

        library.returnBook(recordId);
    }

    private static void extendBorrow() {
        System.out.println("\n════════════════════════════════");
        System.out.println("          GIA HẠN SÁCH          ");
        System.out.println("════════════════════════════════");
        String recordId = getStringInput("Mã phiếu mượn: ");
        int days = getIntInput("Số ngày gia hạn (1-14): ");

        if (days < 1 || days > 14) {
            System.out.println(" Số ngày gia hạn không hợp lệ!");
            return;
        }

        library.extendBorrow(recordId, days);
    }

    // ========== REPORT MENU ==========

    private static void reportMenu() {
        while (true) {
            System.out.println("\n╔════════════════════════════════╗");
            System.out.println("║       BÁO CÁO & THỐNG KÊ       ║");
            System.out.println("╚════════════════════════════════╝");
            System.out.println("1. Top sách được mượn nhiều");
            System.out.println("2. Top độc giả tích cực");
            System.out.println("3. Danh sách sách quá hạn");
            System.out.println("4. Thống kê tổng quan");
            System.out.println("5. Quay lại");
            System.out.println("────────────────────────────────────────");

            int choice = getIntInput("Chọn: ");

            switch (choice) {
                case 1: library.generatePopularBooksReport(); break;
                case 2: library.generateActiveReadersReport(); break;
                case 3: library.generateOverdueReport(); break;
                case 4: library.generateMonthlyStatistics(); break;
                case 5: return;
                default: System.out.println(" Lựa chọn không hợp lệ!");
            }
        }
    }

    // ========== UTILITY FUNCTIONS ==========

    // Reader-specific functions
    private static void viewMyBorrowedBooks() {
        if (currentUser.getLinkedEntityId() == null) {
            System.out.println(" Tài khoản chưa được liên kết với độc giả!");
            return;
        }

        String readerId = currentUser.getLinkedEntityId();
        List<BorrowRecord> records = library.getBorrowHistory(readerId);

        List<BorrowRecord> borrowing = new ArrayList<>();
        for (BorrowRecord record : records) {
            if (record.getStatus() == models.enums.BorrowStatus.BORROWED) {
                borrowing.add(record);
            }
        }

        if (borrowing.isEmpty()) {
            System.out.println(" Bạn chưa mượn sách nào.");
            return;
        }

        System.out.println("\n SÁCH BẠN ĐANG MƯỢN:");
        System.out.println("═══════════════════════════════════════");
        for (BorrowRecord record : borrowing) {
            System.out.println(record.getInfo());
            if (record.isOverdue()) {
                System.out.println("  QUÁ HẠN: " + record.getDaysLate() + " ngày");
            }
        }
    }

    private static void viewMyBorrowHistory() {
        if (currentUser.getLinkedEntityId() == null) {
            System.out.println(" Tài khoản chưa được liên kết với độc giả!");
            return;
        }

        viewBorrowHistoryForReader(currentUser.getLinkedEntityId());
    }

    private static void renewMyBook() {
        if (currentUser.getLinkedEntityId() == null) {
            System.out.println(" Tài khoản chưa được liên kết với độc giả!");
            return;
        }

        System.out.println("\n════════════════════════════════");
        System.out.println("          GIA HẠN SÁCH          ");
        System.out.println("════════════════════════════════");
        String recordId = getStringInput("Mã phiếu mượn: ");

        // Verify this record belongs to current user
        try {
            BorrowRecord record = library.getBorrowHistory(currentUser.getLinkedEntityId())
                    .stream()
                    .filter(r -> r.getRecordId().equals(recordId))
                    .findFirst()
                    .orElse(null);

            if (record == null) {
                System.out.println(" Không tìm thấy phiếu mượn hoặc không thuộc về bạn!");
                return;
            }

            int days = getIntInput("Số ngày gia hạn (1-14): ");
            if (days < 1 || days > 14) {
                System.out.println(" Số ngày không hợp lệ!");
                return;
            }

            library.extendBorrow(recordId, days);

        } catch (Exception e) {
            System.err.println(" Lỗi: " + e.getMessage());
        }
    }

    // User management functions
    private static void createUser() {
        System.out.println("\n═════════════════════════════════");
        System.out.println("        TẠO TÀI KHOẢN MỚI        ");
        System.out.println("═════════════════════════════════");

        System.out.println("\nVai trò:");
        System.out.println("1. ADMIN");
        System.out.println("2. LIBRARIAN");
        System.out.println("3. READER (Đăng ký độc giả mới)");

        int roleChoice = getIntInput("Chọn: ");

        switch (roleChoice) {
            case 1: createAdminOrLibrarian(UserRole.ADMIN); break;
            case 2: createAdminOrLibrarian(UserRole.LIBRARIAN); break;
            case 3: createReaderAccount(); break;
            default:
                System.out.println(" Lựa chọn không hợp lệ!");
        }
    }

    private static void createAdminOrLibrarian(UserRole role) {
        System.out.println("\n--- Tạo tài khoản " + role.getDisplayName() + " ---\n");

        String username = getStringInput("Tên đăng nhập: ");

        try {
            if (userDAO.exists(username)) {
                System.out.println(" Tên đăng nhập đã tồn tại!");
                return;
            }

            String password = getPasswordInput("Mật khẩu: ");
            String fullName = getStringInput("Họ tên: ");
            String email = getStringInput("Email: ");

            User newUser = new User(username, password, role, fullName, email);
            userDAO.save(newUser);

            System.out.println("\n Đã tạo tài khoản: " + username);
            System.out.println("   Vai trò: " + role.getDisplayName());

        } catch (Exception e) {
            System.err.println(" Lỗi: " + e.getMessage());
        }
    }

    private static void createReaderAccount() {
        registerReader();
    }

    private static void listUsers() {
        try {
            List<User> users = userDAO.findAll();

            if (users.isEmpty()) {
                System.out.println(" Chưa có người dùng nào.");
                return;
            }

            System.out.println("\n════════════════════════════════");
            System.out.println("     DANH SÁCH NGƯỜI DÙNG       ");
            System.out.println("════════════════════════════════");
            for (User user : users) {
                System.out.println(user.getInfo());

                // If READER, show linked reader info
                if (user.getRole() == UserRole.READER && user.getLinkedEntityId() != null) {
                    Reader reader = library.findReaderById(user.getLinkedEntityId());
                    if (reader != null) {
                        System.out.println("  → Liên kết: " + reader.getInfo());
                    }
                }
            }

        } catch (Exception e) {
            System.err.println(" Lỗi: " + e.getMessage());
        }
    }

    private static void toggleUserStatus() {
        String username = getStringInput("Tên đăng nhập: ");

        try {
            User user = userDAO.findByUsername(username);
            if (user == null) {
                System.out.println(" Không tìm thấy người dùng!");
                return;
            }

            user.setActive(!user.isActive());
            userDAO.update(user);

            System.out.println(" Đã " + (user.isActive() ? "mở khóa" : "khóa") + " tài khoản: " + username);

        } catch (Exception e) {
            System.err.println(" Lỗi: " + e.getMessage());
        }
    }

    private static void changeUserPassword() {
        String username = getStringInput("Tên đăng nhập: ");

        try {
            if (!userDAO.exists(username)) {
                System.out.println(" Không tìm thấy người dùng!");
                return;
            }

            String newPassword = getPasswordInput("Mật khẩu mới: ");
            String newPasswordHash = User.hashPassword(newPassword);

            userDAO.changePassword(username, newPasswordHash);
            System.out.println(" Đã đổi mật khẩu cho: " + username);

        } catch (Exception e) {
            System.err.println(" Lỗi: " + e.getMessage());
        }
    }

    private static void viewBorrowHistoryForReader(String readerId) {
        Reader reader = library.findReaderById(readerId);
        if (reader == null) {
            System.out.println(" Không tìm thấy độc giả!");
            return;
        }

        List<BorrowRecord> history = library.getBorrowHistory(readerId);

        if (history.isEmpty()) {
            System.out.println(" Không có lịch sử mượn sách.");
            return;
        }

        System.out.println("\n Lịch sử mượn của: " + reader.getName());
        System.out.println("═══════════════════════════════════════");
        for (BorrowRecord record : history) {
            System.out.println(record.getInfo());
        }
    }

    private static void viewInventoryLogs() {
        if (!checkPermission("MANAGE_BOOKS")) return;

        System.out.println("\n═══════════════════════════════════");
        System.out.println("         LỊCH SỬ NHẬP SÁCH         ");
        System.out.println("═══════════════════════════════════");
        System.out.println("1. Xem tất cả lịch sử");
        System.out.println("2. Xem theo ISBN");
        System.out.println("3. Xem 20 lần nhập gần nhất");

        int choice = getIntInput("Chọn: ");

        try {
            database.dao.BookInventoryLogDAO logDAO = new database.impl.BookInventoryLogDAOImpl();
            java.util.List<models.BookInventoryLog> logs = null;

            switch (choice) {
                case 1:
                    logs = logDAO.findAll();
                    break;
                case 2:
                    String isbn = getStringInput("Nhập ISBN: ");
                    logs = logDAO.findByISBN(isbn);
                    break;
                case 3:
                    logs = logDAO.findRecent(20);
                    break;
                default:
                    System.out.println(" Lựa chọn không hợp lệ!");
                    return;
            }

            if (logs == null || logs.isEmpty()) {
                System.out.println(" Không có lịch sử nào.");
                return;
            }

            System.out.println("\n Tìm thấy " + logs.size() + " bản ghi:");
            System.out.println("═══════════════════════════════════════");
            for (models.BookInventoryLog log : logs) {
                System.out.println(log.getInfo());
            }

        } catch (java.sql.SQLException e) {
            System.err.println(" Lỗi khi xem lịch sử: " + e.getMessage());
        }
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
                System.out.println(" Vui lòng nhập số hợp lệ!");
            }
        }
    }

    private static double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                double value = Double.parseDouble(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println(" Vui lòng nhập số hợp lệ!");
            }
        }
    }
}