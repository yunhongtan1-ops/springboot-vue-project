CREATE DATABASE IF NOT EXISTS ai_interview DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_interview;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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