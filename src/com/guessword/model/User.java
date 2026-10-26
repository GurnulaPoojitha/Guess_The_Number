package com.guessword.model;

import java.time.LocalDateTime;

public record User(long id, String username, String passwordHash, Role role, LocalDateTime createdAt) {

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    @Override
    public String toString() {
        return username; // shown in the admin's player drop-down
    }
}
