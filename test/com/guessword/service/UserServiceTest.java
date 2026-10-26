package com.guessword.service;

import com.guessword.Test;
import com.guessword.TestApp;
import com.guessword.model.Role;
import com.guessword.model.User;

import static com.guessword.Assert.assertEquals;
import static com.guessword.Assert.assertFalse;
import static com.guessword.Assert.assertThrows;
import static com.guessword.Assert.assertTrue;

public class UserServiceTest extends TestApp {

    @Test
    void registeredUsersArePlayers() {
        User user = app.users.registerPlayer("PlayerOne", "abc$1", "abc$1");

        assertEquals(Role.PLAYER, user.role());
        assertTrue(app.userRepository.findByUsername("PlayerOne").isPresent(), "user should be saved");
        assertFalse(user.passwordHash().contains("abc$1"), "password must be hashed");
    }

    @Test
    void registeredPlayerCanLogIn() {
        app.users.registerPlayer("PlayerOne", "abc$1", "abc$1");

        assertTrue(app.users.login("PlayerOne", "abc$1").isPresent(), "right password logs in");
        assertTrue(app.users.login("playerone", "abc$1").isPresent(), "username is not case-sensitive");
        assertFalse(app.users.login("PlayerOne", "wrong$1").isPresent(), "wrong password is refused");
        assertFalse(app.users.login("Nobody", "abc$1").isPresent(), "unknown user is refused");
    }

    @Test
    void seededAdminCanLogIn() {
        User admin = app.users.login("Admin", "Admin$123").orElseThrow();
        assertEquals(Role.ADMIN, admin.role());
    }

    @Test
    void badUsernameIsRejected() {
        for (String bad : new String[]{"Abcd", "lowercase", "UPPERCASE", "Player1", ""}) {
            assertThrows(AppException.class, "Username must be", () -> app.users.registerPlayer(bad, "abc$1", "abc$1"));
        }
    }

    @Test
    void weakPasswordIsRejected() {
        for (String bad : new String[]{"ab$1", "abcdef1", "abcdef$", "123456$"}) {
            assertThrows(AppException.class, "Password must be", () -> app.users.registerPlayer("PlayerOne", bad, bad));
        }
    }

    @Test
    void passwordsMustMatch() {
        assertThrows(AppException.class, "do not match", () -> app.users.registerPlayer("PlayerOne", "abc$1", "abc$2"));
        assertFalse(app.userRepository.existsByUsername("PlayerOne"), "nothing should be saved");
    }

    @Test
    void usernamesAreUniqueIgnoringCase() {
        app.users.registerPlayer("PlayerOne", "abc$1", "abc$1");
        assertThrows(AppException.class, "already taken", () -> app.users.registerPlayer("PLAYERone", "abc$1", "abc$1"));
        assertThrows(AppException.class, "already taken", () -> app.users.registerPlayer("Admin", "abc$1", "abc$1"));
    }

    @Test
    void playersListExcludesTheAdmin() {
        app.users.registerPlayer("PlayerOne", "abc$1", "abc$1");
        assertEquals(1, app.users.players().size());
        assertEquals("PlayerOne", app.users.players().get(0).username());
    }
}
