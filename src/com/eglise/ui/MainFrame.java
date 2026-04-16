package com.eglise.ui;
import java.awt.image.BufferedImage;
import com.eglise.service.AuthService;
import com.eglise.service.BackupService;
import com.eglise.service.ReportService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.LocalDate;
import java.util.Map;

public class MainFrame extends JFrame {

    private JTabbedPane tabbedPane;
    private JLabel lblStatus;
    private JLabel lblUser;

    public MainFrame() {
        // Afficher login d'abord
        LoginDialog login = new LoginDialog(null);
        login.setVisible(true);

        if (!login.isAuthenticated()) {
            System.exit(0);
        }

        buildUI();
        setTitle("Église Apostolique « Source de Grâce » - Gestion");
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setIconImage(createAppIcon());
    }

    private Image createAppIcon() {
        // Icône simple générée programmatiquement
        BufferedImage img = new java.awt.image.BufferedImage(32, 32, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(13, 71, 161));
        g.fillRect(0, 0, 32, 32);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Serif", Font.BOLD, 20));
        g.drawString("✞", 6, 24);
        g.dispose();
        return img;
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        // === HEADER ===
        JPanel header = buildHeader();
        add(header, BorderLayout.NORTH);

        // === MENU BAR ===
        setJMenuBar(buildMenuBar());

        // === ONGLETS PRINCIPAUX ===
        tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 13));
        tabbedPane.setBackground(new Color(245, 245, 250));

        tabbedPane.addTab("🏠 Tableau de Bord", new DashboardPanel());
        tabbedPane.addTab("👥 Membres", new MembrePanel());
        tabbedPane.addTab("💰 Finances", new FinancePanel());
        tabbedPane.addTab("📅 Événements", new EvenementPanel());
        tabbedPane.addTab("✝️ Sacrements", new SacrementPanel());
        tabbedPane.addTab("📋 Présences", new PresencePanel());
        tabbedPane.addTab("📢 Communication", new CommunicationPanel());
        tabbedPane.addTab("💾 Sauvegarde", new BackupPanel());

        // Onglets admin seulement
        if (AuthService.getInstance().isAdmin()) {
            tabbedPane.addTab("⚙️ Paramètres", new SettingsPanel());
        }

        add(tabbedPane, BorderLayout.CENTER);

        // === STATUS BAR ===
        JPanel statusBar = buildStatusBar();
        add(statusBar, BorderLayout.SOUTH);

        // Auto-save au fermeture
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(MainFrame.this,
                        "Voulez-vous créer une sauvegarde avant de quitter ?",
                        "Sauvegarde automatique", JOptionPane.YES_NO_CANCEL_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        BackupService.getInstance().sauvegardeAutomatique();
                        JOptionPane.showMessageDialog(MainFrame.this, "Sauvegarde créée avec succès.");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainFrame.this, "Erreur sauvegarde: " + ex.getMessage());
                    }
                }
                if (confirm != JOptionPane.CANCEL_OPTION) {
                    System.exit(0);
                }
            }
        });
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(13, 71, 161));
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel lblTitle = new JLabel("✞  Église Apostolique « Source de Grâce »");
        lblTitle.setFont(new Font("Georgia", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        lblUser = new JLabel("👤 " + AuthService.getInstance().getCurrentUser().toUpperCase()
                + " [" + AuthService.getInstance().getCurrentRole() + "]");
        lblUser.setFont(new Font("Arial", Font.PLAIN, 12));
        lblUser.setForeground(new Color(200, 220, 255));

        header.add(lblTitle, BorderLayout.WEST);
        header.add(lblUser, BorderLayout.EAST);
        return header;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Menu Fichier
        JMenu menuFichier = new JMenu("Fichier");
        JMenuItem itemSauv = new JMenuItem("💾 Sauvegarder maintenant");
        itemSauv.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        itemSauv.addActionListener(e -> actionSauvegarder());

        JMenuItem itemExport = new JMenuItem("📤 Exporter les données");
        itemExport.addActionListener(e -> actionExporter());

        JMenuItem itemQuitter = new JMenuItem("🚪 Quitter");
        itemQuitter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        itemQuitter.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        menuFichier.add(itemSauv);
        menuFichier.add(itemExport);
        menuFichier.addSeparator();
        menuFichier.add(itemQuitter);

        // Menu Rapports
        JMenu menuRapports = new JMenu("Rapports");
        JMenuItem itemRapportFin = new JMenuItem("📊 Rapport financier mensuel");
        itemRapportFin.addActionListener(e -> actionRapportFinancier());
        JMenuItem itemRapportMbr = new JMenuItem("📋 Rapport des membres");
        itemRapportMbr.addActionListener(e -> actionRapportMembres());

        menuRapports.add(itemRapportFin);
        menuRapports.add(itemRapportMbr);

        // Menu Aide
        JMenu menuAide = new JMenu("Aide");
        JMenuItem itemAPropos = new JMenuItem("À propos");
        itemAPropos.addActionListener(e -> showAPropos());
        menuAide.add(itemAPropos);

        menuBar.add(menuFichier);
        menuBar.add(menuRapports);
        menuBar.add(menuAide);
        return menuBar;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                new EmptyBorder(3, 10, 3, 10)));
        bar.setBackground(new Color(245, 245, 245));

        lblStatus = new JLabel("Prêt");
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 11));

        JLabel lblDB = new JLabel("Base de données: " + BackupService.getInstance().getTailleDB() + " KB");
        lblDB.setFont(new Font("Arial", Font.PLAIN, 11));
        lblDB.setForeground(Color.GRAY);

        bar.add(lblStatus, BorderLayout.WEST);
        bar.add(lblDB, BorderLayout.EAST);
        return bar;
    }

    private void actionSauvegarder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choisir le dossier de sauvegarde");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        // Proposer Google Drive si disponible
        File driveDir = new File(System.getProperty("user.home") + File.separator + "Google Drive");
        if (!driveDir.exists()) driveDir = new File(System.getProperty("user.home") + File.separator + "OneDrive");
        if (driveDir.exists()) chooser.setCurrentDirectory(driveDir);

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String path = BackupService.getInstance().creerSauvegarde(chooser.getSelectedFile().getAbsolutePath());
                setStatus("✅ Sauvegarde créée: " + path);
                JOptionPane.showMessageDialog(this, "Sauvegarde créée avec succès !\n" + path, "Succès", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void actionExporter() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Dossier d'export CSV");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            // L'export détaillé est fait dans BackupPanel
            JOptionPane.showMessageDialog(this, "Pour l'export CSV détaillé, utilisez l'onglet Sauvegarde.", "Info", JOptionPane.INFORMATION_MESSAGE);
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 2); // onglet backup
        }
    }

    private void actionRapportFinancier() {
        String moisStr = JOptionPane.showInputDialog(this, "Mois (1-12):", LocalDate.now().getMonthValue());
        if (moisStr == null) return;
        try {
            int mois = Integer.parseInt(moisStr.trim());
            String rapport = ReportService.getInstance().rapportFinancierMensuel(mois, LocalDate.now().getYear());
            showRapport("Rapport Financier", rapport);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Mois invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actionRapportMembres() {
        String rapport = ReportService.getInstance().rapportMembres();
        showRapport("Rapport des Membres", rapport);
    }

    private void showRapport(String titre, String contenu) {
        JDialog dlg = new JDialog(this, titre, true);
        dlg.setSize(700, 550);
        dlg.setLocationRelativeTo(this);

        JTextArea ta = new JTextArea(contenu);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setEditable(false);
        ta.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(ta);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSauv = new JButton("💾 Sauvegarder");
        btnSauv.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Enregistrer le rapport");
            if (fc.showSaveDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                try {
                    ReportService.getInstance().sauvegarderRapport(contenu,
                            fc.getSelectedFile().getParent(),
                            fc.getSelectedFile().getName());
                    JOptionPane.showMessageDialog(dlg, "Rapport sauvegardé !");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, "Erreur: " + ex.getMessage());
                }
            }
        });
        JButton btnClose = new JButton("Fermer");
        btnClose.addActionListener(e -> dlg.dispose());
        btnPanel.add(btnSauv);
        btnPanel.add(btnClose);

        dlg.add(scroll, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showAPropos() {
        JOptionPane.showMessageDialog(this,
                "Système de Gestion d'Église\n" +
                "Église Apostolique « Source de Grâce »\n\n" +
                "Version 1.0 - 2025\n" +
                "Développé par Junior NATOLO\n\n" +
                "Fonctionnalités: Membres, Finances, Événements\n" +
                "Sauvegarde sur Drive/USB, Rapports CSV/TXT",
                "À propos", JOptionPane.INFORMATION_MESSAGE);
    }

    public void setStatus(String msg) {
        lblStatus.setText(msg);
        // Reset après 5s
        new Timer(5000, e -> lblStatus.setText("Prêt")).start();
    }
}
