package com.guessword;

import com.guessword.db.Database;
import com.guessword.ui.LoginFrame;
import com.guessword.ui.Theme;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.time.Clock;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        AppContext app;
        try {
            app = new AppContext(Database.DEFAULT_URL, Clock.systemDefaultZone());
        } catch (RuntimeException e) {
            // Most often: the app is already open in another window, which locks the database file.
            JOptionPane.showMessageDialog(null,
                    "Could not open the database.\nIs Guess the Word already running?\n\n" + e.getMessage(),
                    "Guess the Word", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(app::close));

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // The default look and feel is fine too.
            }
            Theme.installDefaults();
            new LoginFrame(app).setVisible(true);
        });
    }
}
