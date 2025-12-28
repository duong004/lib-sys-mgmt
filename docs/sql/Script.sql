-- ==================== LIBRARY DATABASE SCHEMA ====================
-- PostgreSQL 12+
-- Run this script to create all tables

-- Create database
CREATE DATABASE library_db;

-- ==================== BOOKS TABLE ====================
CREATE TABLE books (
    isbn VARCHAR(20) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    publisher VARCHAR(255),
    publish_year INTEGER,
    category VARCHAR(100),
    total_copies INTEGER NOT NULL DEFAULT 1 CHECK (total_copies >= 0),
    available_copies INTEGER NOT NULL DEFAULT 1 CHECK (available_copies >= 0),
    price DECIMAL(10, 2) DEFAULT 0.00,
    book_type VARCHAR(50) NOT NULL CHECK (book_type IN ('TextBook', 'ReferenceBook', 'Magazine')),
    extra_info JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_available_copies CHECK (available_copies <= total_copies)
);

-- Indexes for faster search
CREATE INDEX idx_books_title ON books USING GIN(to_tsvector('english', title));
CREATE INDEX idx_books_author ON books USING GIN(to_tsvector('english', author));
CREATE INDEX idx_books_category ON books(category);
CREATE INDEX idx_books_type ON books(book_type);
CREATE INDEX idx_books_extra_info ON books USING GIN(extra_info);

-- ==================== READERS TABLE ====================
CREATE TABLE readers (
    reader_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    date_of_birth DATE,
    membership_type VARCHAR(20) NOT NULL CHECK (membership_type IN ('STANDARD', 'PREMIUM', 'STUDENT', 'SENIOR')),
    registration_date DATE NOT NULL DEFAULT CURRENT_DATE,
    current_borrows INTEGER DEFAULT 0 CHECK (current_borrows >= 0),
    total_borrowed INTEGER DEFAULT 0 CHECK (total_borrowed >= 0),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_readers_email ON readers(email);
CREATE INDEX idx_readers_membership ON readers(membership_type);
CREATE INDEX idx_readers_active ON readers(is_active);
CREATE INDEX idx_readers_name ON readers USING GIN(to_tsvector('english', name));

-- ==================== LIBRARIANS TABLE ====================
CREATE TABLE librarians (
    employee_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    position VARCHAR(100),
    hire_date DATE DEFAULT CURRENT_DATE,
    salary DECIMAL(12, 2),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_librarians_email ON librarians(email);
CREATE INDEX idx_librarians_active ON librarians(is_active);

-- ==================== BORROW RECORDS TABLE ====================
CREATE TABLE borrow_records (
    record_id VARCHAR(30) PRIMARY KEY,
    reader_id VARCHAR(20) NOT NULL,
    isbn VARCHAR(20) NOT NULL,
    borrow_date DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date DATE NOT NULL,
    return_date DATE,
    status VARCHAR(20) NOT NULL CHECK (status IN ('BORROWED', 'RETURNED', 'OVERDUE', 'LOST')),
    fine DECIMAL(10, 2) DEFAULT 0.00 CHECK (fine >= 0),
    renewal_count INTEGER DEFAULT 0 CHECK (renewal_count >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign keys
    CONSTRAINT fk_reader FOREIGN KEY (reader_id) 
        REFERENCES readers(reader_id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT fk_book FOREIGN KEY (isbn) 
        REFERENCES books(isbn) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    
    -- Constraints
    CONSTRAINT check_due_date CHECK (due_date >= borrow_date),
    CONSTRAINT check_return_date CHECK (return_date IS NULL OR return_date >= borrow_date),
    CONSTRAINT check_renewal_count CHECK (renewal_count <= 2)
);

-- Indexes
CREATE INDEX idx_records_reader ON borrow_records(reader_id);
CREATE INDEX idx_records_isbn ON borrow_records(isbn);
CREATE INDEX idx_records_status ON borrow_records(status);
CREATE INDEX idx_records_due_date ON borrow_records(due_date);
CREATE INDEX idx_records_borrow_date ON borrow_records(borrow_date);

-- ==================== TRIGGERS ====================

-- Auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_books_updated_at 
    BEFORE UPDATE ON books
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_readers_updated_at 
    BEFORE UPDATE ON readers
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_librarians_updated_at 
    BEFORE UPDATE ON librarians
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_records_updated_at 
    BEFORE UPDATE ON borrow_records
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Auto-update overdue status
CREATE OR REPLACE FUNCTION check_overdue_records()
RETURNS void AS $$
BEGIN
    UPDATE borrow_records
    SET status = 'OVERDUE'
    WHERE status = 'BORROWED' 
    AND due_date < CURRENT_DATE;
END;
$$ LANGUAGE plpgsql;

-- ==================== VIEWS ====================

-- Active borrows view
CREATE OR REPLACE VIEW v_active_borrows AS
SELECT 
    br.record_id,
    r.reader_id,
    r.name AS reader_name,
    r.email AS reader_email,
    b.isbn,
    b.title AS book_title,
    b.author AS book_author,
    br.borrow_date,
    br.due_date,
    br.renewal_count,
    CASE 
        WHEN br.due_date < CURRENT_DATE THEN 'OVERDUE'
        WHEN br.due_date = CURRENT_DATE THEN 'DUE_TODAY'
        ELSE 'ACTIVE'
    END AS borrow_status,
    GREATEST(0, CURRENT_DATE - br.due_date) AS days_overdue
FROM borrow_records br
JOIN readers r ON br.reader_id = r.reader_id
JOIN books b ON br.isbn = b.isbn
WHERE br.status = 'BORROWED'
ORDER BY br.due_date;

-- Book statistics view
CREATE OR REPLACE VIEW v_book_statistics AS
SELECT 
    b.isbn,
    b.title,
    b.author,
    b.category,
    b.total_copies,
    b.available_copies,
    COUNT(br.record_id) AS total_borrows,
    COUNT(CASE WHEN br.status = 'BORROWED' THEN 1 END) AS current_borrows,
    COUNT(CASE WHEN br.status = 'RETURNED' THEN 1 END) AS total_returns,
    COALESCE(AVG(CASE 
        WHEN br.return_date IS NOT NULL 
        THEN br.return_date - br.borrow_date 
    END), 0) AS avg_borrow_days
FROM books b
LEFT JOIN borrow_records br ON b.isbn = br.isbn
GROUP BY b.isbn, b.title, b.author, b.category, b.total_copies, b.available_copies
ORDER BY total_borrows DESC;

-- Reader statistics view
CREATE OR REPLACE VIEW v_reader_statistics AS
SELECT 
    r.reader_id,
    r.name,
    r.email,
    r.membership_type,
    r.current_borrows,
    r.total_borrowed,
    COUNT(br.record_id) AS verified_borrows,
    COUNT(CASE WHEN br.status = 'BORROWED' THEN 1 END) AS currently_borrowing,
    COUNT(CASE WHEN br.status = 'OVERDUE' THEN 1 END) AS overdue_count,
    COALESCE(SUM(br.fine), 0) AS total_fines
FROM readers r
LEFT JOIN borrow_records br ON r.reader_id = br.reader_id
WHERE r.is_active = TRUE
GROUP BY r.reader_id, r.name, r.email, r.membership_type, 
         r.current_borrows, r.total_borrowed
ORDER BY r.total_borrowed DESC;

-- Overdue report view
CREATE OR REPLACE VIEW v_overdue_report AS
SELECT 
    br.record_id,
    r.reader_id,
    r.name AS reader_name,
    r.email AS reader_email,
    r.phone AS reader_phone,
    b.isbn,
    b.title AS book_title,
    br.borrow_date,
    br.due_date,
    br.fine,
    CURRENT_DATE - br.due_date AS days_overdue,
    (CURRENT_DATE - br.due_date) * 5000 AS calculated_fine
FROM borrow_records br
JOIN readers r ON br.reader_id = r.reader_id
JOIN books b ON br.isbn = b.isbn
WHERE br.status IN ('BORROWED', 'OVERDUE')
AND br.due_date < CURRENT_DATE
ORDER BY days_overdue DESC;

-- ==================== STORED PROCEDURES ====================

-- Calculate fine for a borrow record
CREATE OR REPLACE FUNCTION calculate_fine(p_record_id VARCHAR)
RETURNS DECIMAL AS $$
DECLARE
    v_fine DECIMAL;
    v_days_late INTEGER;
BEGIN
    SELECT 
        GREATEST(0, CURRENT_DATE - due_date) * 5000
    INTO v_fine
    FROM borrow_records
    WHERE record_id = p_record_id;
    
    RETURN COALESCE(v_fine, 0);
END;
$$ LANGUAGE plpgsql;

-- Get borrow limit for membership type
CREATE OR REPLACE FUNCTION get_borrow_limit(p_membership_type VARCHAR)
RETURNS INTEGER AS $$
BEGIN
    RETURN CASE p_membership_type
        WHEN 'STANDARD' THEN 3
        WHEN 'PREMIUM' THEN 10
        WHEN 'STUDENT' THEN 5
        WHEN 'SENIOR' THEN 5
        ELSE 3
    END;
END;
$$ LANGUAGE plpgsql;

-- Check if reader can borrow
CREATE OR REPLACE FUNCTION can_reader_borrow(p_reader_id VARCHAR)
RETURNS BOOLEAN AS $$
DECLARE
    v_current_borrows INTEGER;
    v_borrow_limit INTEGER;
    v_membership VARCHAR;
    v_is_active BOOLEAN;
BEGIN
    SELECT current_borrows, membership_type, is_active
    INTO v_current_borrows, v_membership, v_is_active
    FROM readers
    WHERE reader_id = p_reader_id;
    
    IF NOT FOUND OR NOT v_is_active THEN
        RETURN FALSE;
    END IF;
    
    v_borrow_limit := get_borrow_limit(v_membership);
    
    RETURN v_current_borrows < v_borrow_limit;
END;
$$ LANGUAGE plpgsql;

-- ==================== SAMPLE DATA ====================

-- Insert sample books
INSERT INTO books (isbn, title, author, publisher, publish_year, category, total_copies, available_copies, price, book_type, extra_info)
VALUES 
    ('978-0-13-468599-1', 'Clean Code', 'Robert C. Martin', 'Prentice Hall', 2008, 'Sách giáo khoa', 5, 5, 450000, 'TextBook', 
     '{"subject":"Programming","grade":12,"edition":1}'::jsonb),
    
    ('978-0-13-468626-4', 'Design Patterns', 'Gang of Four', 'Addison-Wesley', 1994, 'Sách giáo khoa', 3, 3, 550000, 'TextBook', 
     '{"subject":"Software Engineering","grade":12,"edition":1}'::jsonb),
    
    ('978-0-13-468627-1', 'The Pragmatic Programmer', 'Andrew Hunt', 'Addison-Wesley', 1999, 'Sách tham khảo', 4, 4, 400000, 'ReferenceBook', 
     '{"topic":"Software Development","canBorrow":true}'::jsonb),
    
    ('978-0-13-468628-8', 'Tech Monthly', 'Various', 'Tech Publishers', 2024, 'Tạp chí', 10, 10, 50000, 'Magazine', 
     '{"issueNumber":202411,"frequency":"Monthly"}'::jsonb),
    
    ('978-0-13-468629-5', 'Introduction to Algorithms', 'Thomas H. Cormen', 'MIT Press', 2009, 'Sách giáo khoa', 6, 6, 800000, 'TextBook', 
     '{"subject":"Computer Science","grade":11,"edition":3}'::jsonb);

-- Insert sample readers
INSERT INTO readers (reader_id, name, email, phone, address, membership_type, current_borrows, total_borrowed)
VALUES 
    ('R001', 'Nguyễn Văn A', 'vana@email.com', '0901234567', '123 Đường ABC, Hà Nội', 'PREMIUM', 0, 0),
    ('R002', 'Trần Thị B', 'thib@email.com', '0902234567', '456 Đường DEF, Hà Nội', 'STANDARD', 0, 0),
    ('R003', 'Lê Văn C', 'vanc@email.com', '0903234567', '789 Đường GHI, Hà Nội', 'STUDENT', 0, 0),
    ('R004', 'Phạm Thị D', 'thid@email.com', '0904234567', '321 Đường JKL, Hà Nội', 'SENIOR', 0, 0);

-- Insert sample librarians
INSERT INTO librarians (employee_id, name, email, phone, position, salary)
VALUES 
    ('LIB001', 'Hoàng Thị E', 'thie@library.com', '0905234567', 'Trưởng phòng', 15000000),
    ('LIB002', 'Đỗ Văn F', 'vanf@library.com', '0906234567', 'Thủ thư', 10000000);

-- ==================== UTILITY QUERIES ====================

-- Check database status
CREATE OR REPLACE VIEW v_database_status AS
SELECT 
    'Books' AS entity,
    COUNT(*) AS total,
    SUM(total_copies) AS total_copies,
    SUM(available_copies) AS available_copies
FROM books
UNION ALL
SELECT 
    'Readers',
    COUNT(*),
    NULL,
    NULL
FROM readers WHERE is_active = TRUE
UNION ALL
SELECT 
    'Active Borrows',
    COUNT(*),
    NULL,
    NULL
FROM borrow_records WHERE status = 'BORROWED'
UNION ALL
SELECT 
    'Overdue',
    COUNT(*),
    NULL,
    NULL
FROM borrow_records WHERE status IN ('BORROWED', 'OVERDUE') AND due_date < CURRENT_DATE;


-- Verify installation
SELECT 'Database setup completed successfully!' AS status;

-- Add status column to readers table
-- Run this migration script to update your existing database

-- Add status column with default value
ALTER TABLE readers 
ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE';

-- Update existing records to have ACTIVE status
UPDATE readers 
SET status = 'ACTIVE' 
WHERE status IS NULL;

-- Add check constraint for valid status values
ALTER TABLE readers 
ADD CONSTRAINT check_reader_status 
CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'));

-- Create index on status for faster queries
CREATE INDEX IF NOT EXISTS idx_readers_status ON readers(status);

-- Sync existing is_active with status
UPDATE readers 
SET status = CASE 
    WHEN is_active = true THEN 'ACTIVE'
    ELSE 'INACTIVE'
END
WHERE status = 'ACTIVE' AND is_active = false;

-- Show results
SELECT reader_id, name, status, is_active 
FROM readers 
ORDER BY reader_id;

-- Create book_inventory_logs table for tracking inventory changes

CREATE TABLE IF NOT EXISTS book_inventory_logs (
    log_id BIGSERIAL PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL,
    quantity_change INTEGER NOT NULL,
    total_copies_after INTEGER NOT NULL,
    action_type VARCHAR(20) NOT NULL CHECK (action_type IN ('ADD_NEW', 'INCREASE_STOCK', 'DECREASE_STOCK')),
    performed_by VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    
    -- Foreign key to books table
    CONSTRAINT fk_inventory_log_isbn 
        FOREIGN KEY (isbn) REFERENCES books(isbn) 
        ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_inventory_logs_isbn ON book_inventory_logs(isbn);
CREATE INDEX IF NOT EXISTS idx_inventory_logs_performer ON book_inventory_logs(performed_by);
CREATE INDEX IF NOT EXISTS idx_inventory_logs_timestamp ON book_inventory_logs(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_inventory_logs_action_type ON book_inventory_logs(action_type);

-- Add comment
COMMENT ON TABLE book_inventory_logs IS 'Tracks all inventory changes for books (additions, stock increases/decreases)';
COMMENT ON COLUMN book_inventory_logs.quantity_change IS 'Positive for increase, negative for decrease';
COMMENT ON COLUMN book_inventory_logs.action_type IS 'ADD_NEW: new book, INCREASE_STOCK: restock, DECREASE_STOCK: damaged/lost';

-- Create users table for authentication and authorization

CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'LIBRARIAN', 'READER')),
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    linked_entity_id VARCHAR(50), -- Reader ID if role is READER
    

);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_linked_entity ON users(linked_entity_id);

-- Add comments
COMMENT ON TABLE users IS 'User accounts for system authentication and authorization';
COMMENT ON COLUMN users.password_hash IS 'SHA-256 hashed password (Base64 encoded)';
COMMENT ON COLUMN users.linked_entity_id IS 'Links to reader_id if role is READER, null otherwise';

-- Insert default admin user (username: admin, password: admin123)
INSERT INTO users (username, password_hash, role, full_name, email) 
VALUES (
    'admin', 
    'JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=', -- SHA-256 hash of "admin123"
    'ADMIN', 
    'System Administrator', 
    'admin@library.com'
) ON CONFLICT (username) DO NOTHING;

-- Insert default librarian (username: librarian, password: lib123)
INSERT INTO users (username, password_hash, role, full_name, email) 
VALUES (
    'librarian', 
    'ZRhFSkmrKRIji1ELIiHw/BzkBJhtP7lLs0MR/2Bp1Gc=', 
    'LIBRARIAN', 
    'Default Librarian', 
    'librarian@library.com'
) ON CONFLICT (username) DO NOTHING;


