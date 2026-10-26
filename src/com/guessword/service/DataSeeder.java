package com.guessword.service;

import com.guessword.model.Role;
import com.guessword.repository.UserRepository;
import com.guessword.repository.WordRepository;

import java.util.List;

/** Puts the 20 starter words and the admin account into an empty database. */
public final class DataSeeder {

    public static final List<String> STARTER_WORDS = List.of(
            "APPLE", "BRAVE", "CRANE", "DREAM", "EAGLE",
            "FLAME", "GRAPE", "HOUSE", "JOKER", "LEMON",
            "MANGO", "NIGHT", "OCEAN", "PIANO", "QUEEN",
            "RIVER", "STONE", "TIGER", "TOWER", "WATER");

    /** Override with -Dadmin.username=... and -Dadmin.password=... when starting the app. */
    public static final String ADMIN_USERNAME = System.getProperty("admin.username", "Admin");
    public static final String ADMIN_PASSWORD = System.getProperty("admin.password", "Admin$123");

    private DataSeeder() {
    }

    public static void seed(WordRepository words, UserRepository users, UserService userService) {
        if (words.count() == 0) {
            STARTER_WORDS.forEach(words::insert);
        }
        if (!users.existsByUsername(ADMIN_USERNAME)) {
            userService.create(ADMIN_USERNAME, ADMIN_PASSWORD, Role.ADMIN);
        }
    }
}
