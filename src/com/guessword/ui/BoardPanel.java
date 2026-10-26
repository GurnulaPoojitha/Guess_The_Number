package com.guessword.ui;

import com.guessword.model.LetterResult;
import com.guessword.service.GameView;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

/** The 5 x 5 grid of letter tiles: earlier guesses in order, then the row being typed, then blanks. */
public class BoardPanel extends JPanel {

    private final int rows;
    private final int cols;
    private final JLabel[][] tiles;

    public BoardPanel(int rows, int cols) {
        super(new GridLayout(rows, cols, 6, 6));
        this.rows = rows;
        this.cols = cols;
        this.tiles = new JLabel[rows][cols];
        setOpaque(false);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                JLabel tile = new JLabel("", SwingConstants.CENTER);
                tile.setOpaque(true);
                tile.setFont(Theme.TILE);
                tile.setPreferredSize(new Dimension(62, 62));
                tiles[r][c] = tile;
                add(tile);
            }
        }
    }

    /** Draws the guesses from {@code view} and, while the game is on, what the player is typing. */
    public void render(GameView view, String typing) {
        int done = view.rows().size();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (r < done) {
                    GameView.Row row = view.rows().get(r);
                    paintScored(tiles[r][c], row.word().charAt(c), row.results().get(c));
                } else if (r == done && view.inProgress()) {
                    String letter = c < typing.length() ? String.valueOf(typing.charAt(c)) : "";
                    paintEmpty(tiles[r][c], letter, Theme.BORDER_ACTIVE);
                } else {
                    paintEmpty(tiles[r][c], "", Theme.BORDER);
                }
            }
        }
    }

    private static void paintScored(JLabel tile, char letter, LetterResult result) {
        tile.setText(String.valueOf(letter));
        tile.setBackground(Theme.colourFor(result));
        tile.setForeground(Color.WHITE);
        tile.setBorder(BorderFactory.createLineBorder(Theme.colourFor(result), 2));
        tile.getAccessibleContext().setAccessibleName(letter + " " + result.name().toLowerCase());
    }

    private static void paintEmpty(JLabel tile, String letter, Color border) {
        tile.setText(letter);
        tile.setBackground(Theme.EMPTY);
        tile.setForeground(Theme.TEXT);
        tile.setBorder(BorderFactory.createLineBorder(letter.isEmpty() ? border : Theme.BORDER_ACTIVE, 2));
    }

    /** Lets tests check what a tile shows. */
    public JLabel tile(int row, int col) {
        return tiles[row][col];
    }
}
