package com.guessword.ui;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.Dimension;

/** Read-only tables for the history and report screens. */
final class Tables {

    private Tables() {
    }

    static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    static JScrollPane scroll(DefaultTableModel model, int height) {
        JTable table = new JTable(model);
        table.setFont(Theme.BODY);
        table.setRowHeight(24);
        table.getTableHeader().setFont(Theme.BOLD);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(460, height));
        return scroll;
    }
}
