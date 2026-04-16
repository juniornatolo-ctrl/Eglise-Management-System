package com.eglise.ui;
import com.eglise.model.*;
import com.eglise.service.*;
import com.eglise.db.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginDialog extends JDialog {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private boolean authenticated = false;

    public LoginDialog(Frame parent) {
        super(parent, "Connexion - Église Apostolique « Source de Grâce »", true);
        buildUI();
        setSize(450, 300);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(13, 71, 161));

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        header.setBackground(new Color(13, 71, 161));
        JLabel lblTitle = new JLabel("✞  Connexion");
        lblTitle.setFont(new Font("Georgia", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle);

        // Form panel
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(25, 40, 25, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        // Username
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        form.add(new JLabel("Utilisateur:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtUsername = new JTextField(15);
        txtUsername.setText("admin");
        styleField(txtUsername);
        form.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        form.add(new JLabel("Mot de passe:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtPassword = new JPasswordField(15);
        styleField(txtPassword);
        form.add(txtPassword, gbc);

        // Hint
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JLabel hint = new JLabel("Par défaut: admin / admin123");
        hint.setFont(new Font("Arial", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);
        form.add(hint, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(Color.WHITE);

        JButton btnLogin = new JButton("  Se connecter  ");
        btnLogin.setBackground(new Color(13, 71, 161));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 13));
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> doLogin());

        JButton btnQuit = new JButton("  Quitter  ");
        btnQuit.setFont(new Font("Arial", Font.PLAIN, 13));
        btnQuit.addActionListener(e -> System.exit(0));

        btnPanel.add(btnLogin);
        btnPanel.add(btnQuit);

        gbc.gridy = 3;
        form.add(btnPanel, gbc);

        // Enter key triggers login
        getRootPane().setDefaultButton(btnLogin);
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        });

        main.add(header, BorderLayout.NORTH);
        main.add(form, BorderLayout.CENTER);
        setContentPane(main);
    }

    private void styleField(JTextField f) {
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        f.setFont(new Font("Arial", Font.PLAIN, 13));
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.", "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (AuthService.getInstance().login(username, password)) {
            authenticated = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Identifiants incorrects.", "Erreur de connexion", JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
            txtPassword.requestFocus();
        }
    }

    public boolean isAuthenticated() { return authenticated; }
}
