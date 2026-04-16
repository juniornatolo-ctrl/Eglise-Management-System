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
import java.util.Map;

public class SacrementPanel extends JPanel {

    private final SacrementDAO dao = new SacrementDAO();
    private final MembreDAO membreDAO = new MembreDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JPanel statsPanel;
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] ICONES = {
        "🔵 BAPTEME_EAU", "🕊️ BAPTEME_SAINT_ESPRIT", "🍞 SAINTE_CENE",
        "💍 MARIAGE", "👶 DEDICACE_ENFANT", "🙏 GUERISON", "✨ AUTRE"
    };

    public SacrementPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(new Color(248, 249, 255));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        buildUI();
        charger();
    }

    private void buildUI() {
        // ─── TITRE ───
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setOpaque(false);

        JLabel title = new JLabel("✝️ Registre des Sacrements");
        title.setFont(new Font("Georgia", Font.BOLD, 20));
        title.setForeground(new Color(13, 71, 161));

        // Stats rapides
        statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        statsPanel.setOpaque(false);

        // Boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnPanel.setOpaque(false);
        JButton btnAdd  = btn("➕ Enregistrer", new Color(0, 120, 60));
        JButton btnEdit = btn("✏️ Modifier", new Color(13, 71, 161));
        JButton btnDel  = btn("🗑️ Supprimer", new Color(180, 30, 30));
        JButton btnCert = btn("📜 Attestation", new Color(100, 60, 160));
        JButton btnRef  = btn("🔄", new Color(80, 80, 80));

        btnAdd.addActionListener(e -> ouvrirFormulaire(null));
        btnEdit.addActionListener(e -> { Sacrement s = getSelected(); if (s != null) ouvrirFormulaire(s); });
        btnDel.addActionListener(e -> supprimer());
        btnCert.addActionListener(e -> genererAttestation());
        btnRef.addActionListener(e -> charger());

        btnPanel.add(btnAdd); btnPanel.add(btnEdit); btnPanel.add(btnDel);
        btnPanel.add(btnCert); btnPanel.add(btnRef);

        JPanel titleRow = new JPanel(new BorderLayout(0, 4));
        titleRow.setOpaque(false);
        titleRow.add(title, BorderLayout.NORTH);
        titleRow.add(statsPanel, BorderLayout.SOUTH);

        topBar.add(titleRow, BorderLayout.WEST);
        topBar.add(btnPanel, BorderLayout.EAST);

        // ─── TABLE ───
        String[] cols = {"ID", "Type", "Nom du Membre", "Date", "Officiant", "Lieu", "Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(75);

        // Couleur par type de sacrement
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    String type = (String) tableModel.getValueAt(row, 1);
                    if (type != null && type.contains("BAPTEME")) c.setBackground(new Color(235, 245, 255));
                    else if (type != null && type.contains("MARIAGE")) c.setBackground(new Color(255, 245, 235));
                    else if (type != null && type.contains("SAINTE_CENE")) c.setBackground(new Color(245, 235, 255));
                    else c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 253));
                }
                return c;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { Sacrement s = getSelected(); if (s != null) ouvrirFormulaire(s); }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));

        add(topBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void charger() {
        List<Sacrement> list = dao.findAll();
        list.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        tableModel.setRowCount(0);
        for (Sacrement s : list) {
            tableModel.addRow(new Object[]{
                s.getId(),
                s.getType() != null ? s.getType().name() : "",
                s.getMembreNom() != null ? s.getMembreNom() : "",
                s.getDate() != null ? s.getDate().format(DF) : "",
                s.getOfficiant() != null ? s.getOfficiant() : "",
                s.getLieu() != null ? s.getLieu() : "",
                s.getNotes() != null ? s.getNotes() : ""
            });
        }
        refreshStats();
    }

    private void refreshStats() {
        statsPanel.removeAll();
        Map<String, Long> counts = dao.compterParType();
        String[] types = {"BAPTEME_EAU", "MARIAGE", "SAINTE_CENE", "DEDICACE_ENFANT"};
        String[] labels = {"🔵 Baptêmes", "💍 Mariages", "🍞 Saintes-Cènes", "👶 Dédicaces"};
        Color[] colors = {
            new Color(30, 100, 200), new Color(180, 100, 0),
            new Color(100, 30, 180), new Color(0, 140, 80)
        };
        for (int i = 0; i < types.length; i++) {
            long count = counts.getOrDefault(types[i], 0L);
            JLabel lbl = new JLabel(labels[i] + ": " + count);
            lbl.setFont(new Font("Arial", Font.BOLD, 11));
            lbl.setForeground(colors[i]);
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(colors[i]),
                    new EmptyBorder(2, 8, 2, 8)));
            statsPanel.add(lbl);
        }
        statsPanel.revalidate(); statsPanel.repaint();
    }

    private Sacrement getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Sélectionnez un sacrement.", "Attention", JOptionPane.WARNING_MESSAGE); return null; }
        return dao.findById((String) tableModel.getValueAt(row, 0)).orElse(null);
    }

    private void ouvrirFormulaire(Sacrement existing) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                existing == null ? "Enregistrer un sacrement" : "Modifier le sacrement",
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(500, 460);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(15, 20, 10, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(5, 5, 5, 5);

        // Type de sacrement
        JComboBox<Sacrement.TypeSacrement> cbType = new JComboBox<>(Sacrement.TypeSacrement.values());
        if (existing != null) cbType.setSelectedItem(existing.getType());

        // Membre
        List<Membre> membres = membreDAO.findActifs();
        String[] memItems = new String[membres.size() + 1];
        memItems[0] = "-- Saisir manuellement --";
        for (int i = 0; i < membres.size(); i++) memItems[i+1] = membres.get(i).getNomComplet();
        JComboBox<String> cbMembre = new JComboBox<>(memItems);

        JTextField fNomManuel  = field(existing != null ? existing.getMembreNom() : "");
        JTextField fDate       = field(existing != null && existing.getDate() != null ? existing.getDate().format(DF) : LocalDate.now().format(DF));
        JTextField fOfficiant  = field(existing != null ? existing.getOfficiant() : "Pasteur");
        JTextField fLieu       = field(existing != null ? existing.getLieu() : "Salle principale");
        JTextField fTemoins    = field(existing != null ? existing.getTemoins() : "");
        JTextField fConjoint   = field(existing != null ? existing.getConjointNom() : "");
        JTextField fParent     = field(existing != null ? existing.getParentNom() : "");
        JTextArea  fNotes      = new JTextArea(existing != null ? existing.getNotes() : "", 3, 20);
        fNotes.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        if (existing != null && existing.getMembreNom() != null) {
            for (int i = 1; i < memItems.length; i++) {
                if (memItems[i].equals(existing.getMembreNom())) { cbMembre.setSelectedIndex(i); break; }
            }
        }

        // Masquer/afficher champs selon type
        cbType.addActionListener(e -> {
            Sacrement.TypeSacrement t = (Sacrement.TypeSacrement) cbType.getSelectedItem();
            fConjoint.setEnabled(t == Sacrement.TypeSacrement.MARIAGE);
            fParent.setEnabled(t == Sacrement.TypeSacrement.DEDICACE_ENFANT);
        });

        Object[][] rows = {
            {"Type de sacrement *", cbType},
            {"Membre (liste)", cbMembre},
            {"Nom du bénéficiaire *", fNomManuel},
            {"Date * (dd/MM/yyyy)", fDate},
            {"Officiant / Pasteur", fOfficiant},
            {"Lieu", fLieu},
            {"Témoins", fTemoins},
            {"Nom conjoint(e) (mariage)", fConjoint},
            {"Nom parent(s) (dédicace)", fParent},
            {"Notes / Observations", new JScrollPane(fNotes)}
        };

        for (int i = 0; i < rows.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0.35; form.add(new JLabel((String) rows[i][0]), g);
            g.gridx = 1; g.weightx = 0.65; form.add((Component) rows[i][1], g);
        }

        // Sync membre depuis liste
        cbMembre.addActionListener(e -> {
            int idx = cbMembre.getSelectedIndex();
            if (idx > 0) fNomManuel.setText(membres.get(idx - 1).getNomComplet());
        });

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        JButton bSave = new JButton(existing == null ? "✅ Enregistrer" : "✅ Mettre à jour");
        bSave.setBackground(new Color(0, 120, 60)); bSave.setForeground(Color.WHITE); bSave.setFocusPainted(false);
        JButton bCancel = new JButton("Annuler");
        bp.add(bSave); bp.add(bCancel);

        bSave.addActionListener(e -> {
            if (fNomManuel.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Le nom du bénéficiaire est obligatoire.", "Validation", JOptionPane.WARNING_MESSAGE); return;
            }
            Sacrement s = existing != null ? existing : new Sacrement();
            s.setType((Sacrement.TypeSacrement) cbType.getSelectedItem());
            s.setMembreNom(fNomManuel.getText().trim());
            if (cbMembre.getSelectedIndex() > 0) s.setMembreId(membres.get(cbMembre.getSelectedIndex()-1).getId());
            s.setOfficiant(fOfficiant.getText().trim());
            s.setLieu(fLieu.getText().trim());
            s.setTemoins(fTemoins.getText().trim());
            s.setConjointNom(fConjoint.getText().trim());
            s.setParentNom(fParent.getText().trim());
            s.setNotes(fNotes.getText().trim());
            try { s.setDate(LocalDate.parse(fDate.getText().trim(), DF)); } catch (Exception ex) { s.setDate(LocalDate.now()); }

            boolean ok = existing == null ? dao.add(s) : dao.update(s);
            if (ok) { charger(); dlg.dispose(); }
            else JOptionPane.showMessageDialog(dlg, "Erreur d'enregistrement.", "Erreur", JOptionPane.ERROR_MESSAGE);
        });
        bCancel.addActionListener(e -> dlg.dispose());

        dlg.setLayout(new BorderLayout());
        dlg.add(new JScrollPane(form), BorderLayout.CENTER);
        dlg.add(bp, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void supprimer() {
        Sacrement s = getSelected(); if (s == null) return;
        int c = JOptionPane.showConfirmDialog(this,
                "Supprimer ce sacrement (" + s.getType() + " - " + s.getMembreNom() + ") ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) { dao.deleteById(s.getId()); charger(); }
    }

    private void genererAttestation() {
        Sacrement s = getSelected(); if (s == null) return;
        String texte = genererTexteAttestation(s);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Attestation - " + s.getType(), Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(600, 480); dlg.setLocationRelativeTo(this);
        JTextArea ta = new JTextArea(texte);
        ta.setFont(new Font("Serif", Font.PLAIN, 13)); ta.setEditable(true);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bPrint = new JButton("🖨️ Imprimer"); JButton bClose = new JButton("Fermer");
        bPrint.addActionListener(e -> {
            try { ta.print(); } catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "Erreur impression."); }
        });
        bClose.addActionListener(e -> dlg.dispose());
        bp.add(bPrint); bp.add(bClose);
        dlg.add(new JScrollPane(ta), BorderLayout.CENTER);
        dlg.add(bp, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private String genererTexteAttestation(Sacrement s) {
        String sep = "─".repeat(55);
        return "\n\n" +
               "          ÉGLISE APOSTOLIQUE « SOURCE DE GRÂCE »\n\n" +
               "                    ATTESTATION\n" +
               "            " + s.getType().name().replace("_", " ") + "\n\n" +
               sep + "\n\n" +
               "  Nous soussignés, attestons par la présente que :\n\n" +
               "  Nom & Prénom  : " + s.getMembreNom() + "\n" +
               (s.getConjointNom() != null && !s.getConjointNom().isEmpty()
                       ? "  Conjoint(e)   : " + s.getConjointNom() + "\n" : "") +
               "  A reçu le sacrement de : " + s.getType().name().replace("_", " ") + "\n" +
               "  Date          : " + (s.getDate() != null ? s.getDate().format(DF) : "") + "\n" +
               "  Lieu          : " + (s.getLieu() != null ? s.getLieu() : "") + "\n" +
               "  Officiant     : " + (s.getOfficiant() != null ? s.getOfficiant() : "") + "\n" +
               (s.getTemoins() != null && !s.getTemoins().isEmpty()
                       ? "  Témoins       : " + s.getTemoins() + "\n" : "") +
               "\n" + sep + "\n\n" +
               "  Notes : " + (s.getNotes() != null ? s.getNotes() : "") + "\n\n\n" +
               "  Fait à ______________, le " + LocalDate.now().format(DF) + "\n\n\n" +
               "  Signature du Pasteur                Sceau de l'Église\n\n" +
               "  _______________________           ___________________\n";
    }

    private void styleTable(JTable t) {
        t.setRowHeight(26);
        t.setFont(new Font("Arial", Font.PLAIN, 12));
        t.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        t.getTableHeader().setBackground(new Color(13, 71, 161));
        t.getTableHeader().setForeground(Color.WHITE);
        t.setSelectionBackground(new Color(200, 220, 255));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 1));
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
}
