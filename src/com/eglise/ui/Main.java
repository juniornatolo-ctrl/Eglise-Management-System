package com.eglise.ui;
package com.eglise;

import com.eglise.db.DatabaseManager;
import com.eglise.ui.MainFrame;
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
