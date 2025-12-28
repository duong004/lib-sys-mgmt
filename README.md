# Hệ Thống Quản Lý Thư Viện (Library Management System)

### **Dự án cuối kỳ môn Lập trình hướng đối tượng (OOP)**
Một ứng dụng quản lý thư viện hiện đại, đa năng, được xây dựng bằng ngôn ngữ **Java**, giao diện **JavaFX** và cơ sở dữ liệu **PostgreSQL**. Hệ thống hỗ trợ quản lý toàn diện từ kho sách, độc giả, quá trình mượn trả đến các báo cáo thống kê chuyên sâu.

---

## Tính Năng Chính

### 1. Quản lý Người dùng & Phân quyền (RBAC)
*   **Đăng nhập hệ thống:** Bảo mật mật khẩu bằng thuật toán băm SHA-256.
*   **Phân quyền dựa trên vai trò (Role-based Access Control):**
   *   **Admin:** Quản trị toàn bộ hệ thống, quản lý tài khoản nhân viên (Librarian), theo dõi tình trạng hệ thống.
   *   **Librarian (Thủ thư):** Quản lý kho sách, đăng ký độc giả, xử lý giao dịch mượn/trả và xem báo cáo.
   *   **Reader (Độc giả):** Tra cứu sách, xem lịch sử mượn cá nhân và yêu cầu gia hạn sách trực tuyến.

### 2. Quản lý Kho sách (Inventory Management)
*   **Đa dạng loại hình:** Hỗ trợ Sách giáo khoa, Sách tham khảo, Tạp chí với các thuộc tính riêng biệt.
*   **Nhật ký nhập kho (Inventory Log):** Tự động ghi lại mọi biến động số lượng sách (ai nhập, lúc nào, số lượng bao nhiêu, lý do gì) để chống thất thoát.
*   **Tìm kiếm thông minh:** Tìm kiếm đa tiêu chí (ISBN, Tên sách, Tác giả, Thể loại).

### 3. Nghiệp vụ Mượn/Trả sách
*   **Quy trình mượn:** Kiểm tra điều kiện mượn (giới hạn thẻ, tình trạng sách) trước khi tạo phiếu.
*   **Quy trình trả:** Tự động tính toán ngày quá hạn và tiền phạt (fine) dựa trên loại sách.
*   **Gia hạn:** Cho phép gia hạn linh hoạt theo quy định của thư viện.

### 4. Báo cáo & Thống kê (Reporting)
*   Báo cáo Top 5 sách được mượn nhiều nhất.
*   Danh sách độc giả tích cực.
*   Thống kê sách quá hạn và tổng tiền phạt thu được.
*   Biểu đồ/Thống kê tổng quan về hiệu suất hoạt động theo tháng.

---

## Công Nghệ Sử Dụng
*   **Ngôn ngữ:** Java 17+ (hoặc Java 21).
*   **Giao diện người dùng:** JavaFX 21 với kiến trúc View-Controller tách biệt.
*   **Cơ sở dữ liệu:** PostgreSQL.
*   **Kết nối DB:** JDBC (Java Database Connectivity).
*   **Thiết kế:** DAO Pattern (Data Access Object), Singleton, Inheritance, Polymorphism.
*   **Styling:** CSS tùy chỉnh cho JavaFX (Modern, Dark/Light Mode).

---

## 📂 Cấu Trúc Thư Mục
```text
project/
├── src/
│   ├── Main.java               # Điểm chạy ứng dụng (Console Version)
│   ├── LibraryApp.java         # Khởi tạo JavaFX Application
│   ├── database/               # Tầng dữ liệu
│   │   ├── config/             # Cấu hình DB 
│   │   ├── dao/                # Các Interface định nghĩa thao tác CRUD
│   │   └── impl/               # Hiện thực hóa các DAO (SQL queries)
│   ├── models/                 # Các thực thể (Books, People, User, Records)
│   │   ├── books/              # Kế thừa: Book -> TextBook, Magazine...
│   │   ├── people/             # Kế thừa: Person -> Reader, Librarian, User
│   │   └── enums/              # Các hằng số (UserRole, MembershipType...)
│   ├── views/                  # Giao diện người dùng JavaFX
│   │   ├── admin/              # Giao diện dành riêng cho Admin
│   │   ├── librarian/          # Giao diện quản lý của Thủ thư
│   │   └── resources/          # Tài nguyên (Stylesheets, Images)
│   ├── services/               # Tầng xử lý nghiệp vụ tập trung (LibraryService)
│   └── interfaces/             # Định nghĩa Searchable, Reportable
└── README.md
```

---

## Cài Đặt & Chạy Ứng Dụng

### 1. **Clone project** về máy:
   ```bash
   git clone https://github.com/duong004/lib-sys-mgmt.git
   ```

### 2. Chuẩn bị Cơ sở dữ liệu
*   Cài đặt PostgreSQL và tạo một database mới tên là `library_db`.
*   Chạy các script SQL (trong thư mục `docs/sql`) hoặc tạo các bảng theo cấu trúc:
   *   `users`, `readers`, `librarians`, `books`, `borrow_records`, `book_inventory_logs`.

### 3. Cấu hình kết nối
*   Tạo file `src/database/config/config.properties`.
*   Chỉnh sửa thông tin đăng nhập phù hợp với máy của bạn:
    ```properties
    db.url=jdbc:postgresql://localhost:5432/library_db
    db.user=your_username
    db.password=your_password
    ```

### 4. Cài đặt Thư viện
*   Đảm bảo bạn đã thêm **PostgreSQL JDBC Driver** vào Project Libraries.
*   Đảm bảo cấu hình **JavaFX SDK** và thêm các `VM Options` nếu cần (đối với OpenJFX).

### 5. Chạy ứng dụng
*   Run `LibraryApp.java` để khởi động giao diện đồ họa (GUI).
*   Run `Main.java` để thử nghiệm phiên bản Console (nếu cần).

---

## Điểm Nổi Bật Về Giao Diện
*   **Responsive Sidebar:** Thanh menu bên trái tự co giãn, hỗ trợ ẩn thanh cuộn thô nhưng vẫn đảm bảo tính năng cuộn mượt mà.
*   **Eye-Friendly UI:** Sử dụng dải màu Deep Blue/Indigo (`#1e1b4b`) và hiệu ứng Blur nhẹ cho nền, giúp người dùng không bị mỏi mắt khi sử dụng lâu.
*   **Smart Alerts:** Các thông báo lỗi/thành công được xử lý an toàn bằng `Platform.runLater()` để không làm treo luồng xử lý đồ họa.
*   **Báo cáo Console-style:** Màn hình báo cáo trực quan với font chữ `Consolas` sắc nét trên nền tối.

---

## Nhật Ký Phát Triển (Changelog)
*   **v1.0.0:** Hoàn thiện core nghiệp vụ trên Console.
*   **v1.1.0:** Chuyển đổi sang JavaFX, áp dụng DAO Pattern để quản lý Database.
*   **v1.2.0:** Thêm hệ thống phân quyền User và Liên kết thực thể (Linked Entity ID).
*   **v1.3.0:** Tối ưu giao diện.

---

## Tác Giả
*   **Nhóm phát triển:** Group 23
*   **Học phần:** Lập trình hướng đối tượng - IT3100
*   Đại học Bách Khoa Hà Nội (HUST)

---
*Cảm ơn bạn đã quan tâm đến dự án của chúng tôi!*