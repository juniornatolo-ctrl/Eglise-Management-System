package com.eglise.ui;

import com.eglise.service.BackupService;
import com.eglise.service.AuthService;
import com.eglise.db.DatabaseManager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class BackupPanel extends JPanel {

    private final BackupService backupService = BackupService.getInstance();
    private JTextArea logArea;
    private JLabel lblTailleDB;
    private DefaultListModel<String> listModel;
    private JList<String> backupList;

    public BackupPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(248, 249, 255));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        buildUI();
        refreshInfo();
        scanBackupsLocaux();
    }

    private void buildUI() {
        // ─── TITRE ───
        JLabel title = new JLabel("💾 Sauvegarde & Restauration");
        title.setFont(new Font("Georgia", Font.BOLD, 22));
        title.setForeground(new Color(13, 71, 161));
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        // ─── PANNEAU GAUCHE : Actions ───
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(320, 0));

        // Section info DB
        JPanel infoPanel = createSection("📊 Informations Base de Données");
        infoPanel.setLayout(new GridLayout(0, 1, 0, 6));
        lblTailleDB = new JLabel("Taille: ...");
        lblTailleDB.setFont(new Font("Arial", Font.PLAIN, 13));
        JLabel lblRepertoire = new JLabel("<html>Répertoire:<br><small>" + DatabaseManager.getInstance().getDbDir() + "</small></html>");
        lblRepertoire.setFont(new Font("Arial", Font.PLAIN, 11));
        infoPanel.add(lblTailleDB);
        infoPanel.add(lblRepertoire);

        // Section sauvegarde
        JPanel savPanel = createSection("☁️ Sauvegarder les données");
        savPanel.setLayout(new GridLayout(0, 1, 0, 8));

        JButton btnDrive  = bigBtn("☁️  Sauvegarder sur Google Drive / OneDrive", new Color(66, 133, 244));
        JButton btnUSB    = bigBtn("🔌  Sauvegarder sur USB / Disque externe", new Color(0, 130, 80));
        JButton btnLocal  = bigBtn("📁  Sauvegarder en local (choisir dossier)", new Color(13, 71, 161));
        JButton btnAuto   = bigBtn("⚡  Sauvegarde automatique", new Color(140, 60, 0));

        btnDrive.addActionListener(e -> sauvegarder("Drive"));
        btnUSB.addActionListener(e -> sauvegarder("USB"));
        btnLocal.addActionListener(e -> sauvegarder("LOCAL"));
        btnAuto.addActionListener(e -> sauvegardeAuto());

        savPanel.add(btnDrive); savPanel.add(btnUSB);
        savPanel.add(btnLocal); savPanel.add(btnAuto);

        // Section restauration
        JPanel restPanel = createSection("🔄 Restaurer les données");
        restPanel.setLayout(new GridLayout(0, 1, 0, 8));

        JButton btnRestore = bigBtn("📂  Restaurer depuis un fichier .zip", new Color(160, 30, 80));
        btnRestore.addActionListener(e -> restaurer());
        restPanel.add(btnRestore);

        if (!AuthService.getInstance().isAdmin()) {
            btnRestore.setEnabled(false);
            btnRestore.setToolTipText("Réservé aux administrateurs");
        }

        leftPanel.add(infoPanel, BorderLayout.NORTH);
        leftPanel.add(savPanel, BorderLayout.CENTER);
        leftPanel.add(restPanel, BorderLayout.SOUTH);

        // ─── PANNEAU DROITE : Historique & Logs ───
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setOpaque(false);

        // Liste des sauvegardes existantes
        JPanel listSection = createSection("📋 Sauvegardes disponibles (dossier local)");
        listSection.setLayout(new BorderLayout(0, 5));

        listModel = new DefaultListModel<>();
        backupList = new JList<>(listModel);
        backupList.setFont(new Font("Monospaced", Font.PLAIN, 11));
        backupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel listBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        listBtns.setOpaque(false);
        JButton btnRefList = new JButton("🔄 Actualiser");
        JButton btnOpenDir = new JButton("📂 Ouvrir dossier");
        btnRefList.addActionListener(e -> scanBackupsLocaux());
        btnOpenDir.addActionListener(e -> ouvrirDossierSauvegarde());
        listBtns.add(btnRefList); listBtns.add(btnOpenDir);

        listSection.add(new JScrollPane(backupList), BorderLayout.CENTER);
        listSection.add(listBtns, BorderLayout.SOUTH);

        // Journal des opérations
        JPanel logSection = createSection("📝 Journal des opérations");
        logSection.setLayout(new BorderLayout());
        logArea = new JTextArea(8, 40);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setEditable(false);
        logArea.setBackground(new Color(20, 25, 35));
        logArea.setForeground(new Color(100, 255, 100));
        logArea.setCaretColor(Color.GREEN);
        logSection.add(new JScrollPane(logArea), BorderLayout.CENTER);

        JButton btnClearLog = new JButton("🗑️ Effacer journal");
        btnClearLog.addActionListener(e -> logArea.setText(""));
        JPanel logBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logBottom.setOpaque(false);
        logBottom.add(btnClearLog);
        logSection.add(logBottom, BorderLayout.SOUTH);

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listSection, logSection);
        rightSplit.setDividerLocation(220);
        rightSplit.setDividerSize(4);
        rightSplit.setBorder(null);

        rightPanel.add(rightSplit, BorderLayout.CENTER);

        // ─── ASSEMBLAGE ───
        JPanel center = new JPanel(new BorderLayout(12, 0));
        center.setOpaque(false);
        center.add(leftPanel, BorderLayout.WEST);
        center.add(rightPanel, BorderLayout.CENTER);

        add(title, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        // ─── BARRE BAS ───
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        bottomBar.setBackground(new Color(240, 245, 255));
        bottomBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 215, 235)));

        JLabel hint = new JLabel("💡 Les sauvegardes sont des fichiers .ZIP contenant toutes les données de l'application. Elles peuvent être copiées dans Google Drive, OneDrive, ou une clé USB.");
        hint.setFont(new Font("Arial", Font.ITALIC, 11));
        hint.setForeground(new Color(80, 100, 130));
        bottomBar.add(hint);

        add(bottomBar, BorderLayout.SOUTH);
        log("Application démarrée. Base de données prête.");
    }

    private void sauvegarder(String type) {
        if (!AuthService.getInstance().isAdmin() && !"LOCAL".equals(type)) {
            // Tous peuvent sauvegarder
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choisir le dossier de sauvegarde");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // Proposer les dossiers Cloud connus
        if ("Drive".equals(type)) {
            File[] driveCandidates = {
                new File(System.getProperty("user.home") + "/Google Drive"),
                new File(System.getProperty("user.home") + "/Google Drive/Mon Drive"),
                new File(System.getProperty("user.home") + "/GoogleDrive"),
                new File("G:/Mon Drive"),
                new File("G:/My Drive")
            };
            for (File f : driveCandidates) {
                if (f.exists()) { chooser.setCurrentDirectory(f); break; }
            }
        } else if ("USB".equals(type)) {
            // Chercher disques amovibles
            File[] roots = File.listRoots();
            if (roots != null) {
                for (File r : roots) {
                    if (!r.getAbsolutePath().equals("C:\\") && !r.getAbsolutePath().equals("/")) {
                        chooser.setCurrentDirectory(r); break;
                    }
                }
            }
        }

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String dest = chooser.getSelectedFile().getAbsolutePath();
            log("Sauvegarde en cours vers: " + dest + " ...");
            try {
                String zipPath = backupService.creerSauvegarde(dest);
                File zip = new File(zipPath);
                long sizeKB = zip.length() / 1024;
                log("✅ Sauvegarde créée avec succès !");
                log("   Fichier: " + zip.getName() + " (" + sizeKB + " KB)");
                log("   Chemin: " + zipPath);
                scanBackupsLocaux();
                refreshInfo();
                JOptionPane.showMessageDialog(this,
                        "✅ Sauvegarde créée avec succès !\n\n" +
                        "Fichier: " + zip.getName() + "\n" +
                        "Taille: " + sizeKB + " KB\n" +
                        "Emplacement: " + zipPath,
                        "Sauvegarde réussie", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                log("❌ Erreur sauvegarde: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Erreur lors de la sauvegarde:\n" + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void sauvegardeAuto() {
        log("Sauvegarde automatique en cours...");
        try {
            String path = backupService.sauvegardeAutomatique();
            log("✅ Sauvegarde automatique créée: " + path);
            scanBackupsLocaux();
            JOptionPane.showMessageDialog(this, "Sauvegarde automatique créée !\n" + path, "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            log("❌ Erreur: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restaurer() {
        int warn = JOptionPane.showConfirmDialog(this,
                "⚠️  ATTENTION: La restauration remplacera toutes les données actuelles.\n" +
                "Une sauvegarde de sécurité sera créée automatiquement avant la restauration.\n\n" +
                "Voulez-vous continuer ?",
                "Confirmation de restauration", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (warn != JOptionPane.YES_OPTION) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Sélectionner le fichier de sauvegarde (.zip)");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Fichiers ZIP (*.zip)", "zip"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String zipPath = chooser.getSelectedFile().getAbsolutePath();
            log("Restauration depuis: " + zipPath + " ...");
            try {
                boolean ok = backupService.restaurerSauvegarde(zipPath);
                if (ok) {
                    log("✅ Restauration réussie !");
                    JOptionPane.showMessageDialog(this,
                            "✅ Restauration réussie !\nRedémarrez l'application pour charger les nouvelles données.",
                            "Restauration réussie", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                log("❌ Erreur restauration: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void scanBackupsLocaux() {
        listModel.clear();
        String autoDir = DatabaseManager.getInstance().getDbDir() + File.separator + "auto_backup";
        File dir = new File(autoDir);
        if (!dir.exists()) {
            listModel.addElement("  Aucune sauvegarde locale trouvée.");
            return;
        }
        File[] zips = dir.listFiles((d, n) -> n.endsWith(".zip"));
        if (zips == null || zips.length == 0) {
            listModel.addElement("  Aucune sauvegarde locale trouvée.");
            return;
        }
        Arrays.sort(zips, Comparator.comparingLong(File::lastModified).reversed());
        for (File z : zips) {
            long kb = z.length() / 1024;
            listModel.addElement("📦 " + z.getName() + " (" + kb + " KB)");
        }
    }

    private void ouvrirDossierSauvegarde() {
        try {
            String path = DatabaseManager.getInstance().getDbDir() + File.separator + "auto_backup";
            File dir = new File(path);
            if (!dir.exists()) dir.mkdirs();
            Desktop.getDesktop().open(dir);
        } catch (Exception ex) {
            log("Impossible d'ouvrir le dossier: " + ex.getMessage());
        }
    }

    private void refreshInfo() {
        long taille = backupService.getTailleDB();
        lblTailleDB.setText("Taille de la base: " + taille + " KB");
    }

    private void log(String message) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.append("[" + ts + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private JPanel createSection(String titre) {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 215, 235)),
                        titre, 0, 0,
                        new Font("Arial", Font.BOLD, 12),
                        new Color(13, 71, 161)),
                new EmptyBorder(8, 8, 8, 8)));
        p.setBackground(Color.WHITE);
        return p;
    }

    private JButton bigBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(new EmptyBorder(10, 14, 10, 14));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(bg.brighter()); }
            @Override public void mouseExited(MouseEvent e) { b.setBackground(bg); }
        });
        return b;
    }
}
