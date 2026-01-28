-- College Attendance Automation Database Schema
-- This file is for reference only - Spring Boot will create tables automatically

-- Create database
CREATE DATABASE IF NOT EXISTS attendance_db;
USE attendance_db;

-- Students table
CREATE TABLE IF NOT EXISTS students (
    sl_no BIGINT AUTO_INCREMENT PRIMARY KEY,
    sic VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    added_on DATETIME NOT NULL,
    attendance_count INT NOT NULL DEFAULT 0,
    is_taken BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    pause_till DATE NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    
    INDEX idx_sic (sic),
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_is_taken (is_taken),
    INDEX idx_is_verified (is_verified)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sample queries for monitoring

-- View all active students
SELECT * FROM students WHERE status = 'ACTIVE';

-- View today's attendance status
SELECT 
    sic,
    email,
    attendance_count,
    CASE 
        WHEN is_taken = TRUE AND is_verified = TRUE THEN 'Marked & Verified'
        WHEN is_taken = TRUE AND is_verified = FALSE THEN 'Marked (Pending Verification)'
        ELSE 'Not Marked'
    END as today_status
FROM students
WHERE status = 'ACTIVE';

-- View attendance statistics
SELECT 
    status,
    COUNT(*) as total_students,
    SUM(attendance_count) as total_attendance,
    AVG(attendance_count) as avg_attendance
FROM students
GROUP BY status;

-- View students who need attention (not verified)
SELECT 
    sic,
    email,
    attendance_count
FROM students
WHERE is_taken = TRUE AND is_verified = FALSE;

-- View paused students
SELECT 
    sic,
    email,
    pause_till,
    DATEDIFF(pause_till, CURDATE()) as days_remaining
FROM students
WHERE status = 'PAUSED';

-- Reset daily flags (done automatically by the application)
-- UPDATE students SET is_taken = FALSE, is_verified = FALSE;

-- Activate students with expired pause
-- UPDATE students 
-- SET status = 'ACTIVE', pause_till = NULL 
-- WHERE status = 'PAUSED' AND pause_till <= CURDATE();
