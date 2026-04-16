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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EvenementPanel extends JPanel {

    private final EvenementDAO dao = new EvenementDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JToggleButton btnAVenir, btnPasses, btnTous;
    private static final DateTimeFormatter DF  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TF  = DateTimeFormatter.ofPattern("HH:mm");

    public EvenementPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(new Color(248, 249, 255));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        buildUI();
        chargerEvenements("AVENIR");
    }

    private void buildUI() {
        // ─── TITRE & BOUTONS FILTRE ───
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setOpaque(false);

        JLabel title = new JLabel("📅 Gestion des Événements");
        title.setFont(new Font("Georgia", Font.BOLD, 20));
        title.setForeground(new Color(13, 71, 161));

        // Filtre toggle
        JPanel filtrePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        filtrePanel.setOpaque(false);
        ButtonGroup bg = new ButtonGroup();
        btnAVenir = new JToggleButton("📆 À venir"); btnAVenir.setSelected(true);
        btnPasses = new JToggleButton("📁 Passés");
        btnTous   = new JToggleButton("📋 Tous");
        for (JToggleButton tb : new JToggleButton[]{btnAVenir, btnPasses, btnTous}) {
            bg.add(tb); styleTB(tb); filtrePanel.add(tb);
        }
        btnAVenir.addActionListener(e -> chargerEvenements("AVENIR"));
        btnPasses.addActionListener(e -> chargerEvenements("PASSES"));
        btnTous.addActionListener(e -> chargerEvenements("TOUS"));

        // Boutons actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionPanel.setOpaque(false);
        JButton btnAdd  = btn("➕ Créer", new Color(0, 120, 60));
        JButton btnEdit = btn("✏️ Modifier", new Color(13, 71, 161));
        JButton btnDel  = btn("🗑️ Supprimer", new Color(180, 30, 30));
        JButton btnStat = btn("📊 Statut", new Color(100, 60, 160));

        btnAdd.addActionListener(e -> ouvrirFormulaire(null));
        btnEdit.addActionListener(e -> { Evenement ev = getSelected(); if (ev != null) ouvrirFormulaire(ev); });
        btnDel.addActionListener(e -> supprimer());
        btnStat.addActionListener(e -> changerStatut());

        if (!AuthService.getInstance().isSecrétaire()) {
            btnAdd.setEnabled(false); btnEdit.setEnabled(false); btnDel.setEnabled(false);
        }

        actionPanel.add(btnAdd); actionPanel.add(btnEdit);
        actionPanel.add(btnDel); actionPanel.add(btnStat);

        topBar.add(title, BorderLayout.NORTH);
        topBar.add(filtrePanel, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);

        // ─── TABLE ───
        String[] cols = {"ID", "Titre", "Type", "Date", "Heure", "Lieu", "Responsable", "Statut"};
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

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    String statut = (String) tableModel.getValueAt(row, 7);
                    if ("TERMINE".equals(statut)) c.setBackground(new Color(240, 245, 240));
                    else if ("ANNULE".equals(statut)) c.setBackground(new Color(250, 235, 235));
                    else c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 248, 255));
                }
                return c;
            }
        });

        table.getColumnModel().getColumn(0).setMaxWidth(70);
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { Evenement ev = getSelected(); if (ev != null) ouvrirFormulaire(ev); }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));

        // ─── PANNEAU DÉTAIL ───
        JPanel detailPanel = buildDetailPanel();

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Evenement ev = getSelectedQuiet();
                updateDetail(ev, detailPanel);
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll, detailPanel);
        split.setDividerLocation(420);
        split.setDividerSize(4);
        split.setBorder(null);

        add(topBar, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildDetailPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(250, 252, 255));
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 215, 240)),
                "📋 Détails de l'événement", 0, 0,
                new Font("Arial", Font.BOLD, 12), new Color(13, 71, 161)));
        JLabel empty = new JLabel("  Sélectionnez un événement pour voir ses détails.", JLabel.LEFT);
        empty.setFont(new Font("Arial", Font.ITALIC, 12));
        empty.setForeground(Color.GRAY);
        p.add(empty, BorderLayout.CENTER);
        return p;
    }

    private void updateDetail(Evenement ev, JPanel detailPanel) {
        detailPanel.removeAll();
        if (ev == null) {
            JLabel l = new JLabel("  Sélectionnez un événement.", JLabel.LEFT);
            l.setFont(new Font("Arial", Font.ITALIC, 12)); l.setForeground(Color.GRAY);
            detailPanel.add(l, BorderLayout.CENTER);
        } else {
            JPanel grid = new JPanel(new GridLayout(0, 2, 10, 4));
            grid.setBackground(new Color(250, 252, 255));
            grid.setBorder(new EmptyBorder(8, 15, 8, 15));

            addDetail(grid, "Titre:", ev.getTitre());
            addDetail(grid, "Type:", ev.getType() != null ? ev.getType().name() : "");
            addDetail(grid, "Date:", ev.getDate() != null ? ev.getDate().format(DF) : "");
            addDetail(grid, "Heure début:", ev.getHeureDebut() != null ? ev.getHeureDebut().format(TF) : "");
            addDetail(grid, "Heure fin:", ev.getHeureFin() != null ? ev.getHeureFin().format(TF) : "");
            addDetail(grid, "Lieu:", ev.getLieu() != null ? ev.getLieu() : "");
            addDetail(grid, "Responsable:", ev.getResponsable() != null ? ev.getResponsable() : "");
            addDetail(grid, "Statut:", ev.getStatut() != null ? ev.getStatut() : "");
            addDetail(grid, "Description:", ev.getDescription() != null ? ev.getDescription() : "");
            addDetail(grid, "Participants:", String.valueOf(ev.getParticipants().size()));

            detailPanel.add(grid, BorderLayout.CENTER);
        }
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private void addDetail(JPanel p, String label, String value) {
        JLabel lbl = new JLabel(label); lbl.setFont(new Font("Arial", Font.BOLD, 11));
        JLabel val = new JLabel(value); val.setFont(new Font("Arial", Font.PLAIN, 11));
        p.add(lbl); p.add(val);
    }

    private void chargerEvenements(String filtre) {
        List<Evenement> list;
        switch (filtre) {
            case "AVENIR": list = dao.findAVenir(); break;
            case "PASSES": list = dao.findPassés(); break;
            default: list = dao.findAll(); break;
        }
        refreshTable(list);
    }

    private void refreshTable(List<Evenement> list) {
        tableModel.setRowCount(0);
        for (Evenement ev : list) {
            tableModel.addRow(new Object[]{
                ev.getId(),
                ev.getTitre(),
                ev.getType() != null ? ev.getType().name() : "",
                ev.getDate() != null ? ev.getDate().format(DF) : "",
                ev.getHeureDebut() != null ? ev.getHeureDebut().format(TF) : "",
                ev.getLieu() != null ? ev.getLieu() : "",
                ev.getResponsable() != null ? ev.getResponsable() : "",
                ev.getStatut()
            });
        }
    }

    private Evenement getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Sélectionnez un événement.", "Attention", JOptionPane.WARNING_MESSAGE); return null; }
        return dao.findById((String) tableModel.getValueAt(row, 0)).orElse(null);
    }

    private Evenement getSelectedQuiet() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return dao.findById((String) tableModel.getValueAt(row, 0)).orElse(null);
    }

    private void ouvrirFormulaire(Evenement existing) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                existing == null ? "Créer un événement" : "Modifier l'événement",
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(500, 460);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(15, 20, 10, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 5, 5, 5);

        JTextField fTitre  = field(existing != null ? existing.getTitre() : "");
        JComboBox<Evenement.TypeEvenement> cbType = new JComboBox<>(Evenement.TypeEvenement.values());
        JTextField fDesc   = field(existing != null ? existing.getDescription() : "");
        JTextField fDate   = field(existing != null && existing.getDate() != null ? existing.getDate().format(DF) : LocalDate.now().format(DF));
        JTextField fHDeb   = field(existing != null && existing.getHeureDebut() != null ? existing.getHeureDebut().format(TF) : "09:00");
        JTextField fHFin   = field(existing != null && existing.getHeureFin() != null ? existing.getHeureFin().format(TF) : "12:00");
        JTextField fLieu   = field(existing != null ? existing.getLieu() : "Salle principale");
        JTextField fResp   = field(existing != null ? existing.getResponsable() : "");
        JComboBox<String> cbStatut = new JComboBox<>(new String[]{"PLANIFIE", "EN_COURS", "TERMINE", "ANNULE"});

        if (existing != null) { cbType.setSelectedItem(existing.getType()); cbStatut.setSelectedItem(existing.getStatut()); }

        Object[][] rows = {
            {"Titre *", fTitre}, {"Type", cbType}, {"Description", fDesc},
            {"Date * (dd/MM/yyyy)", fDate}, {"Heure début (HH:mm)", fHDeb},
            {"Heure fin (HH:mm)", fHFin}, {"Lieu", fLieu},
            {"Responsable", fResp}, {"Statut", cbStatut}
        };
        for (int i = 0; i < rows.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0.35; form.add(new JLabel((String) rows[i][0]), g);
            g.gridx = 1; g.weightx = 0.65; form.add((Component) rows[i][1], g);
        }

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        JButton bSave = new JButton(existing == null ? "✅ Créer" : "✅ Mettre à jour");
        bSave.setBackground(new Color(0, 120, 60)); bSave.setForeground(Color.WHITE); bSave.setFocusPainted(false);
        JButton bCancel = new JButton("Annuler");
        bp.add(bSave); bp.add(bCancel);

        bSave.addActionListener(e -> {
            if (fTitre.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Le titre est obligatoire.", "Validation", JOptionPane.WARNING_MESSAGE); return;
            }
            Evenement ev = existing != null ? existing : new Evenement();
            ev.setTitre(fTitre.getText().trim());
            ev.setType((Evenement.TypeEvenement) cbType.getSelectedItem());
            ev.setDescription(fDesc.getText().trim());
            ev.setLieu(fLieu.getText().trim());
            ev.setResponsable(fResp.getText().trim());
            ev.setStatut((String) cbStatut.getSelectedItem());
            try { ev.setDate(LocalDate.parse(fDate.getText().trim(), DF)); } catch (Exception ex) { ev.setDate(LocalDate.now()); }
            try { ev.setHeureDebut(LocalTime.parse(fHDeb.getText().trim(), TF)); } catch (Exception ignored) {}
            try { ev.setHeureFin(LocalTime.parse(fHFin.getText().trim(), TF)); } catch (Exception ignored) {}

            boolean ok = existing == null ? dao.add(ev) : dao.update(ev);
            if (ok) { chargerEvenements("TOUS"); dlg.dispose(); }
            else JOptionPane.showMessageDialog(dlg, "Erreur d'enregistrement.", "Erreur", JOptionPane.ERROR_MESSAGE);
        });
        bCancel.addActionListener(e -> dlg.dispose());

        dlg.setLayout(new BorderLayout());
        dlg.add(new JScrollPane(form), BorderLayout.CENTER);
        dlg.add(bp, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void supprimer() {
        Evenement ev = getSelected(); if (ev == null) return;
        int c = JOptionPane.showConfirmDialog(this, "Supprimer \"" + ev.getTitre() + "\" ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) { dao.deleteById(ev.getId()); chargerEvenements("TOUS"); }
    }

    private void changerStatut() {
        Evenement ev = getSelected(); if (ev == null) return;
        String[] statuts = {"PLANIFIE", "EN_COURS", "TERMINE", "ANNULE"};
        String choix = (String) JOptionPane.showInputDialog(this,
                "Nouveau statut pour \"" + ev.getTitre() + "\":",
                "Changer le statut", JOptionPane.QUESTION_MESSAGE, null, statuts, ev.getStatut());
        if (choix != null) { ev.setStatut(choix); dao.update(ev); chargerEvenements("TOUS"); }
    }

    private void styleTB(JToggleButton tb) {
        tb.setFont(new Font("Arial", Font.BOLD, 12));
        tb.setFocusPainted(false);
        tb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
