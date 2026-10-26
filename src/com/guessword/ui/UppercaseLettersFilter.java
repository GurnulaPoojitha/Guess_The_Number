package com.guessword.ui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.util.Locale;

/** Text-field filter that turns typing into upper case, drops anything but A-Z, and caps the length. */
public class UppercaseLettersFilter extends DocumentFilter {

    private final int maxLength;

    public UppercaseLettersFilter(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String text, AttributeSet attrs)
            throws BadLocationException {
        replace(fb, offset, 0, text, attrs);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
        String clean = text == null ? "" : text.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        int room = maxLength - (fb.getDocument().getLength() - length);
        if (clean.length() > room) {
            clean = clean.substring(0, Math.max(0, room));
        }
        super.replace(fb, offset, length, clean, attrs);
    }
}
