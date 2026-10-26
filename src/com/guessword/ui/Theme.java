package com.guessword.ui;

import com.guessword.model.LetterResult;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Font;

/** Colours and fonts shared by all windows. */
public final class Theme {

    public static final Color GREEN = new Color(106, 170, 100);
    public static final Color ORANGE = new Color(240, 149, 46);
    public static final Color GREY = new Color(120, 124, 126);
    public static final Color EMPTY = Color.WHITE;
    public static final Color BORDER = new Color(211, 214, 218);
    public static final Color BORDER_ACTIVE = new Color(135, 138, 140);
    public static final Color TEXT = new Color(26, 26, 27);
    public static final Color MUTED = new Color(107, 111, 114);
    public static final Color ERROR = new Color(170, 40, 30);
    public static final Color SUCCESS = new Color(30, 110, 40);
    public static final Color PRIMARY = new Color(47, 111, 222);
    public static final Color BACKGROUND = new Color(246, 247, 248);

    public static final Font TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 22);
    public static final Font BODY = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    public static final Font BOLD = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    public static final Font TILE = new Font(Font.SANS_SERIF, Font.BOLD, 30);
    public static final Font STAT = new Font(Font.SANS_SERIF, Font.BOLD, 30);
    public static final Font MONO = new Font(Font.MONOSPACED, Font.BOLD, 18);

    private Theme() {
    }

    /** Gives every standard Swing component the same readable font. Call once at start-up. */
    public static void installDefaults() {
        for (String key : new String[]{"Button.font", "Label.font", "TextField.font", "PasswordField.font",
                "ComboBox.font", "List.font", "Table.font", "TableHeader.font", "TabbedPane.font",
                "OptionPane.messageFont", "OptionPane.buttonFont", "ToolTip.font"}) {
            UIManager.put(key, BODY);
        }
    }

    public static Color colourFor(LetterResult result) {
        return switch (result) {
            case CORRECT -> GREEN;
            case PRESENT -> ORANGE;
            case ABSENT -> GREY;
        };
    }

    public static Border padding(int size) {
        return BorderFactory.createEmptyBorder(size, size, size, size);
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BODY);
        label.setForeground(MUTED);
        return label;
    }

    /** Small grey help text that wraps onto several lines. */
    public static JLabel hint(String text) {
        JLabel label = muted(toHtml(text));
        label.setFont(BODY.deriveFont(12f));
        return label;
    }

    public static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BOLD);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return button;
    }

    /** A label used for error or success messages under a form. */
    public static JLabel messageLabel() {
        JLabel label = new JLabel(" ");
        label.setFont(BODY);
        return label;
    }

    public static void showError(JLabel label, String text) {
        label.setForeground(ERROR);
        label.setText(toHtml(text));
    }

    public static void showSuccess(JLabel label, String text) {
        label.setForeground(SUCCESS);
        label.setText(toHtml(text));
    }

    /** Wraps text in HTML so long messages wrap inside a fixed-width label. */
    static String toHtml(String text) {
        String escaped = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return "<html><div style='width:320px'>" + escaped + "</div></html>";
    }
}
