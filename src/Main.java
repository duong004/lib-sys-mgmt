import database.config.DatabaseConfig;
import database.impl.LibraryService;
import models.BorrowRecord;
import models.books.*;
import models.enums.MembershipType;
import models.people.Reader;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static LibraryService library;
    private static Scanner scanner;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);

        // Test database connection first
        System.out.println("========================================");
        System.out.println("       HỆ THỐNG QUẢN LÝ THƯ VIỆN");
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
        library = new LibraryService("Thư viện RHUST", "0 Đường ĐCV, HN");

        System.out.println(" Hệ thống đã sẵn sàng!");

        // Main menu loop
        while (true) {
            displayMainMenu();
            int menuChoice = getIntInput("Chọn chức năng: ");

            switch (menuChoice) {
                case 1: bookManagementMenu(); break;
                case 2: readerManagementMenu(); break;
                case 3: borrowReturnMenu(); break;
                case 4: reportMenu(); break;
                case 5:
                    System.out.println("\n========================================");
                    System.out.println("  Cảm ơn đã sử dụng hệ thống!");
                    System.out.println("========================================");
                    scanner.close();
                    return;
                default:
                    System.out.println(" Lựa chọn không hợp lệ!");
            }
        }
    }

    private static void displayMainMenu() {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║     HỆ THỐNG QUẢN LÝ THƯ VIỆN     ║");
        System.out.println("╚═══════════════════════════════════╝");
        System.out.println("1.  Quản lý Sách");
        System.out.println("2.  Quản lý Độc giả");
        System.out.println("3.  Mượn/Trả sách");
        System.out.println("4.  Báo cáo & Thống kê");
        System.out.println("5.  Thoát");
        System.out.println("────────────────────────────────────────");
    }

    // ========== BOOK MANAGEMENT MENU ==========

    private static void bookManagementMenu() {
        while (true) {
            System.out.println("\n╔═════════════════════════════════╗");
            System.out.println("║           QUẢN LÝ SÁCH          ║");
            System.out.println("╚═════════════════════════════════╝");
            System.out.println("1.  Thêm sách mới");
            System.out.println("2.  Xóa sách");
            System.out.println("3.  Tìm kiếm sách");
            System.out.println("4.  Hiển thị tất cả sách");
            System.out.println("5.  Quay lại");
            System.out.println("────────────────────────────────────────");

            int choice = getIntInput("Chọn: ");

            switch (choice) {
                case 1: addBook(); break;
                case 2: removeBook(); break;
                case 3: searchBook(); break;
                case 4: library.displayAllBooks(); break;
                case 5: return;
                default: System.out.println(" Lựa chọn không hợp lệ!");
            }
        }
    }

    private static void addBook() {
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("               THÊM SÁCH MỚI");
        System.out.println("═══════════════════════════════════════");
        System.out.println("Loại sách:");
        System.out.println("1.  Sách giáo khoa");
        System.out.println("2.  Sách tham khảo");
        System.out.println("3.  Tạp chí");

        int type = getIntInput("Chọn loại: ");

        String isbn = getStringInput("ISBN: ");

        // Check if ISBN already exists
        Book existingBook = library.searchByISBN(isbn);
        if (existingBook != null) {
            System.out.println(" ISBN này đã tồn tại trong hệ thống!");
            return;
        }

        String title = getStringInput("Tên sách: ");
        String author = getStringInput("Tác giả: ");
        int copies = getIntInput("Số lượng: ");

        if (copies <= 0) {
            System.out.println(" Số lượng phải lớn hơn 0!");
            return;
        }

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
                System.out.println(" Loại sách không hợp lệ!");
                return;
        }

        // Set additional info
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

        library.addBook(book);
    }

    private static void removeBook() {
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("                 XÓA SÁCH");
        System.out.println("═══════════════════════════════════════");
        String isbn = getStringInput("Nhập ISBN sách cần xóa: ");
        library.removeBook(isbn);
    }

    private static void searchBook() {
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("               TÌM KIẾM SÁCH");
        System.out.println("═══════════════════════════════════════");
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
            System.out.println("\n╔══════════════════════════════════╗");
            System.out.println("║          QUẢN LÝ ĐỘC GIẢ         ║");
            System.out.println("╚══════════════════════════════════╝");
            System.out.println("1.  Đăng ký độc giả mới");
            System.out.println("2.  Xóa độc giả");
            System.out.println("3.  Hiển thị tất cả độc giả");
            System.out.println("4.  Xem lịch sử mượn");
            System.out.println("5.  Quay lại");
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
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("          ĐĂNG KÝ ĐỘC GIẢ MỚI");
        System.out.println("═══════════════════════════════════════");

        System.out.println("(Mã độc giả sẽ được tự động tạo)\n");

        String name = getStringInput("Họ tên: ");
        String email = getStringInput("Email: ");
        String phone = getStringInput("Số điện thoại: ");

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

        Reader reader = new Reader(name, email, phone, type);

        // Validate email and phone
        if (!reader.validateEmail()) {
            System.out.println("  Cảnh báo: Email không hợp lệ!");
        }
        if (!reader.validatePhone()) {
            System.out.println("  Cảnh báo: Số điện thoại không hợp lệ!");
        }

        library.registerReader(reader);
    }

    private static void removeReader() {
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("               XÓA ĐỘC GIẢ");
        System.out.println("═══════════════════════════════════════");
        String id = getStringInput("Nhập mã độc giả cần xóa: ");
        library.removeReader(id);
    }

    private static void viewBorrowHistory() {
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("          LỊCH SỬ MƯỢN SÁCH");
        System.out.println("═══════════════════════════════════════");
        String readerId = getStringInput("Nhập mã độc giả: ");

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

    // ========== BORROW/RETURN MENU ==========

    private static void borrowReturnMenu() {
        while (true) {
            System.out.println("\n╔═══════════════════════════════╗");
            System.out.println("║          MƯỢN/TRẢ SÁCH        ║");
            System.out.println("╚═══════════════════════════════╝");
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
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("              MƯỢN SÁCH");
        System.out.println("═══════════════════════════════════════");
        String readerId = getStringInput("Mã độc giả: ");
        String isbn = getStringInput("ISBN sách: ");

        library.borrowBook(readerId, isbn);
    }

    private static void returnBook() {
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("               TRẢ SÁCH");
        System.out.println("═══════════════════════════════════════");
        String recordId = getStringInput("Mã phiếu mượn: ");

        library.returnBook(recordId);
    }

    private static void extendBorrow() {
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("             GIA HẠN SÁCH");
        System.out.println("═══════════════════════════════════════");
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
            System.out.println("\n╔══════════════════════════════════╗");
            System.out.println("║        BÁO CÁO & THỐNG KÊ        ║");
            System.out.println("╚══════════════════════════════════╝");
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