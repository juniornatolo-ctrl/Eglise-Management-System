package com.eglise.ui;
import com.eglise.model.*;
import com.eglise.service.*;
import com.eglise.db.*;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FinancePanel extends JPanel {

    private final TransactionDAO dao = new TransactionDAO();
    private final MembreDAO membreDAO = new MembreDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblSolde, lblEntrees, lblDepenses;
    private JComboBox<String> cbFiltreType;
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FinancePanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(new Color(248, 249, 255));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        buildUI();
        chargerTransactions();
    }

    private void buildUI() {
        // ─── TITRE ───
        JLabel title = new JLabel("💰 Gestion Financière");
        title.setFont(new Font("Georgia", Font.BOLD, 20));
        title.setForeground(new Color(13, 71, 161));

        // ─── CARTES RÉSUMÉ ───
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        cardsPanel.setOpaque(false);
        lblEntrees  = card("📥 Total Entrées", "0 FCFA", new Color(0, 120, 60));
        lblDepenses = card("📤 Total Dépenses", "0 FCFA", new Color(180, 30, 30));
        lblSolde    = card("💵 Solde Caisse", "0 FCFA", new Color(13, 71, 161));
        cardsPanel.add(wrapCard(lblEntrees, "📥 Entrées", new Color(0, 120, 60)));
        cardsPanel.add(wrapCard(lblDepenses, "📤 Dépenses", new Color(180, 30, 30)));
        cardsPanel.add(wrapCard(lblSolde, "💵 Solde", new Color(13, 71, 161)));

        // ─── TOOLBAR ───
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);

        cbFiltreType = new JComboBox<>(new String[]{"TOUTES", "OFFRANDE", "DIME", "DON", "CONTRIBUTION", "DEPENSE", "AUTRE"});
        cbFiltreType.addActionListener(e -> filtrer());

        JButton btnAdd    = btn("➕ Nouvelle transaction", new Color(0, 120, 60));
        JButton btnEdit   = btn("✏️ Modifier", new Color(13, 71, 161));
        JButton btnDel    = btn("🗑️ Supprimer", new Color(180, 30, 30));
        JButton btnRapport = btn("📊 Rapport mensuel", new Color(100, 60, 160));
        JButton btnExport = btn("📤 Export CSV", new Color(80, 80, 80));

        btnAdd.addActionListener(e -> ouvrirFormulaire(null));
        btnEdit.addActionListener(e -> { Transaction t = getSelected(); if (t != null) ouvrirFormulaire(t); });
        btnDel.addActionListener(e -> supprimer());
        btnRapport.addActionListener(e -> genererRapport());
        btnExport.addActionListener(e -> exporterCSV());

        if (!AuthService.getInstance().isTrésorier()) {
            btnAdd.setEnabled(false); btnEdit.setEnabled(false); btnDel.setEnabled(false);
        }

        toolbar.add(new JLabel("Filtre: ")); toolbar.add(cbFiltreType);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(btnAdd); toolbar.add(btnEdit); toolbar.add(btnDel);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(btnRapport); toolbar.add(btnExport);

        // ─── TABLE ───
        String[] cols = {"ID", "Date", "Type", "Description", "Membre", "Mode Paiement", "Montant (FCFA)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(13, 71, 161));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(200, 220, 255));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        // Renderer couleur selon type
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    String type = (String) tableModel.getValueAt(row, 2);
                    if ("DEPENSE".equals(type)) c.setBackground(new Color(255, 240, 240));
                    else c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 255, 245));
                }
                // Aligner montant à droite
                if (col == 6) ((JLabel)c).setHorizontalAlignment(SwingConstants.RIGHT);
                return c;
            }
        });

        table.getColumnModel().getColumn(0).setMaxWidth(75);
        table.getColumnModel().getColumn(6).setMinWidth(120);

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { Transaction t = getSelected(); if (t != null) ouvrirFormulaire(t); }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));

        // ─── ASSEMBLAGE ───
        JPanel topPanel = new JPanel(new BorderLayout(0, 8));
        topPanel.setOpaque(false);
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(cardsPanel, BorderLayout.CENTER);
        topPanel.add(toolbar, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void chargerTransactions() {
        refreshTable(dao.findAll());
        updateSummary();
    }

    private void filtrer() {
        String type = (String) cbFiltreType.getSelectedItem();
        if ("TOUTES".equals(type)) {
            refreshTable(dao.findAll());
        } else {
            refreshTable(dao.findByType(Transaction.Type.valueOf(type)));
        }
    }

    private void refreshTable(List<Transaction> list) {
        tableModel.setRowCount(0);
        for (Transaction t : list) {
            tableModel.addRow(new Object[]{
                t.getId(),
                t.getDate().format(DF),
                t.getType().name(),
                t.getDescription() != null ? t.getDescription() : "",
                t.getMembreNom() != null ? t.getMembreNom() : "",
                t.getModePaiement(),
                String.format("%,.0f", t.getMontant())
            });
        }
        updateSummary();
    }

    private void updateSummary() {
        lblEntrees.setText(String.format("%,.0f FCFA", dao.totalEntrees()));
        lblDepenses.setText(String.format("%,.0f FCFA", dao.totalDepenses()));
        double solde = dao.solde();
        lblSolde.setText(String.format("%,.0f FCFA", solde));
        lblSolde.setForeground(solde >= 0 ? Color.WHITE : new Color(255, 180, 180));
    }

    private Transaction getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Sélectionnez une transaction.", "Attention", JOptionPane.WARNING_MESSAGE); return null; }
        String id = (String) tableModel.getValueAt(row, 0);
        return dao.findById(id).orElse(null);
    }

    private void ouvrirFormulaire(Transaction existing) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                existing == null ? "Nouvelle transaction" : "Modifier transaction",
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(480, 400);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(15, 20, 10, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 5, 6, 5);

        JComboBox<Transaction.Type> cbType = new JComboBox<>(Transaction.Type.values());
        JTextField fMontant = fieldNum(existing != null ? String.valueOf((int)existing.getMontant()) : "");
        JTextField fDesc    = field(existing != null ? existing.getDescription() : "");
        JTextField fDate    = field(existing != null ? existing.getDate().format(DF) : LocalDate.now().format(DF));

        // Sélection membre
        List<Membre> membres = membreDAO.findActifs();
        String[] membreItems = new String[membres.size() + 1];
        membreItems[0] = "-- Aucun --";
        for (int i = 0; i < membres.size(); i++) membreItems[i + 1] = membres.get(i).getNomComplet();
        JComboBox<String> cbMembre = new JComboBox<>(membreItems);
        if (existing != null && existing.getMembreNom() != null) cbMembre.setSelectedItem(existing.getMembreNom());

        JComboBox<String> cbMode = new JComboBox<>(new String[]{"ESPECES", "MOBILE_MONEY", "VIREMENT", "CHEQUE"});
        if (existing != null) { cbType.setSelectedItem(existing.getType()); cbMode.setSelectedItem(existing.getModePaiement()); }

        Object[][] rows = {
            {"Type *", cbType}, {"Montant (FCFA) *", fMontant},
            {"Description", fDesc}, {"Date (dd/MM/yyyy)", fDate},
            {"Membre / Donateur", cbMembre}, {"Mode de paiement", cbMode}
        };
        for (int i = 0; i < rows.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0.35; form.add(new JLabel((String) rows[i][0]), g);
            g.gridx = 1; g.weightx = 0.65; form.add((Component) rows[i][1], g);
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        JButton btnSave = new JButton(existing == null ? "✅ Enregistrer" : "✅ Mettre à jour");
        btnSave.setBackground(new Color(0, 120, 60)); btnSave.setForeground(Color.WHITE); btnSave.setFocusPainted(false);
        JButton btnCancel = new JButton("Annuler");
        btnPanel.add(btnSave); btnPanel.add(btnCancel);

        btnSave.addActionListener(e -> {
            try {
                double montant = Double.parseDouble(fMontant.getText().trim().replace(" ", "").replace(",", "."));
                if (montant <= 0) throw new NumberFormatException();

                Transaction t = existing != null ? existing : new Transaction();
                t.setType((Transaction.Type) cbType.getSelectedItem());
                t.setMontant(montant);
                t.setDescription(fDesc.getText().trim());
                t.setModePaiement((String) cbMode.getSelectedItem());
                try { t.setDate(LocalDate.parse(fDate.getText().trim(), DF)); } catch (Exception ignored) {}
                if (cbMembre.getSelectedIndex() > 0) {
                    Membre sel = membres.get(cbMembre.getSelectedIndex() - 1);
                    t.setMembreId(sel.getId());
                    t.setMembreNom(sel.getNomComplet());
                }

                boolean ok = existing == null ? dao.add(t) : dao.update(t);
                if (ok) { chargerTransactions(); dlg.dispose(); }
                else JOptionPane.showMessageDialog(dlg, "Erreur d'enregistrement.", "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Montant invalide (nombres positifs uniquement).", "Validation", JOptionPane.WARNING_MESSAGE);
            }
        });
        btnCancel.addActionListener(e -> dlg.dispose());

        dlg.setLayout(new BorderLayout());
        dlg.add(new JScrollPane(form), BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void supprimer() {
        Transaction t = getSelected();
        if (t == null) return;
        int c = JOptionPane.showConfirmDialog(this, "Supprimer cette transaction ?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) { dao.deleteById(t.getId()); chargerTransactions(); }
    }

    private void genererRapport() {
        String moisStr = JOptionPane.showInputDialog(this, "Mois (1-12):", LocalDate.now().getMonthValue());
        if (moisStr == null) return;
        try {
            int mois = Integer.parseInt(moisStr.trim());
            String rapport = ReportService.getInstance().rapportFinancierMensuel(mois, LocalDate.now().getYear());
            JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Rapport Financier - Mois " + mois, Dialog.ModalityType.APPLICATION_MODAL);
            dlg.setSize(650, 500); dlg.setLocationRelativeTo(this);
            JTextArea ta = new JTextArea(rapport);
            ta.setFont(new Font("Monospaced", Font.PLAIN, 12)); ta.setEditable(false);
            JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton bSave = new JButton("💾 Enregistrer"); JButton bClose = new JButton("Fermer");
            bSave.addActionListener(ev -> {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (fc.showSaveDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                    try {
                        ReportService.getInstance().sauvegarderRapport(rapport, fc.getSelectedFile().getAbsolutePath(), "rapport_financier_" + mois);
                        JOptionPane.showMessageDialog(dlg, "Rapport sauvegardé !");
                    } catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "Erreur: " + ex.getMessage()); }
                }
            });
            bClose.addActionListener(ev -> dlg.dispose());
            bp.add(bSave); bp.add(bClose);
            dlg.add(new JScrollPane(ta), BorderLayout.CENTER);
            dlg.add(bp, BorderLayout.SOUTH);
            dlg.setVisible(true);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Mois invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exporterCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Exporter transactions en CSV");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                List<Transaction> list = dao.findAll();
                String[] headers = {"ID", "Date", "Type", "Description", "Membre", "Mode Paiement", "Montant FCFA"};
                String[][] data = new String[list.size()][];
                for (int i = 0; i < list.size(); i++) {
                    Transaction t = list.get(i);
                    data[i] = new String[]{
                        t.getId(), t.getDate().format(DF), t.getType().name(),
                        t.getDescription(), t.getMembreNom(), t.getModePaiement(),
                        String.valueOf(t.getMontant())
                    };
                }
                String path = BackupService.getInstance().exporterCSV(fc.getSelectedFile().getAbsolutePath(), data, headers, "finances_export");
                JOptionPane.showMessageDialog(this, "Export CSV:\n" + path);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ─── Helpers ───
    private JLabel card(String label, String val, Color color) {
        JLabel l = new JLabel(val, JLabel.CENTER);
        l.setFont(new Font("Georgia", Font.BOLD, 20));
        l.setForeground(Color.WHITE);
        return l;
    }

    private JPanel wrapCard(JLabel valLabel, String title, Color color) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(color);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker()),
                new EmptyBorder(10, 8, 10, 8)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        p.add(valLabel, gbc);
        gbc.gridy = 1;
        JLabel lbl = new JLabel(title, JLabel.CENTER);
        lbl.setFont(new Font("Arial", Font.PLAIN, 11));
        lbl.setForeground(new Color(220, 240, 255));
        p.add(lbl, gbc);
        return p;
    }

    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JTextField field(String val) {
        JTextField f = new JTextField(val);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 7, 4, 7)));
        return f;
    }

    private JTextField fieldNum(String val) {
        JTextField f = field(val);
        f.setHorizontalAlignment(SwingConstants.RIGHT);
        return f;
    }
}
