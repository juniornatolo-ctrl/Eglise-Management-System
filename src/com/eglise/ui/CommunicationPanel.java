package com.eglise.ui;

import com.eglise.dao.MessageDAO;
import com.eglise.model.Message;
import com.eglise.service.AuthService;
import com.eglise.service.CommunicationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CommunicationPanel extends JPanel {

    private final CommunicationService service = CommunicationService.getInstance();
    private final MessageDAO dao;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextArea aireComposition;
    private JTextField txtSujet;
    private JComboBox<Message.Canal> cbCanal;
    private JComboBox<String> cbCible;
    private JTextArea logSMS;

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public CommunicationPanel() {
        dao = service.getMessageDAO();
        setLayout(new BorderLayout(8, 8));
        setBackground(new Color(248, 249, 255));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        buildUI();
        charger();
    }

    private void buildUI() {
        JLabel title = new JLabel("📢 Communication & Annonces");
        title.setFont(new Font("Georgia", Font.BOLD, 20));
        title.setForeground(new Color(13, 71, 161));
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        // ─── PANNEAU GAUCHE : Composition ───
        JPanel compPanel = new JPanel(new BorderLayout(0, 8));
        compPanel.setBackground(Color.WHITE);
        compPanel.setPreferredSize(new Dimension(400, 0));
        compPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 215, 235)),
                "✏️ Composer un message", 0, 0,
                new Font("Arial", Font.BOLD, 12), new Color(13, 71, 161)));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        fields.setBorder(new EmptyBorder(8, 8, 0, 8));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(4, 4, 4, 4);

        txtSujet = field("");
        cbCanal  = new JComboBox<>(Message.Canal.values());
        cbCible  = new JComboBox<>(new String[]{
            "TOUS", "ROLE:PASTEUR", "ROLE:RESPONSABLE", "ROLE:MEMBRE",
            "DEPARTEMENT:Jeunesse", "DEPARTEMENT:Femmes", "DEPARTEMENT:Hommes",
            "DEPARTEMENT:Culte & Louange", "DEPARTEMENT:Évangélisation"
        });

        g.gridx=0; g.gridy=0; g.weightx=0.3; fields.add(new JLabel("Sujet:"), g);
        g.gridx=1; g.weightx=0.7; fields.add(txtSujet, g);
        g.gridx=0; g.gridy=1; g.weightx=0.3; fields.add(new JLabel("Canal:"), g);
        g.gridx=1; g.weightx=0.7; fields.add(cbCanal, g);
        g.gridx=0; g.gridy=2; g.weightx=0.3; fields.add(new JLabel("Destinataires:"), g);
        g.gridx=1; g.weightx=0.7; fields.add(cbCible, g);

        aireComposition = new JTextArea(10, 30);
        aireComposition.setFont(new Font("Arial", Font.PLAIN, 13));
        aireComposition.setLineWrap(true); aireComposition.setWrapStyleWord(true);
        aireComposition.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Compteur caractères (pour SMS)
        JLabel lblCount = new JLabel("0 caractères");
        lblCount.setFont(new Font("Arial", Font.ITALIC, 11));
        lblCount.setForeground(Color.GRAY);
        aireComposition.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void update() {
                int n = aireComposition.getText().length();
                lblCount.setText(n + " caractères" + (n > 160 ? " ⚠️ > 160 (multi-SMS)" : ""));
                lblCount.setForeground(n > 160 ? new Color(180, 80, 0) : Color.GRAY);
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        // Templates
        JPanel tmplPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        tmplPanel.setBackground(Color.WHITE);
        tmplPanel.add(new JLabel("Modèle: "));
        String[] templates = {"─ Choisir ─", "RAPPEL_CULTE", "CONVOCATION", "ANNONCE_BAPTEME", "RAPPEL_DIME", "PRIERE_URGENCE"};
        JComboBox<String> cbTemplate = new JComboBox<>(templates);
        cbTemplate.addActionListener(e -> {
            String t = (String) cbTemplate.getSelectedItem();
            if (t != null && !t.startsWith("─")) {
                aireComposition.setText(service.getTemplate(t));
                cbTemplate.setSelectedIndex(0);
            }
        });
        tmplPanel.add(cbTemplate);

        // Boutons d'action
        JPanel actionBtns = new JPanel(new GridLayout(1, 3, 6, 0));
        actionBtns.setBackground(Color.WHITE);
        actionBtns.setBorder(new EmptyBorder(8, 8, 8, 8));
        JButton btnEnvoyer  = bigBtn("📤 Envoyer", new Color(13, 71, 161));
        JButton btnBrouillon= bigBtn("💾 Brouillon", new Color(100, 100, 100));
        JButton btnEffacer  = bigBtn("🗑️ Effacer", new Color(180, 30, 30));
        btnEnvoyer.addActionListener(e -> envoyer());
        btnBrouillon.addActionListener(e -> sauverBrouillon());
        btnEffacer.addActionListener(e -> { aireComposition.setText(""); txtSujet.setText(""); });
        actionBtns.add(btnEnvoyer); actionBtns.add(btnBrouillon); actionBtns.add(btnEffacer);

        compPanel.add(fields, BorderLayout.NORTH);
        compPanel.add(tmplPanel, BorderLayout.CENTER);
        JPanel editZone = new JPanel(new BorderLayout());
        editZone.setBackground(Color.WHITE);
        editZone.add(new JScrollPane(aireComposition), BorderLayout.CENTER);
        editZone.add(lblCount, BorderLayout.SOUTH);
        // Re-add with better layout
        compPanel.setLayout(new BorderLayout(0, 4));
        JPanel topComp = new JPanel(new BorderLayout(0, 4));
        topComp.setBackground(Color.WHITE);
        topComp.add(fields, BorderLayout.NORTH);
        topComp.add(tmplPanel, BorderLayout.SOUTH);
        compPanel.add(topComp, BorderLayout.NORTH);
        compPanel.add(editZone, BorderLayout.CENTER);
        compPanel.add(actionBtns, BorderLayout.SOUTH);

        // ─── PANNEAU DROITE : Historique + Log SMS ───
        JPanel rightPanel = new JPanel(new BorderLayout(0, 8));
        rightPanel.setOpaque(false);

        // Historique messages
        JPanel histSection = new JPanel(new BorderLayout());
        histSection.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 215, 235)),
                "📬 Historique des messages", 0, 0,
                new Font("Arial", Font.BOLD, 12), new Color(13, 71, 161)));
        histSection.setBackground(Color.WHITE);

        String[] cols = {"Date", "Canal", "Sujet", "Cible", "Statut"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setFont(new Font("Arial", Font.PLAIN, 11));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.getTableHeader().setBackground(new Color(13, 71, 161));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setShowGrid(false);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                if (!sel) {
                    String statut = (String) tableModel.getValueAt(r, 4);
                    if ("ENVOYE".equals(statut)) comp.setBackground(new Color(240, 255, 240));
                    else comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 248, 255));
                }
                return comp;
            }
        });

        JPanel histBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        histBtns.setBackground(Color.WHITE);
        JButton btnSupp = new JButton("🗑️ Supprimer");
        btnSupp.addActionListener(e -> supprimerMessage());
        JButton btnRef  = new JButton("🔄 Actualiser");
        btnRef.addActionListener(e -> charger());
        histBtns.add(btnSupp); histBtns.add(btnRef);

        histSection.add(new JScrollPane(table), BorderLayout.CENTER);
        histSection.add(histBtns, BorderLayout.SOUTH);

        // Log SMS simulé
        JPanel smsSection = new JPanel(new BorderLayout());
        smsSection.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 215, 235)),
                "📱 Journal SMS simulé", 0, 0,
                new Font("Arial", Font.BOLD, 12), new Color(13, 71, 161)));
        logSMS = new JTextArea(5, 30);
        logSMS.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logSMS.setEditable(false);
        logSMS.setBackground(new Color(20, 25, 35));
        logSMS.setForeground(new Color(100, 255, 100));
        smsSection.add(new JScrollPane(logSMS));

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, histSection, smsSection);
        rightSplit.setDividerLocation(300); rightSplit.setDividerSize(4); rightSplit.setBorder(null);
        rightPanel.add(rightSplit, BorderLayout.CENTER);

        // ─── ASSEMBLAGE ───
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, compPanel, rightPanel);
        mainSplit.setDividerLocation(400); mainSplit.setDividerSize(5); mainSplit.setBorder(null);

        add(title, BorderLayout.NORTH);
        add(mainSplit, BorderLayout.CENTER);
    }

    private void charger() {
        tableModel.setRowCount(0);
        List<Message> msgs = dao.findAll();
        msgs.sort((a, b) -> b.getDateCreation().compareTo(a.getDateCreation()));
        for (Message m : msgs) {
            tableModel.addRow(new Object[]{
                m.getDateCreation() != null ? m.getDateCreation().format(DT) : "",
                m.getCanal() != null ? m.getCanal().name() : "",
                m.getSujet() != null ? m.getSujet() : "",
                m.getCible() != null ? m.getCible() : "",
                m.getStatut() != null ? m.getStatut().name() : ""
            });
        }
        // Mettre à jour le log SMS
        logSMS.setText("");
        for (String log : service.getSmsLog()) logSMS.append(log + "\n");
    }

    private void envoyer() {
        String sujet = txtSujet.getText().trim();
        String contenu = aireComposition.getText().trim();
        if (sujet.isEmpty() || contenu.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sujet et message sont obligatoires.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Message.Canal canal = (Message.Canal) cbCanal.getSelectedItem();
        String cible = (String) cbCible.getSelectedItem();
        String exp = AuthService.getInstance().getCurrentUser();

        int n = service.resoudreDestinatairesCount(cible);
        int ok = JOptionPane.showConfirmDialog(this,
                "Envoyer ce message à " + n + " destinataire(s) via " + canal + " ?\n\nSujet: " + sujet,
                "Confirmation d'envoi", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        service.envoyerAnnonce(sujet, contenu, cible, canal, exp);
        charger();
        aireComposition.setText(""); txtSujet.setText("");
        JOptionPane.showMessageDialog(this, "✅ Message envoyé avec succès à " + n + " destinataire(s) !");
    }

    private void sauverBrouillon() {
        String sujet = txtSujet.getText().trim();
        String contenu = aireComposition.getText().trim();
        if (sujet.isEmpty()) { JOptionPane.showMessageDialog(this, "Sujet obligatoire.", "Validation", JOptionPane.WARNING_MESSAGE); return; }
        service.sauvegarderBrouillon(sujet, contenu, (String) cbCible.getSelectedItem(),
                (Message.Canal) cbCanal.getSelectedItem(), AuthService.getInstance().getCurrentUser());
        charger();
        JOptionPane.showMessageDialog(this, "Brouillon sauvegardé.");
    }

    private void supprimerMessage() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Sélectionnez un message.", "Attention", JOptionPane.WARNING_MESSAGE); return; }
        List<Message> msgs = dao.findAll();
        msgs.sort((a, b) -> b.getDateCreation().compareTo(a.getDateCreation()));
        Message m = msgs.get(row);
        int c = JOptionPane.showConfirmDialog(this, "Supprimer ce message ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) { dao.deleteById(m.getId()); charger(); }
    }

    private JButton bigBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setFont(new Font("Arial", Font.BOLD, 13));
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
