CREATE DATABASE IF NOT EXISTS ai_interview DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_interview;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @user_role_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'users'
      AND COLUMN_NAME = 'role'
);
SET @user_role_sql = IF(
    @user_role_column_exists = 0,
    'ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT ''USER'' AFTER nickname',
    'SELECT 1'
);
PREPARE stmt_user_role FROM @user_role_sql;
EXECUTE stmt_user_role;
DEALLOCATE PREPARE stmt_user_role;

ALTER TABLE users MODIFY COLUMN password VARCHAR(255) NOT NULL;
UPDATE users SET role = 'USER' WHERE role IS NULL OR role = '';

CREATE TABLE IF NOT EXISTS questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    question_type VARCHAR(30) NOT NULL,
    difficulty VARCHAR(30) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS practice_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_content TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_practice_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_practice_question FOREIGN KEY (question_id) REFERENCES questions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @practice_user_idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'practice_records'
      AND INDEX_NAME = 'idx_practice_user_created_at'
);
SET @practice_user_idx_sql = IF(
    @practice_user_idx_exists = 0,
    'ALTER TABLE practice_records ADD INDEX idx_practice_user_created_at (user_id, created_at)',
    'SELECT 1'
);
PREPARE stmt_practice_user_idx FROM @practice_user_idx_sql;
EXECUTE stmt_practice_user_idx;
DEALLOCATE PREPARE stmt_practice_user_idx;

SET @practice_question_idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'practice_records'
      AND INDEX_NAME = 'idx_practice_question_id'
);
SET @practice_question_idx_sql = IF(
    @practice_question_idx_exists = 0,
    'ALTER TABLE practice_records ADD INDEX idx_practice_question_id (question_id)',
    'SELECT 1'
);
PREPARE stmt_practice_question_idx FROM @practice_question_idx_sql;
EXECUTE stmt_practice_question_idx;
DEALLOCATE PREPARE stmt_practice_question_idx;

CREATE TABLE IF NOT EXISTS practice_reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    practice_record_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    overall_score INT NULL,
    summary TEXT NULL,
    highlight_excerpt TEXT NULL,
    strengths_json TEXT NULL,
    improvements_json TEXT NULL,
    follow_ups_json TEXT NULL,
    generator_name VARCHAR(64) NULL,
    error_message VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_practice_review_record UNIQUE (practice_record_id),
    CONSTRAINT fk_practice_review_record FOREIGN KEY (practice_record_id) REFERENCES practice_records(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @practice_review_score_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'practice_reviews'
      AND COLUMN_NAME = 'overall_score'
);
SET @practice_review_score_sql = IF(
    @practice_review_score_exists = 0,
    'ALTER TABLE practice_reviews ADD COLUMN overall_score INT NULL AFTER status',
    'SELECT 1'
);
PREPARE stmt_practice_review_score FROM @practice_review_score_sql;
EXECUTE stmt_practice_review_score;
DEALLOCATE PREPARE stmt_practice_review_score;

SET @practice_review_summary_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'practice_reviews'
      AND COLUMN_NAME = 'summary'
);
SET @practice_review_summary_sql = IF(
    @practice_review_summary_exists = 0,
    'ALTER TABLE practice_reviews ADD COLUMN summary TEXT NULL AFTER overall_score',
    'SELECT 1'
);
PREPARE stmt_practice_review_summary FROM @practice_review_summary_sql;
EXECUTE stmt_practice_review_summary;
DEALLOCATE PREPARE stmt_practice_review_summary;

SET @practice_review_excerpt_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'practice_reviews'
      AND COLUMN_NAME = 'highlight_excerpt'
);
SET @practice_review_excerpt_sql = IF(
    @practice_review_excerpt_exists = 0,
    'ALTER TABLE practice_reviews ADD COLUMN highlight_excerpt TEXT NULL AFTER summary',
    'SELECT 1'
);
PREPARE stmt_practice_review_excerpt FROM @practice_review_excerpt_sql;
EXECUTE stmt_practice_review_excerpt;
DEALLOCATE PREPARE stmt_practice_review_excerpt;

SET @practice_review_strengths_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'practice_reviews'
      AND COLUMN_NAME = 'strengths_json'
);
SET @practice_review_strengths_sql = IF(
    @practice_review_strengths_exists = 0,
    'ALTER TABLE practice_reviews ADD COLUMN strengths_json TEXT NULL AFTER highlight_excerpt',
    'SELECT 1'
);
PREPARE stmt_practice_review_strengths FROM @practice_review_strengths_sql;
EXECUTE stmt_practice_review_strengths;
DEALLOCATE PREPARE stmt_practice_review_strengths;

SET @practice_review_improvements_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'practice_reviews'
      AND COLUMN_NAME = 'improvements_json'
);
SET @practice_review_improvements_sql = IF(
    @practice_review_improvements_exists = 0,
    'ALTER TABLE practice_reviews ADD COLUMN improvements_json TEXT NULL AFTER strengths_json',
    'SELECT 1'
);
PREPARE stmt_practice_review_improvements FROM @practice_review_improvements_sql;
EXECUTE stmt_practice_review_improvements;
DEALLOCATE PREPARE stmt_practice_review_improvements;

SET @practice_review_follow_ups_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'practice_reviews'
      AND COLUMN_NAME = 'follow_ups_json'
);
SET @practice_review_follow_ups_sql = IF(
    @practice_review_follow_ups_exists = 0,
    'ALTER TABLE practice_reviews ADD COLUMN follow_ups_json TEXT NULL AFTER improvements_json',
    'SELECT 1'
);
PREPARE stmt_practice_review_follow_ups FROM @practice_review_follow_ups_sql;
EXECUTE stmt_practice_review_follow_ups;
DEALLOCATE PREPARE stmt_practice_review_follow_ups;

SET @practice_review_generator_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'practice_reviews'
      AND COLUMN_NAME = 'generator_name'
);
SET @practice_review_generator_sql = IF(
    @practice_review_generator_exists = 0,
    'ALTER TABLE practice_reviews ADD COLUMN generator_name VARCHAR(64) NULL AFTER follow_ups_json',
    'SELECT 1'
);
PREPARE stmt_practice_review_generator FROM @practice_review_generator_sql;
EXECUTE stmt_practice_review_generator;
DEALLOCATE PREPARE stmt_practice_review_generator;

SET @practice_review_error_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'practice_reviews'
      AND COLUMN_NAME = 'error_message'
);
SET @practice_review_error_sql = IF(
    @practice_review_error_exists = 0,
    'ALTER TABLE practice_reviews ADD COLUMN error_message VARCHAR(255) NULL AFTER generator_name',
    'SELECT 1'
);
PREPARE stmt_practice_review_error FROM @practice_review_error_sql;
EXECUTE stmt_practice_review_error;
DEALLOCATE PREPARE stmt_practice_review_error;

INSERT INTO questions (id, title, content, question_type, difficulty)
VALUES
    (1, 'Two Sum', 'Given an integer array nums and an integer target, return indices of two numbers such that they add up to target.', 'ALGORITHM', 'EASY'),
    (2, 'Reverse Linked List', 'Reverse a singly linked list and return the new head.', 'ALGORITHM', 'MEDIUM'),
    (3, 'Explain CAP Theorem', 'What is CAP theorem? How do distributed systems trade off consistency, availability and partition tolerance?', 'INTERVIEW', 'MEDIUM')
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    content = VALUES(content),
    question_type = VALUES(question_type),
    difficulty = VALUES(difficulty);