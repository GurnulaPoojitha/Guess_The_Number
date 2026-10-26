package com.guessword.ui;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;

/** A vertical, left-aligned stack of components. Used to lay out the simple forms. */
class Column extends JPanel {

    Column() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
    }

    Column add(JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Text fields and tables stretch across the window but never grow taller. Labels and
        // buttons are left at their natural size, which follows their text when it changes.
        if (!(component instanceof JLabel) && !(component instanceof AbstractButton)) {
            component.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
        }
        super.add(component);
        return this;
    }

    Column gap(int pixels) {
        Component strut = Box.createVerticalStrut(pixels);
        ((JComponent) strut).setAlignmentX(Component.LEFT_ALIGNMENT);
        super.add(strut);
        return this;
    }

    Column label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.BOLD);
        return gap(8).add(label).gap(3);
    }
}
