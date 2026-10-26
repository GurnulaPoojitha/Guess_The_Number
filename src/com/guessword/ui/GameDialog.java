package com.guessword.ui;

import com.guessword.AppContext;
import com.guessword.model.User;
import com.guessword.service.AppException;
import com.guessword.service.GameService;
import com.guessword.service.GameView;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;

/**
 * The game itself. Closing the window part-way through leaves the game unfinished, and the
 * player can come back to it from their home window.
 */
class GameDialog extends JDialog {

    private final AppContext app;
    private final User user;
    private final long gameId;

    private final BoardPanel board = new BoardPanel(GameService.MAX_GUESSES, 5);
    private final JTextField input = new JTextField(6);
    private final JLabel progress = Theme.muted(" ");
    private final JLabel message = Theme.messageLabel();
    private GameView view;

    GameDialog(JFrame owner, AppContext app, User user, long gameId) {
        super(owner, "Guess the Word", true);
        this.app = app;
        this.user = user;
        this.gameId = gameId;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JLabel title = Theme.title("Guess the word");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        progress.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);
        top.add(progress, BorderLayout.SOUTH);

        JPanel boardWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        boardWrap.setOpaque(false);
        boardWrap.add(board);

        input.setFont(Theme.MONO);
        input.setHorizontalAlignment(SwingConstants.CENTER);
        input.setToolTipText("Exactly 5 upper-case letters (A-Z)");
        ((AbstractDocument) input.getDocument()).setDocumentFilter(new UppercaseLettersFilter(5));
        input.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { redraw(); }
            public void removeUpdate(DocumentEvent e) { redraw(); }
            public void changedUpdate(DocumentEvent e) { redraw(); }
        });
        JButton submit = Theme.primaryButton("Submit");
        submit.addActionListener(e -> submit());

        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        inputRow.setOpaque(false);
        inputRow.add(input);
        inputRow.add(submit);

        message.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);
        bottom.add(inputRow, BorderLayout.NORTH);
        bottom.add(legend(), BorderLayout.CENTER);
        bottom.add(message, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(Theme.BACKGROUND);
        content.setBorder(Theme.padding(20));
        content.add(top, BorderLayout.NORTH);
        content.add(boardWrap, BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);
        setContentPane(content);
        getRootPane().setDefaultButton(submit); // Enter submits

        view = app.games.getGameView(user, gameId);
        redraw();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private static JPanel legend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        legend.setOpaque(false);
        legend.add(chip(Theme.GREEN, "Right spot"));
        legend.add(chip(Theme.ORANGE, "Wrong spot"));
        legend.add(chip(Theme.GREY, "Not in word"));
        return legend;
    }

    private static JLabel chip(Color colour, String text) {
        JLabel label = Theme.muted(text);
        label.setIcon(new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(colour);
                g.fillRect(x, y, 14, 14);
            }
            public int getIconWidth() { return 14; }
            public int getIconHeight() { return 14; }
        });
        label.setIconTextGap(6);
        return label;
    }

    private void redraw() {
        board.render(view, input.getText());
        progress.setText(view.inProgress()
                ? "Guess " + (view.attemptsUsed() + 1) + " of " + view.maxAttempts()
                : view.status().label());
    }

    private void submit() {
        try {
            view = app.games.submitGuess(user, gameId, input.getText());
            message.setText(" ");
            input.setText("");
            redraw();
        } catch (AppException e) {
            Theme.showError(message, e.getMessage());
            pack();
            return;
        }

        if (view.won()) {
            String tries = view.attemptsUsed() == 1 ? "try" : "tries";
            JOptionPane.showMessageDialog(this,
                    "You guessed " + view.answer() + " in " + view.attemptsUsed() + " " + tries + ".",
                    "Congratulations!", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // OK stops the game
        } else if (view.lost()) {
            JOptionPane.showMessageDialog(this,
                    "Better luck next time!\nThe word was " + view.answer() + ".",
                    "Better luck next time", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }
}
