package com.eglise.ui;
import com.eglise.model.*;
import com.eglise.service.*;
import com.eglise.db.*;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Initialiser la base de données
        DatabaseManager.getInstance().initialize();

        // Lancer l'interface graphique sur l'EDT
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new MainFrame().setVisible(true);
        });
    }
}
