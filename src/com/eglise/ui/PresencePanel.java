package com.eglise.ui;

import com.eglise.dao.PresenceDAO;
import com.eglise.model.Presence;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PresencePanel extends JPanel {

    private final PresenceDAO dao = new PresenceDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private GraphiquePresence graphique;
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] TYPES_SERVICE = {
        "CULTE_DIMANCHE", "PRIERE_MERCREDI", "PRIERE_VENDREDI",
        "CONFERENCE", "JEUNESSE", "FEMMES", "EVANGELISATION", "AUTRE"
    };

    public PresencePanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(new Color(248, 249, 255));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        buildUI();
        charger();
    }

    private void buildUI() {
        // ─── TITRE & BOUTONS ───
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("📋 Feuilles de Présence");
        title.setFont(new Font("Georgia", Font.BOLD, 20));
        title.setForeground(new Color(13, 71, 161));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnPanel.setOpaque(false);
        JButton btnAdd  = btn("➕ Nouvelle feuille", new Color(0, 120, 60));
        JButton btnEdit = btn("✏️ Modifier", new Color(13, 71, 161));
        JButton btnDel  = btn("🗑️ Supprimer", new Color(180, 30, 30));
        JButton btnRef  = btn("🔄 Actualiser", new Color(80, 80, 80));

        btnAdd.addActionListener(e -> ouvrirFormulaire(null));
        btnEdit.addActionListener(e -> { Presence p = getSelected(); if (p != null) ouvrirFormulaire(p); });
        btnDel.addActionListener(e -> supprimer());
        btnRef.addActionListener(e -> charger());

        btnPanel.add(btnAdd); btnPanel.add(btnEdit); btnPanel.add(btnDel); btnPanel.add(btnRef);
        topBar.add(title, BorderLayout.WEST);
        topBar.add(btnPanel, BorderLayout.EAST);

        // ─── TABLE ───
        String[] cols = {"ID", "Date", "Type de Service", "Présents", "Visiteurs", "Total", "Officiant", "Notes"};
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
                if (!sel) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 248, 255));
                if (col == 5 && !sel) { c.setFont(new Font("Arial", Font.BOLD, 12)); }
                return c;
            }
        });
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        table.getColumnModel().getColumn(3).setMaxWidth(80);
        table.getColumnModel().getColumn(4).setMaxWidth(80);
        table.getColumnModel().getColumn(5).setMaxWidth(60);

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { Presence p = getSelected(); if (p != null) ouvrirFormulaire(p); }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));

        // ─── GRAPHIQUE TENDANCE ───
        graphique = new GraphiquePresence();
        graphique.setPreferredSize(new Dimension(0, 160));
        graphique.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 215, 235)),
                "📈 Tendance des présences (12 derniers services)",
                0, 0, new Font("Arial", Font.BOLD, 12), new Color(13, 71, 161)));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll, graphique);
        split.setDividerLocation(380);
        split.setDividerSize(4);
        split.setBorder(null);

        add(topBar, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
    }

    private void charger() {
        List<Presence> list = dao.findRecentes(200);
        tableModel.setRowCount(0);
        for (Presence p : list) {
            int membres = p.getNomsPresents().size();
            tableModel.addRow(new Object[]{
                p.getId(),
                p.getDate() != null ? p.getDate().format(DF) : "",
                p.getTypeService(),
                membres,
                p.getVisiteurs(),
                p.getTotalPresents(),
                p.getOfficiants() != null ? p.getOfficiants() : "",
                p.getNotes() != null ? p.getNotes() : ""
            });
        }
        // Mettre à jour le graphique
        List<Presence> last12 = dao.findRecentes(12);
        graphique.setData(last12);
        graphique.repaint();
    }

    private Presence getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Sélectionnez une feuille.", "Attention", JOptionPane.WARNING_MESSAGE); return null; }
        return dao.findById((String) tableModel.getValueAt(row, 0)).orElse(null);
    }

    private void ouvrirFormulaire(Presence existing) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                existing == null ? "Nouvelle feuille de présence" : "Modifier la feuille",
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(520, 500);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(15, 20, 10, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(5, 5, 5, 5);

        JComboBox<String> cbType = new JComboBox<>(TYPES_SERVICE);
        if (existing != null) cbType.setSelectedItem(existing.getTypeService());
        JTextField fDate  = field(existing != null && existing.getDate() != null ? existing.getDate().format(DF) : LocalDate.now().format(DF));
        JTextField fOff   = field(existing != null ? existing.getOfficiants() : "");

        // Zone de saisie des noms (un par ligne)
        JTextArea fNoms   = new JTextArea(existing != null ? String.join("\n", existing.getNomsPresents()) : "", 8, 25);
        fNoms.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        fNoms.setFont(new Font("Arial", Font.PLAIN, 12));

        SpinnerNumberModel spinModel = new SpinnerNumberModel(
                existing != null ? existing.getVisiteurs() : 0, 0, 9999, 1);
        JSpinner spinVisiteurs = new JSpinner(spinModel);

        JTextArea fNotes = new JTextArea(existing != null && existing.getNotes() != null ? existing.getNotes() : "", 2, 25);
        fNotes.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        Object[][] rows = {
            {"Type de service *", cbType},
            {"Date * (dd/MM/yyyy)", fDate},
            {"Officiant / Prédicateur", fOff},
        };
        for (int i = 0; i < rows.length; i++) {
            g.gridx=0; g.gridy=i; g.weightx=0.35; form.add(new JLabel((String) rows[i][0]), g);
            g.gridx=1; g.weightx=0.65; form.add((Component) rows[i][1], g);
        }

        g.gridx=0; g.gridy=3; form.add(new JLabel("Noms présents (1 par ligne):"), g);
        g.gridx=1; form.add(new JScrollPane(fNoms), g);

        g.gridx=0; g.gridy=4; form.add(new JLabel("Visiteurs non-membres:"), g);
        g.gridx=1; form.add(spinVisiteurs, g);

        g.gridx=0; g.gridy=5; form.add(new JLabel("Notes:"), g);
        g.gridx=1; form.add(new JScrollPane(fNotes), g);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        JButton bSave = new JButton(existing == null ? "✅ Enregistrer" : "✅ Mettre à jour");
        bSave.setBackground(new Color(0, 120, 60)); bSave.setForeground(Color.WHITE); bSave.setFocusPainted(false);
        JButton bCancel = new JButton("Annuler");
        bp.add(bSave); bp.add(bCancel);

        bSave.addActionListener(e -> {
            Presence p = existing != null ? existing : new Presence();
            p.setTypeService((String) cbType.getSelectedItem());
            p.setOfficiants(fOff.getText().trim());
            p.setVisiteurs((Integer) spinVisiteurs.getValue());
            p.setNotes(fNotes.getText().trim());
            try { p.setDate(LocalDate.parse(fDate.getText().trim(), DF)); } catch (Exception ex) { p.setDate(LocalDate.now()); }

            // Parser les noms
            java.util.ArrayList<String> noms = new java.util.ArrayList<>();
            for (String ligne : fNoms.getText().split("\n")) {
                String nom = ligne.trim();
                if (!nom.isEmpty()) noms.add(nom);
            }
            p.setNomsPresents(noms);

            boolean ok = existing == null ? dao.add(p) : dao.update(p);
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
        Presence p = getSelected(); if (p == null) return;
        int c = JOptionPane.showConfirmDialog(this,
                "Supprimer la feuille du " + p.getDate().format(DF) + " ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) { dao.deleteById(p.getId()); charger(); }
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

    // ─── Graphique interne ───
    static class GraphiquePresence extends JPanel {
        private List<Presence> data;

        public void setData(List<Presence> data) {
            this.data = data;
            java.util.Collections.reverse(this.data);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());

            if (data == null || data.isEmpty()) {
                g.setColor(Color.GRAY);
                g.setFont(new Font("Arial", Font.ITALIC, 12));
                g.drawString("Aucune donnée de présence disponible.", 20, getHeight() / 2);
                return;
            }

            int padLeft = 50, padRight = 20, padTop = 25, padBottom = 35;
            int w = getWidth() - padLeft - padRight;
            int h = getHeight() - padTop - padBottom;

            int maxVal = data.stream().mapToInt(Presence::getTotalPresents).max().orElse(1);
            if (maxVal == 0) maxVal = 1;

            // Axe Y
            g.setColor(new Color(200, 210, 230));
            for (int i = 0; i <= 4; i++) {
                int y = padTop + h - (i * h / 4);
                g.drawLine(padLeft, y, padLeft + w, y);
                g.setColor(Color.GRAY);
                g.setFont(new Font("Arial", Font.PLAIN, 10));
                g.drawString(String.valueOf(maxVal * i / 4), 5, y + 4);
                g.setColor(new Color(200, 210, 230));
            }

            // Barres + courbe
            int n = data.size();
            int barW = Math.max(8, w / (n + 1));
            int[] xpts = new int[n], ypts = new int[n];

            for (int i = 0; i < n; i++) {
                int val = data.get(i).getTotalPresents();
                int barH = (int) ((double) val / maxVal * h);
                int x = padLeft + (i + 1) * w / (n + 1) - barW / 2;
                int y = padTop + h - barH;

                // Barre dégradée
                GradientPaint gp = new GradientPaint(x, y, new Color(66, 133, 244, 200), x, padTop + h, new Color(30, 80, 180, 80));
                g.setPaint(gp);
                g.fillRoundRect(x, y, barW, barH, 4, 4);
                g.setColor(new Color(30, 80, 180));
                g.drawRoundRect(x, y, barW, barH, 4, 4);

                // Valeur au-dessus
                g.setColor(new Color(13, 71, 161));
                g.setFont(new Font("Arial", Font.BOLD, 10));
                g.drawString(String.valueOf(val), x + barW/2 - 7, y - 4);

                xpts[i] = x + barW / 2;
                ypts[i] = y;

                // Étiquette date
                DateTimeFormatter dfShort = DateTimeFormatter.ofPattern("dd/MM");
                g.setColor(Color.DARK_GRAY);
                g.setFont(new Font("Arial", Font.PLAIN, 9));
                String dateStr = data.get(i).getDate() != null ? data.get(i).getDate().format(dfShort) : "";
                g.drawString(dateStr, x + barW/2 - 12, padTop + h + 15);
            }

            // Courbe de tendance
            if (n > 1) {
                g.setColor(new Color(255, 100, 50, 200));
                g.setStroke(new BasicStroke(2.5f));
                for (int i = 0; i < n - 1; i++) {
                    g.drawLine(xpts[i], ypts[i], xpts[i+1], ypts[i+1]);
                }
                // Points
                for (int i = 0; i < n; i++) {
                    g.setColor(new Color(255, 100, 50));
                    g.fillOval(xpts[i] - 4, ypts[i] - 4, 8, 8);
                }
            }
        }
    }
}
