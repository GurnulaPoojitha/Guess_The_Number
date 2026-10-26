package com.guessword.ui;

import com.guessword.Test;
import com.guessword.model.GameStatus;
import com.guessword.service.GameView;
import com.guessword.service.WordEvaluator;

import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import java.awt.Color;
import java.util.List;

import static com.guessword.Assert.assertEquals;

/** Checks what the board and the guess box actually show, without opening a window. */
public class BoardPanelTest {

    private static GameView.Row row(String target, String guess) {
        return new GameView.Row(guess, WordEvaluator.evaluate(target, guess));
    }

    @Test
    void earlierGuessesAreShownInOrderWithTheirColours() {
        GameView view = new GameView(1, GameStatus.IN_PROGRESS,
                List.of(row("TOWER", "AUDIO"), row("TOWER", "HOMER")), 5, null);
        BoardPanel board = new BoardPanel(5, 5);

        board.render(view, "");

        assertEquals("A", board.tile(0, 0).getText());
        assertEquals(Theme.GREY, board.tile(0, 0).getBackground());
        assertEquals("O", board.tile(0, 4).getText());
        assertEquals(Theme.ORANGE, board.tile(0, 4).getBackground());
        assertEquals("H", board.tile(1, 0).getText());
        assertEquals(Theme.GREEN, board.tile(1, 1).getBackground()); // O in the right place
        assertEquals(Color.WHITE, board.tile(1, 1).getForeground());
    }

    @Test
    void currentRowShowsWhatThePlayerIsTyping() {
        GameView view = new GameView(1, GameStatus.IN_PROGRESS, List.of(row("TOWER", "AUDIO")), 5, null);
        BoardPanel board = new BoardPanel(5, 5);

        board.render(view, "TON");

        assertEquals("T", board.tile(1, 0).getText());
        assertEquals("N", board.tile(1, 2).getText());
        assertEquals("", board.tile(1, 3).getText());
        assertEquals(Theme.EMPTY, board.tile(1, 0).getBackground());
        assertEquals("", board.tile(2, 0).getText());
    }

    @Test
    void typingIsConvertedToUpperCaseLettersOnly() throws Exception {
        JTextField field = new JTextField();
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new UppercaseLettersFilter(5));

        field.getDocument().insertString(0, "to1w-e r", null);
        assertEquals("TOWER", field.getText());

        field.getDocument().insertString(5, "S", null); // already 5 letters
        assertEquals("TOWER", field.getText());
    }
}
