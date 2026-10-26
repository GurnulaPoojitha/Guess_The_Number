package com.guessword.service;

import com.guessword.model.Role;
import com.guessword.model.User;
import com.guessword.repository.UserRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserRepository users;
    private final Clock clock;

    public UserService(UserRepository users, Clock clock) {
        this.users = users;
        this.clock = clock;
    }

    /** Self-registration always creates a PLAYER; the admin account is created by {@link DataSeeder}. */
    public User registerPlayer(String username, String password, String confirmPassword) {
        validate(username, password);
        if (!password.equals(confirmPassword)) {
            throw new AppException("Passwords do not match.");
        }
        return create(username, password, Role.PLAYER);
    }

    public User create(String username, String password, Role role) {
        validate(username, password);
        if (users.existsByUsername(username)) {
            throw new AppException("Username '" + username + "' is already taken.");
        }
        return users.insert(username, PasswordHasher.hash(password), role, LocalDateTime.now(clock));
    }

    private static void validate(String username, String password) {
        if (!ValidationRules.isValidUsername(username)) {
            throw new AppException(ValidationRules.USERNAME_MESSAGE);
        }
        if (!ValidationRules.isValidPassword(password)) {
            throw new AppException(ValidationRules.PASSWORD_MESSAGE);
        }
    }

    /** Returns the user if the username and password are right. */
    public Optional<User> login(String username, String password) {
        return users.findByUsername(username == null ? "" : username.trim())
                .filter(u -> PasswordHasher.matches(password, u.passwordHash()));
    }

    public List<User> players() {
        return users.findByRole(Role.PLAYER);
    }
}
