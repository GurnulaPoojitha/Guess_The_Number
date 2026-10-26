package com.guessword.ui;

import com.guessword.AppContext;
import com.guessword.model.User;
import com.guessword.model.Word;
import com.guessword.service.AppException;
import com.guessword.service.DayReport;
import com.guessword.service.UserReport;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Admin window with three tabs: Day report, User report and the word list. */
public class AdminFrame extends JFrame {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final AppContext app;

    // Day report
    private final JTextField dateField = new JTextField(10);
    private final JLabel usersPlayed = statValue();
    private final JLabel correctGuesses = statValue();
    private final JLabel wordsTried = statValue();
    private final JLabel dayMessage = Theme.messageLabel();
    private final DefaultTableModel dayTable = Tables.model("Player", "Words tried", "Correct guesses");

    // User report
    private final JComboBox<User> playerBox = new JComboBox<>();
    private final JLabel userTotals = Theme.muted(" ");
    private final JLabel userMessage = Theme.messageLabel();
    private final DefaultTableModel userTable = Tables.model("Date", "Words tried", "Correct guesses");

    // Words
    private final DefaultListModel<Word> wordList = new DefaultListModel<>();
    private final JList<Word> wordJList = new JList<>(wordList);
    private final JTextField newWord = new JTextField(8);
    private final JLabel wordCount = Theme.muted(" ");
    private final JLabel wordMessage = Theme.messageLabel();

    public AdminFrame(AppContext app, User admin) {
        super("Guess the Word - Admin");
        this.app = app;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JButton logout = new JButton("Log out");
        logout.addActionListener(e -> Navigator.logout(this, app));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        header.add(Theme.title("Admin - " + admin.username()), BorderLayout.WEST);
        header.add(logout, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.BOLD);
        tabs.addTab("Day report", dayReportTab());
        tabs.addTab("User report", userReportTab());
        tabs.addTab("Words", wordsTab());
        tabs.addChangeListener(e -> {
            // New players may have registered since the window opened.
            if (tabs.getSelectedIndex() == 1) {
                reloadPlayers();
            }
        });

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Theme.BACKGROUND);
        content.setBorder(Theme.padding(20));
        content.add(header, BorderLayout.NORTH);
        content.add(tabs, BorderLayout.CENTER);
        setContentPane(content);

        dateField.setText(LocalDate.now(app.clock).toString());
        runDayReport();
        reloadPlayers();
        reloadWords();

        pack();
        setMinimumSize(new Dimension(560, 520));
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------------------------ Day report

    private JComponent dayReportTab() {
        JButton run = Theme.primaryButton("Run report");
        run.addActionListener(e -> runDayReport());
        JButton today = new JButton("Today");
        today.addActionListener(e -> {
            dateField.setText(LocalDate.now(app.clock).toString());
            runDayReport();
        });
        dateField.addActionListener(e -> runDayReport()); // Enter in the date box

        JPanel toolbar = row(new JLabel("Date (yyyy-mm-dd):"), dateField, run, today);

        JPanel stats = new JPanel(new GridLayout(1, 3, 10, 0));
        stats.setOpaque(false);
        stats.add(stat(usersPlayed, "Users played"));
        stats.add(stat(correctGuesses, "Correct guesses"));
        stats.add(stat(wordsTried, "Words tried"));

        return tab(new Column()
                .add(toolbar)
                .gap(4).add(dayMessage)
                .gap(8).add(stats)
                .label("Players on this day")
                .add(Tables.scroll(dayTable, 180)));
    }

    private void runDayReport() {
        LocalDate date;
        try {
            date = LocalDate.parse(dateField.getText().trim());
        } catch (DateTimeParseException e) {
            Theme.showError(dayMessage, "Please enter the date as yyyy-mm-dd, for example 2026-10-14.");
            return;
        }
        DayReport report = app.reports.dayReport(date);
        usersPlayed.setText(String.valueOf(report.usersPlayed()));
        correctGuesses.setText(String.valueOf(report.correctGuesses()));
        wordsTried.setText(String.valueOf(report.wordsTried()));
        dayTable.setRowCount(0);
        report.perUser().forEach(r -> dayTable.addRow(new Object[]{r.username(), r.wordsTried(), r.correctGuesses()}));
        dayMessage.setForeground(Theme.MUTED);
        dayMessage.setText(report.perUser().isEmpty()
                ? "No games were played on " + DATE.format(date) + "."
                : "Report for " + DATE.format(date) + ".");
    }

    // ------------------------------------------------------------------ User report

    private JComponent userReportTab() {
        JButton run = Theme.primaryButton("Run report");
        run.addActionListener(e -> runUserReport());
        playerBox.setPreferredSize(new Dimension(180, playerBox.getPreferredSize().height));

        return tab(new Column()
                .add(row(new JLabel("Player:"), playerBox, run))
                .gap(4).add(userMessage)
                .gap(8).add(Tables.scroll(userTable, 240))
                .gap(6).add(userTotals));
    }

    private void reloadPlayers() {
        User selected = (User) playerBox.getSelectedItem();
        playerBox.removeAllItems();
        for (User p : app.users.players()) {
            playerBox.addItem(p);
            if (selected != null && selected.id() == p.id()) {
                playerBox.setSelectedItem(p);
            }
        }
        if (playerBox.getItemCount() == 0) {
            userMessage.setForeground(Theme.MUTED);
            userMessage.setText("No players have registered yet.");
        }
    }

    private void runUserReport() {
        User player = (User) playerBox.getSelectedItem();
        if (player == null) {
            Theme.showError(userMessage, "Choose a player first.");
            return;
        }
        UserReport report = app.reports.userReport(player.id());
        userTable.setRowCount(0);
        report.rows().forEach(r -> userTable.addRow(new Object[]{
                DATE.format(r.date()), r.wordsTried(), r.correctGuesses()}));
        userMessage.setForeground(Theme.MUTED);
        userMessage.setText(report.rows().isEmpty()
                ? report.username() + " hasn't played any games yet."
                : "Report for " + report.username() + ".");
        userTotals.setText("Total: " + report.totalWordsTried() + " words tried, "
                + report.totalCorrectGuesses() + " correct guesses.");
    }

    // ------------------------------------------------------------------ Words

    private JComponent wordsTab() {
        JButton add = Theme.primaryButton("Add word");
        add.addActionListener(e -> addWord());
        newWord.addActionListener(e -> addWord());
        newWord.setFont(Theme.MONO);
        JButton remove = new JButton("Remove selected");
        remove.addActionListener(e -> removeWord());

        wordJList.setFont(Theme.MONO);
        wordJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        wordJList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        wordJList.setVisibleRowCount(-1);
        wordJList.setFixedCellWidth(90);
        JScrollPane scroll = new JScrollPane(wordJList);
        scroll.setPreferredSize(new Dimension(460, 230));

        return tab(new Column()
                .add(row(new JLabel("New word:"), newWord, add, remove))
                .gap(4).add(wordMessage)
                .gap(8).add(wordCount)
                .gap(4).add(scroll));
    }

    private void reloadWords() {
        wordList.clear();
        app.words.listWords().forEach(wordList::addElement);
        wordCount.setText(wordList.size() + " words");
    }

    private void addWord() {
        try {
            Word word = app.words.addWord(newWord.getText());
            newWord.setText("");
            reloadWords();
            Theme.showSuccess(wordMessage, "Added '" + word.text() + "'.");
        } catch (AppException e) {
            Theme.showError(wordMessage, e.getMessage());
        }
    }

    private void removeWord() {
        Word selected = wordJList.getSelectedValue();
        if (selected == null) {
            Theme.showError(wordMessage, "Select a word in the list first.");
            return;
        }
        try {
            app.words.deleteWord(selected.id());
            reloadWords();
            Theme.showSuccess(wordMessage, "Removed '" + selected.text() + "'.");
        } catch (AppException e) {
            Theme.showError(wordMessage, e.getMessage());
        }
    }

    // ------------------------------------------------------------------ layout helpers

    private static JComponent tab(Column column) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBorder(Theme.padding(14));
        wrap.add(column, BorderLayout.NORTH);
        return wrap;
    }

    private static JPanel row(JComponent... components) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        for (JComponent c : components) {
            row.add(c);
        }
        return row;
    }

    private static JLabel statValue() {
        JLabel label = new JLabel("0");
        label.setFont(Theme.STAT);
        label.setForeground(Theme.TEXT);
        return label;
    }

    private static JPanel stat(JLabel value, String caption) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(java.awt.Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER), Theme.padding(10)));
        box.add(value, BorderLayout.CENTER);
        box.add(Theme.muted(caption), BorderLayout.SOUTH);
        return box;
    }
}
