package com.guessword.ui;

import com.guessword.AppContext;
import com.guessword.model.User;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Optional;

public class LoginFrame extends JFrame {

    private final AppContext app;
    private final JTextField username = new JTextField(20);
    private final JPasswordField password = new JPasswordField(20);
    private final JLabel message = Theme.messageLabel();

    public LoginFrame(AppContext app) {
        super("Guess the Word - Log in");
        this.app = app;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JButton login = Theme.primaryButton("Log in");
        JButton register = new JButton("Create an account");
        login.addActionListener(e -> login());
        register.addActionListener(e -> register());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttons.setOpaque(false);
        buttons.add(login);
        buttons.add(javax.swing.Box.createHorizontalStrut(10));
        buttons.add(register);

        Column form = new Column()
                .add(Theme.title("GUESS THE WORD"))
                .add(Theme.muted("Log in to play, or create a new player account."))
                .gap(10)
                .label("Username").add(username)
                .label("Password").add(password)
                .gap(14).add(buttons)
                .gap(10).add(message);
        form.setBorder(Theme.padding(24));

        getContentPane().setBackground(Theme.BACKGROUND);
        add(form, BorderLayout.CENTER);
        getRootPane().setDefaultButton(login);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    private void login() {
        Optional<User> user = app.users.login(username.getText(), new String(password.getPassword()));
        if (user.isEmpty()) {
            Theme.showError(message, "Invalid username or password.");
            password.setText("");
            pack();
            return;
        }
        Navigator.openHome(this, app, user.get());
    }

    private void register() {
        RegisterDialog dialog = new RegisterDialog(this, app);
        dialog.setVisible(true); // modal: waits here until the dialog closes
        dialog.registeredUsername().ifPresent(name -> {
            username.setText(name);
            password.setText("");
            password.requestFocusInWindow();
            Theme.showSuccess(message, "Registration successful. Please log in.");
            pack();
        });
    }
}
