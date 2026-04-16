package com.eglise.ui;
import com.eglise.model.*;
import com.eglise.service.*;
import com.eglise.db.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {

    private final ReportService reportService = ReportService.getInstance();
    private final EvenementDAO evenementDAO = new EvenementDAO();
    private JPanel statsPanel;
    private JPanel eventsPanel;

    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(248, 249, 255));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        buildUI();
        refreshData();
    }

    private void buildUI() {
        // Titre
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("📊 Tableau de Bord");
        title.setFont(new Font("Georgia", Font.BOLD, 22));
        title.setForeground(new Color(13, 71, 161));
        JButton btnRefresh = new JButton("🔄 Actualiser");
        btnRefresh.addActionListener(e -> refreshData());
        btnRefresh.setFocusPainted(false);
        titlePanel.add(title, BorderLayout.WEST);
        titlePanel.add(btnRefresh, BorderLayout.EAST);

        // Cartes statistiques
        statsPanel = new JPanel(new GridLayout(2, 4, 12, 12));
        statsPanel.setOpaque(false);

        // Événements à venir
        eventsPanel = new JPanel(new BorderLayout());
        eventsPanel.setBackground(Color.WHITE);
        eventsPanel.setBorder(createCardBorder("📅 Prochains Événements"));

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);
        content.add(titlePanel, BorderLayout.NORTH);
        content.add(statsPanel, BorderLayout.CENTER);
        content.add(eventsPanel, BorderLayout.SOUTH);

        add(content, BorderLayout.CENTER);
    }

    private void refreshData() {
        Map<String, Object> stats = reportService.getStatsDashboard();

        statsPanel.removeAll();
        int[] membres = {(int)(Integer) stats.get("totalMembres")};
        addCard(statsPanel, "👥 Membres", String.valueOf(stats.get("totalMembres")), new Color(13, 71, 161));
        addCard(statsPanel, "📅 Événements", String.valueOf(stats.get("evenementsAVenir")), new Color(0, 120, 80));
        addCard(statsPanel, "💵 Solde Caisse", formatMontant((double) stats.get("soldeCaisse")), new Color(30, 90, 30));
        addCard(statsPanel, "📥 Entrées Totales", formatMontant((double) stats.get("entreesTotal")), new Color(0, 100, 120));
        addCard(statsPanel, "📤 Dépenses Totales", formatMontant((double) stats.get("depensesTotal")), new Color(160, 50, 0));
        addCard(statsPanel, "📥 Entrées ce mois", formatMontant((double) stats.get("entreeMois")), new Color(60, 90, 160));
        addCard(statsPanel, "✝️ Sacrements", String.valueOf(stats.get("totalSacrements")), new Color(100, 30, 140));
        addCard(statsPanel, "📋 Feuilles Présence", String.valueOf(stats.get("totalPresences")), new Color(0, 100, 100));

        statsPanel.revalidate();
        statsPanel.repaint();

        // Événements à venir
        eventsPanel.removeAll();
        eventsPanel.setBorder(createCardBorder("📅 Prochains Événements"));
        List<Evenement> events = evenementDAO.findAVenir();
        if (events.isEmpty()) {
            JLabel lbl = new JLabel("  Aucun événement planifié.", JLabel.LEFT);
            lbl.setFont(new Font("Arial", Font.ITALIC, 13));
            lbl.setForeground(Color.GRAY);
            eventsPanel.add(lbl, BorderLayout.CENTER);
        } else {
            JPanel listPanel = new JPanel(new GridLayout(Math.min(events.size(), 5), 1, 0, 5));
            listPanel.setOpaque(false);
            listPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (int i = 0; i < Math.min(events.size(), 5); i++) {
                Evenement ev = events.get(i);
                JLabel lbl = new JLabel("• " + ev.getTitre() + " — " + ev.getDate().format(df) + " @ " + ev.getLieu());
                lbl.setFont(new Font("Arial", Font.PLAIN, 12));
                listPanel.add(lbl);
            }
            eventsPanel.add(listPanel, BorderLayout.CENTER);
        }
        eventsPanel.revalidate();
        eventsPanel.repaint();
    }

    private void addCard(JPanel parent, String title, String value, Color color) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(color);
        card.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                new EmptyBorder(15, 12, 15, 12)));

        JLabel lblVal = new JLabel(value, JLabel.CENTER);
        lblVal.setFont(new Font("Georgia", Font.BOLD, 22));
        lblVal.setForeground(Color.WHITE);

        JLabel lblTitle = new JLabel("<html><center>" + title + "</center></html>", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 11));
        lblTitle.setForeground(new Color(220, 235, 255));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(lblVal, gbc);
        gbc.gridy = 1;
        card.add(lblTitle, gbc);

        parent.add(card);
    }

    private Border createCardBorder(String title) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 210, 230)),
                        title, 0, 0,
                        new Font("Arial", Font.BOLD, 13),
                        new Color(13, 71, 161)),
                new EmptyBorder(5, 5, 5, 5));
    }

    private String formatMontant(double v) {
        return String.format("%,.0f FCFA", v);
    }
}
