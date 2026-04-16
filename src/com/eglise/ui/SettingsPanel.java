package com.eglise.ui;

import com.eglise.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Set;

public class SettingsPanel extends JPanel {

    private final AuthService auth = AuthService.getInstance();

    public SettingsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(248, 249, 255));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("⚙️ Paramètres & Administration");
        title.setFont(new Font("Georgia", Font.BOLD, 22));
        title.setForeground(new Color(13, 71, 161));
        title.setBorder(new EmptyBorder(0, 0, 15, 0));

        JTabbedPane tabs = new JTabbedPane();

        // ─── Onglet Utilisateurs ───
        JPanel usersPanel = new JPanel(new BorderLayout(0, 10));
        usersPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        usersPanel.setBackground(Color.WHITE);

        JLabel lblUsers = new JLabel("Utilisateurs enregistrés:");
        lblUsers.setFont(new Font("Arial", Font.BOLD, 13));

        Set<String> users = auth.getUtilisateurs();
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String u : users) model.addElement("👤  " + u);
        JList<String> userList = new JList<>(model);
        userList.setFont(new Font("Monospaced", Font.PLAIN, 13));

        // Formulaire ajout utilisateur
        JPanel addUserForm = new JPanel(new GridBagLayout());
        addUserForm.setBackground(Color.WHITE);
        addUserForm.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 215, 235)),
                "Ajouter un utilisateur", 0, 0,
                new Font("Arial", Font.BOLD, 12), new Color(13, 71, 161)));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(5, 8, 5, 8);

        JTextField fUser = field(""); JPasswordField fPass = new JPasswordField();
        JComboBox<String> cbRole = new JComboBox<>(new String[]{"MEMBRE", "SECRETAIRE", "TRESORIER", "PASTEUR", "ADMIN"});
        styleField(fPass);

        g.gridx=0; g.gridy=0; g.weightx=0.3; addUserForm.add(new JLabel("Identifiant:"), g);
        g.gridx=1; g.weightx=0.7; addUserForm.add(fUser, g);
        g.gridx=0; g.gridy=1; g.weightx=0.3; addUserForm.add(new JLabel("Mot de passe:"), g);
        g.gridx=1; g.weightx=0.7; addUserForm.add(fPass, g);
        g.gridx=0; g.gridy=2; g.weightx=0.3; addUserForm.add(new JLabel("Rôle:"), g);
        g.gridx=1; g.weightx=0.7; addUserForm.add(cbRole, g);

        JButton btnAddUser = new JButton("➕ Ajouter l'utilisateur");
        btnAddUser.setBackground(new Color(0, 120, 60)); btnAddUser.setForeground(Color.WHITE);
        btnAddUser.setFocusPainted(false);
        g.gridx=0; g.gridy=3; g.gridwidth=2;
        addUserForm.add(btnAddUser, g);

        btnAddUser.addActionListener(e -> {
            String username = fUser.getText().trim();
            String password = new String(fPass.getPassword()).trim();
            String role = (String) cbRole.getSelectedItem();
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Identifiant et mot de passe obligatoires.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (auth.ajouterUtilisateur(username, password, role)) {
                model.addElement("👤  " + username);
                fUser.setText(""); fPass.setText("");
                JOptionPane.showMessageDialog(this, "Utilisateur \"" + username + "\" ajouté avec le rôle " + role);
            } else {
                JOptionPane.showMessageDialog(this, "Impossible d'ajouter l'utilisateur.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        usersPanel.add(lblUsers, BorderLayout.NORTH);
        usersPanel.add(new JScrollPane(userList), BorderLayout.CENTER);
        usersPanel.add(addUserForm, BorderLayout.SOUTH);

        // ─── Onglet Mot de passe ───
        JPanel mdpPanel = new JPanel(new GridBagLayout());
        mdpPanel.setBackground(Color.WHITE);
        mdpPanel.setBorder(new EmptyBorder(25, 40, 25, 40));
        GridBagConstraints gm = new GridBagConstraints();
        gm.fill = GridBagConstraints.HORIZONTAL; gm.insets = new Insets(8, 5, 8, 5);

        JTextField fMdpUser   = field(auth.getCurrentUser());
        JPasswordField fAncien = new JPasswordField(); styleField(fAncien);
        JPasswordField fNouveau = new JPasswordField(); styleField(fNouveau);
        JPasswordField fConfirm = new JPasswordField(); styleField(fConfirm);

        Object[][] mdpRows = {
            {"Utilisateur:", fMdpUser},
            {"Ancien mot de passe:", fAncien},
            {"Nouveau mot de passe:", fNouveau},
            {"Confirmer:", fConfirm}
        };
        for (int i = 0; i < mdpRows.length; i++) {
            gm.gridx=0; gm.gridy=i; gm.weightx=0.4; mdpPanel.add(new JLabel((String)mdpRows[i][0]), gm);
            gm.gridx=1; gm.weightx=0.6; mdpPanel.add((Component)mdpRows[i][1], gm);
        }

        JButton btnChangeMdp = new JButton("🔐 Changer le mot de passe");
        btnChangeMdp.setBackground(new Color(13, 71, 161)); btnChangeMdp.setForeground(Color.WHITE);
        btnChangeMdp.setFocusPainted(false);
        gm.gridx=0; gm.gridy=4; gm.gridwidth=2; mdpPanel.add(btnChangeMdp, gm);

        btnChangeMdp.addActionListener(e -> {
            String user = fMdpUser.getText().trim();
            String ancien = new String(fAncien.getPassword());
            String nouveau = new String(fNouveau.getPassword());
            String confirm = new String(fConfirm.getPassword());
            if (nouveau.isEmpty()) { JOptionPane.showMessageDialog(this, "Nouveau mot de passe vide.", "Validation", JOptionPane.WARNING_MESSAGE); return; }
            if (!nouveau.equals(confirm)) { JOptionPane.showMessageDialog(this, "Les mots de passe ne correspondent pas.", "Validation", JOptionPane.WARNING_MESSAGE); return; }
            if (nouveau.length() < 6) { JOptionPane.showMessageDialog(this, "Le mot de passe doit faire au moins 6 caractères.", "Validation", JOptionPane.WARNING_MESSAGE); return; }
            if (auth.changerMotDePasse(user, ancien, nouveau)) {
                JOptionPane.showMessageDialog(this, "✅ Mot de passe changé avec succès !");
                fAncien.setText(""); fNouveau.setText(""); fConfirm.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Ancien mot de passe incorrect ou utilisateur introuvable.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        // ─── Onglet À propos ───
        JPanel aboutPanel = new JPanel(new GridBagLayout());
        aboutPanel.setBackground(Color.WHITE);
        JTextArea about = new JTextArea(
                "╔══════════════════════════════════════╗\n" +
                "║   Système de Gestion d'Église        ║\n" +
                "║   Église Apostolique Source de Grâce ║\n" +
                "╚══════════════════════════════════════╝\n\n" +
                "Version      : 1.0.0\n" +
                "Développeur  : Junior NATOLO\n" +
                "Technologie  : Java 21 + Swing\n" +
                "Stockage     : Sérialisation Java (fichiers .dat)\n" +
                "Sauvegarde   : ZIP (Drive, USB, Local)\n\n" +
                "Fonctionnalités:\n" +
                "  ✅ Gestion des membres\n" +
                "  ✅ Gestion financière (offrandes, dîmes, dépenses)\n" +
                "  ✅ Gestion des événements\n" +
                "  ✅ Sauvegarde ZIP sur Drive/USB\n" +
                "  ✅ Export CSV pour Excel\n" +
                "  ✅ Rapports TXT\n" +
                "  ✅ Authentification multi-rôles\n\n" +
                "Comptes par défaut:\n" +
                "  admin / admin123\n" +
                "  pasteur / pasteur123\n" +
                "  secretaire / secret123\n" +
                "  tresorier / tresorier123\n"
        );
        about.setFont(new Font("Monospaced", Font.PLAIN, 12));
        about.setEditable(false);
        about.setBackground(Color.WHITE);
        aboutPanel.add(new JScrollPane(about));

        tabs.addTab("👥 Utilisateurs", usersPanel);
        tabs.addTab("🔐 Mot de passe", mdpPanel);
        tabs.addTab("ℹ️ À propos", aboutPanel);

        add(title, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    private JTextField field(String val) {
        JTextField f = new JTextField(val);
        styleField(f); return f;
    }

    private void styleField(JTextField f) {
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 7, 4, 7)));
        f.setFont(new Font("Arial", Font.PLAIN, 13));
    }
}
