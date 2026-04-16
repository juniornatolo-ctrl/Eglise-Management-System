package com.eglise.ui;

import com.eglise.dao.MembreDAO;
import com.eglise.model.Membre;
import com.eglise.service.AuthService;
import com.eglise.service.BackupService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MembrePanel extends JPanel {

    private final MembreDAO dao = new MembreDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtRecherche;
    private List<Membre> currentList;
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] ROLES = {"MEMBRE", "RESPONSABLE", "SECRETAIRE", "TRESORIER", "PASTEUR"};
    private static final String[] DEPARTEMENTS = {
        "Culte & Louange", "Jeunesse", "Femmes", "Hommes",
        "Enfants", "Évangélisation", "Intercesseurs", "Administration", "Autre"
    };

    public MembrePanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(new Color(248, 249, 255));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        buildUI();
        chargerMembres();
    }

    private void buildUI() {
        // ─── TOOLBAR ───
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        JLabel title = new JLabel("👥 Gestion des Membres");
        title.setFont(new Font("Georgia", Font.BOLD, 20));
        title.setForeground(new Color(13, 71, 161));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchPanel.setOpaque(false);
        txtRecherche = new JTextField(20);
        txtRecherche.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 230)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        txtRecherche.setFont(new Font("Arial", Font.PLAIN, 13));
        txtRecherche.putClientProperty("JTextField.placeholderText", "Rechercher...");
        txtRecherche.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { filtrer(txtRecherche.getText()); }
        });

        JButton btnSearch = btnAction("🔍", "Rechercher", new Color(70, 130, 180));
        btnSearch.addActionListener(e -> filtrer(txtRecherche.getText()));

        searchPanel.add(new JLabel("Recherche: "));
        searchPanel.add(txtRecherche);
        searchPanel.add(btnSearch);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnPanel.setOpaque(false);

        JButton btnAdd    = btnAction("➕ Ajouter", null, new Color(0, 130, 80));
        JButton btnEdit   = btnAction("✏️ Modifier", null, new Color(13, 71, 161));
        JButton btnDel    = btnAction("🗑️ Supprimer", null, new Color(180, 30, 30));
        JButton btnExport = btnAction("📤 Exporter CSV", null, new Color(100, 60, 160));
        JButton btnRef    = btnAction("🔄", null, new Color(80, 80, 80));

        btnAdd.addActionListener(e -> ouvrirFormulaire(null));
        btnEdit.addActionListener(e -> {
            Membre m = getSelectedMembre();
            if (m != null) ouvrirFormulaire(m);
        });
        btnDel.addActionListener(e -> supprimerMembre());
        btnExport.addActionListener(e -> exporterCSV());
        btnRef.addActionListener(e -> chargerMembres());

        if (!AuthService.getInstance().isSecrétaire()) { btnAdd.setEnabled(false); btnEdit.setEnabled(false); btnDel.setEnabled(false); }

        btnPanel.add(btnAdd); btnPanel.add(btnEdit); btnPanel.add(btnDel); btnPanel.add(btnExport); btnPanel.add(btnRef);

        toolbar.add(title, BorderLayout.WEST);
        toolbar.add(searchPanel, BorderLayout.CENTER);
        toolbar.add(btnPanel, BorderLayout.EAST);

        // ─── TABLE ───
        String[] cols = {"ID", "Nom Complet", "Téléphone", "Email", "Département", "Rôle", "Adhésion", "Statut"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(13, 71, 161));
        table.getTableHeader().setForeground(Color.BLACK);
        table.setSelectionBackground(new Color(200, 220, 255));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        // Colorier lignes alternées
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel){
                 c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 245, 255));
                 c.setBackground(Color.BLACK);
               } else {
               c.setBackground(Color.WHITE);
               }

        // Colonne ID cachée mais utilisable
        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.getColumnModel().getColumn(0).setMinWidth(60);

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Membre m = getSelectedMembre();
                    if (m != null) ouvrirFormulaire(m);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));

        // ─── STATUS BAR ───
        JLabel lblCount = new JLabel();
        lblCount.setFont(new Font("Arial", Font.ITALIC, 11));
        lblCount.setForeground(Color.GRAY);

        add(toolbar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(lblCount, BorderLayout.SOUTH);
    }

    private void chargerMembres() {
        currentList = dao.findAll();
        refreshTable(currentList);
    }

    private void filtrer(String query) {
        if (query == null || query.trim().isEmpty()) {
            refreshTable(dao.findAll());
        } else {
            refreshTable(dao.rechercher(query.trim()));
        }
    }

    private void refreshTable(List<Membre> list) {
        currentList = list;
        tableModel.setRowCount(0);
        for (Membre m : list) {
            tableModel.addRow(new Object[]{
                m.getId(),
                m.getNomComplet(),
                m.getTelephone(),
                m.getEmail() != null ? m.getEmail() : "",
                m.getDepartement() != null ? m.getDepartement() : "",
                m.getRole(),
                m.getDateAdhesion() != null ? m.getDateAdhesion().format(DF) : "",
                m.isActif() ? "✅ Actif" : "❌ Inactif"
            });
        }
    }

    private Membre getSelectedMembre() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un membre.", "Attention", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String id = (String) tableModel.getValueAt(row, 0);
        return dao.findById(id).orElse(null);
    }

    private void ouvrirFormulaire(Membre existing) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                existing == null ? "Ajouter un membre" : "Modifier un membre",
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(520, 480);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(15, 20, 10, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 5, 5, 5);

        // Champs
        JTextField fNom       = field(existing != null ? existing.getNom() : "");
        JTextField fPrenom    = field(existing != null ? existing.getPrenom() : "");
        JTextField fTel       = field(existing != null ? existing.getTelephone() : "");
        JTextField fEmail     = field(existing != null ? existing.getEmail() : "");
        JTextField fAdresse   = field(existing != null ? existing.getAdresse() : "");
        JComboBox<String> cbRole   = new JComboBox<>(ROLES);
        JComboBox<String> cbDept   = new JComboBox<>(DEPARTEMENTS);
        JTextField fDateNaiss = field(existing != null && existing.getDateNaissance() != null
                ? existing.getDateNaissance().format(DF) : "");
        JCheckBox cbActif     = new JCheckBox("Membre actif", existing == null || existing.isActif());

        if (existing != null) {
            cbRole.setSelectedItem(existing.getRole());
            cbDept.setSelectedItem(existing.getDepartement());
        }

        Object[][] rows = {
            {"Nom *", fNom}, {"Prénom *", fPrenom}, {"Téléphone *", fTel},
            {"Email", fEmail}, {"Adresse", fAdresse},
            {"Rôle", cbRole}, {"Département", cbDept},
            {"Date de naissance (dd/MM/yyyy)", fDateNaiss}, {"", cbActif}
        };

        for (int i = 0; i < rows.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0.35;
            form.add(new JLabel((String) rows[i][0]), g);
            g.gridx = 1; g.weightx = 0.65;
            form.add((Component) rows[i][1], g);
        }

        // Boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        JButton btnSave = new JButton(existing == null ? "✅ Enregistrer" : "✅ Mettre à jour");
        btnSave.setBackground(new Color(0, 120, 80));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        JButton btnCancel = new JButton("Annuler");
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        btnSave.addActionListener(e -> {
            if (fNom.getText().trim().isEmpty() || fPrenom.getText().trim().isEmpty() || fTel.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Les champs Nom, Prénom et Téléphone sont obligatoires.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Membre m = existing != null ? existing : new Membre();
            m.setNom(fNom.getText().trim());
            m.setPrenom(fPrenom.getText().trim());
            m.setTelephone(fTel.getText().trim());
            m.setEmail(fEmail.getText().trim());
            m.setAdresse(fAdresse.getText().trim());
            m.setRole((String) cbRole.getSelectedItem());
            m.setDepartement((String) cbDept.getSelectedItem());
            m.setActif(cbActif.isSelected());
            try {
                if (!fDateNaiss.getText().trim().isEmpty())
                    m.setDateNaissance(LocalDate.parse(fDateNaiss.getText().trim(), DF));
            } catch (Exception ignored) {}

            boolean ok = existing == null ? dao.add(m) : dao.update(m);
            if (ok) {
                chargerMembres();
                dlg.dispose();
                JOptionPane.showMessageDialog(this, "Membre " + (existing == null ? "ajouté" : "modifié") + " avec succès !");
            } else {
                JOptionPane.showMessageDialog(dlg, "Erreur lors de l'enregistrement.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnCancel.addActionListener(e -> dlg.dispose());

        dlg.setLayout(new BorderLayout());
        dlg.add(new JScrollPane(form), BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void supprimerMembre() {
        Membre m = getSelectedMembre();
        if (m == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Supprimer le membre " + m.getNomComplet() + " ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            dao.deleteById(m.getId());
            chargerMembres();
        }
    }

    private void exporterCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Exporter membres en CSV");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                List<Membre> list = dao.findAll();
                String[] headers = {"ID", "Nom", "Prénom", "Téléphone", "Email", "Adresse", "Département", "Rôle", "Adhésion", "Actif"};
                String[][] data = new String[list.size()][];
                DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (int i = 0; i < list.size(); i++) {
                    Membre m = list.get(i);
                    data[i] = new String[]{
                        m.getId(), m.getNom(), m.getPrenom(), m.getTelephone(),
                        m.getEmail(), m.getAdresse(), m.getDepartement(), m.getRole(),
                        m.getDateAdhesion() != null ? m.getDateAdhesion().format(df) : "",
                        m.isActif() ? "Oui" : "Non"
                    };
                }
                String path = BackupService.getInstance().exporterCSV(
                        fc.getSelectedFile().getAbsolutePath(), data, headers, "membres_export");
                JOptionPane.showMessageDialog(this, "Export CSV créé:\n" + path);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur export: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JTextField field(String val) {
        JTextField f = new JTextField(val, 18);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 7, 4, 7)));
        return f;
    }

    private JButton btnAction(String text, String tooltip, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (tooltip != null) b.setToolTipText(tooltip);
        return b;
    }
}
}
}
