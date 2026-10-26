package com.guessword.ui;

import com.guessword.AppContext;
import com.guessword.model.User;
import com.guessword.service.AppException;
import com.guessword.service.PlayerSummary;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.time.format.DateTimeFormatter;

/** The player's home window: today's words, a start/continue button and recent games. */
public class PlayerFrame extends JFrame {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final AppContext app;
    private final User user;
    private final JLabel today = new JLabel();
    private final JButton play = Theme.primaryButton("Start a new game");
    private final JLabel message = Theme.messageLabel();
    private final DefaultTableModel history = Tables.model("Date", "Word", "Result", "Guesses");

    public PlayerFrame(AppContext app, User user) {
        super("Guess the Word - " + user.username());
        this.app = app;
        this.user = user;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JButton logout = new JButton("Log out");
        logout.addActionListener(e -> Navigator.logout(this, app));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(Theme.title("Welcome, " + user.username()), BorderLayout.WEST);
        header.add(logout, BorderLayout.EAST);

        today.setFont(Theme.BODY.deriveFont(16f));
        play.addActionListener(e -> play());

        Column body = new Column()
                .add(header)
                .gap(14).add(today)
                .gap(10).add(play)
                .gap(6).add(message)
                .gap(12).add(Theme.hint("How to play: guess the hidden 5-letter word in up to 5 tries. "
                        + "Green = right letter, right spot. Orange = in the word, wrong spot. "
                        + "Grey = not in the word. You can play up to 3 words a day."))
                .label("Your recent games")
                .add(Tables.scroll(history, 200));
        body.setBorder(Theme.padding(20));

        getContentPane().setBackground(Theme.BACKGROUND);
        add(body);
        refresh();
        pack();
        setLocationRelativeTo(null);
    }

    private void refresh() {
        PlayerSummary summary = app.games.summary(user);
        today.setText("Words played today: " + summary.gamesToday() + " / " + summary.maxPerDay());

        if (summary.hasActiveGame()) {
            play.setText("Continue current game");
            play.setEnabled(true);
        } else if (summary.canStartNewGame()) {
            play.setText("Start a new game");
            play.setEnabled(true);
        } else {
            play.setText("No words left today");
            play.setEnabled(false);
            Theme.showSuccess(message, "You've played all your words for today. Come back tomorrow!");
        }

        history.setRowCount(0);
        for (PlayerSummary.HistoryItem item : summary.history()) {
            history.addRow(new Object[]{
                    DATE.format(item.date()),
                    item.word() == null ? "(unfinished)" : item.word(),
                    item.status().label(),
                    item.attempts()});
        }
    }

    private void play() {
        message.setText(" ");
        try {
            long gameId = app.games.startOrResumeGame(user);
            new GameDialog(this, app, user, gameId).setVisible(true); // waits until the game window closes
        } catch (AppException e) {
            Theme.showError(message, e.getMessage());
        }
        refresh();
    }
}
