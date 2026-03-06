package com.ai.interview.security;

public record AuthenticatedUser(Long userId, String username, String role) {
}