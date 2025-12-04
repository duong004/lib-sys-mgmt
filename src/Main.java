import models.BorrowRecord;
import models.books.*;
import models.people.*;
import models.enums.MembershipType;
import services.Library;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static Library library;
    private static Scanner scanner;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        library = new Library("Thư viện RHUST", "0 Đường ĐCV, HN");

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
                    scanner.close();
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

        System.out.println("✓ Đã khởi tạo dữ liệu mẫu thành công!\n");
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
