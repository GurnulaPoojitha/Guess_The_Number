package com.guessword.ui;

import com.guessword.AppContext;
import com.guessword.service.AppException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.util.Optional;

/** Lets a new player sign up. Admin accounts can't be created here. */
class RegisterDialog extends JDialog {

    private final AppContext app;
    private final JTextField username = new JTextField(20);
    private final JPasswordField password = new JPasswordField(20);
    private final JPasswordField confirm = new JPasswordField(20);
    private final JLabel message = Theme.messageLabel();
    private String registered;

    RegisterDialog(JFrame owner, AppContext app) {
        super(owner, "Create an account", true);
        this.app = app;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JButton submit = Theme.primaryButton("Register");
        submit.addActionListener(e -> register());

        Column form = new Column()
                .add(Theme.title("Create an account"))
                .label("Username").add(username)
                .add(Theme.hint("At least 5 letters (A-Z only), with upper- and lower-case. Example: PlayerOne"))
                .label("Password").add(password)
                .add(Theme.hint("At least 5 characters with a letter, a number and one of $ % * @. Example: game$1"))
                .label("Confirm password").add(confirm)
                .gap(14).add(submit)
                .gap(10).add(message);
        form.setBorder(Theme.padding(24));

        getContentPane().setBackground(Theme.BACKGROUND);
        add(form);
        getRootPane().setDefaultButton(submit);
        pack();
        setLocationRelativeTo(owner);
    }

    private void register() {
        try {
            registered = app.users.registerPlayer(username.getText().trim(),
                    new String(password.getPassword()), new String(confirm.getPassword())).username();
            dispose();
        } catch (AppException e) {
            Theme.showError(message, e.getMessage());
            pack();
        }
    }

    Optional<String> registeredUsername() {
        return Optional.ofNullable(registered);
    }
}
