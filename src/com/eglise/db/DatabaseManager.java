package com.eglise.db;
package com.eglise.db;

import java.io.*;
import java.nio.file.*;
import java.sql.*;

/**
 * Gestionnaire de base de données SQLite embarquée.
 * Utilise le driver JDBC standard de Java (Derby embarqué ou fichier plat).
 * Pour production: ajouter sqlite-jdbc.jar dans le classpath.
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private static final String DB_DIR = System.getProperty("user.home") + File.separator + "EgliseApp";
    private static final String DB_FILE = DB_DIR + File.separator + "eglise.db";

    // Simulation de connexion via fichiers binaires sérialisés
    // En production réelle: Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
    private boolean initialized = false;

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    public void initialize() {
        File dir = new File(DB_DIR);
        if (!dir.exists()) dir.mkdirs();

        // Créer les fichiers de données si absents
        String[] tables = {"membres", "finances", "evenements", "sacre ments", "communications", "utilisateurs"};
        for (String table : tables) {
            File f = new File(DB_DIR + File.separator + table + ".dat");
            if (!f.exists()) {
                try { f.createNewFile(); } catch (IOException ignored) {}
            }
        }
        initialized = true;
        System.out.println("[DB] Base de données initialisée dans: " + DB_DIR);
    }

    public String getDbDir() { return DB_DIR; }
    public boolean isInitialized() { return initialized; }
}
