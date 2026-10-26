package com.guessword.ui;

import com.guessword.AppContext;
import com.guessword.model.User;

import java.awt.Window;

/** Switches between the login window and each kind of user's home window. */
final class Navigator {

    private Navigator() {
    }

    static void openHome(Window current, AppContext app, User user) {
        current.dispose();
        if (user.isAdmin()) {
            new AdminFrame(app, user).setVisible(true);
        } else {
            new PlayerFrame(app, user).setVisible(true);
        }
    }

    static void logout(Window current, AppContext app) {
        current.dispose();
        new LoginFrame(app).setVisible(true);
    }
}
