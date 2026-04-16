package com.eglise.service;

import com.eglise.db.DatabaseManager;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.*;

/**
 * Service de sauvegarde et restauration des données.
 * Permet de sauvegarder/restaurer sur n'importe quel répertoire (USB, Google Drive, OneDrive, etc.)
 */
public class BackupService {

    private static BackupService instance;
    private final String dbDir;
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static BackupService getInstance() {
        if (instance == null) instance = new BackupService();
        return instance;
    }

    private BackupService() {
        this.dbDir = DatabaseManager.getInstance().getDbDir();
    }

    /**
     * Crée une sauvegarde ZIP dans le dossier destination.
     * @param destinationDir Le dossier de destination (Drive, USB, etc.)
     * @return Le chemin du fichier ZIP créé, ou null en cas d'erreur
     */
    public String creerSauvegarde(String destinationDir) throws IOException {
        File destDir = new File(destinationDir);
        if (!destDir.exists()) destDir.mkdirs();

        String timestamp = LocalDateTime.now().format(DT_FORMAT);
        String zipName = "EgliseApp_Backup_" + timestamp + ".zip";
        String zipPath = destinationDir + File.separator + zipName;

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath))) {
            zos.setLevel(Deflater.BEST_COMPRESSION);

            File sourceDir = new File(dbDir);
            File[] files = sourceDir.listFiles((dir, name) -> name.endsWith(".dat"));

            if (files != null) {
                for (File file : files) {
                    ZipEntry entry = new ZipEntry(file.getName());
                    zos.putNextEntry(entry);

                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            zos.write(buffer, 0, bytesRead);
                        }
                    }
                    zos.closeEntry();
                }
            }

            // Ajouter un fichier info.txt avec métadonnées
            ZipEntry infoEntry = new ZipEntry("backup_info.txt");
            zos.putNextEntry(infoEntry);
            String info = "Sauvegarde EgliseApp\n" +
                    "Date: " + LocalDateTime.now() + "\n" +
                    "Application: Église Apostolique Source de Grâce\n" +
                    "Version: 1.0\n";
            zos.write(info.getBytes("UTF-8"));
            zos.closeEntry();
        }

        System.out.println("[BACKUP] Sauvegarde créée: " + zipPath);
        return zipPath;
    }

    /**
     * Restaure les données depuis un fichier ZIP de sauvegarde.
     * @param zipFilePath Chemin du fichier ZIP à restaurer
     */
    public boolean restaurerSauvegarde(String zipFilePath) throws IOException {
        File zipFile = new File(zipFilePath);
        if (!zipFile.exists()) {
            System.err.println("[RESTORE] Fichier introuvable: " + zipFilePath);
            return false;
        }

        // Créer une sauvegarde de sécurité avant restauration
        creerSauvegarde(dbDir + File.separator + "before_restore");

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".dat")) {
                    File outFile = new File(dbDir + File.separator + entry.getName());
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = zis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                }
                zis.closeEntry();
            }
        }

        System.out.println("[RESTORE] Restauration réussie depuis: " + zipFilePath);
        return true;
    }

    /**
     * Sauvegarde automatique dans le répertoire de l'application.
     */
    public String sauvegardeAutomatique() throws IOException {
        String autoBackupDir = dbDir + File.separator + "auto_backup";
        return creerSauvegarde(autoBackupDir);
    }

    /**
     * Exporte les données en CSV pour Excel.
     */
    public String exporterCSV(String destinationDir, String[][] data, String[] headers, String filename) throws IOException {
        File destDir = new File(destinationDir);
        if (!destDir.exists()) destDir.mkdirs();

        String csvPath = destinationDir + File.separator + filename + ".csv";

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(csvPath), "UTF-8"))) {
            // BOM UTF-8 pour Excel
            pw.print('\uFEFF');
            // En-têtes
            pw.println(String.join(";", headers));
            // Données
            for (String[] row : data) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < row.length; i++) {
                    if (i > 0) sb.append(";");
                    String cell = row[i] == null ? "" : row[i].replace("\"", "\"\"");
                    if (cell.contains(";") || cell.contains("\"") || cell.contains("\n")) {
                        sb.append("\"").append(cell).append("\"");
                    } else {
                        sb.append(cell);
                    }
                }
                pw.println(sb.toString());
            }
        }

        return csvPath;
    }

    /**
     * Retourne la taille de la base de données en KB.
     */
    public long getTailleDB() {
        File dir = new File(dbDir);
        long total = 0;
        File[] files = dir.listFiles((d, n) -> n.endsWith(".dat"));
        if (files != null) {
            for (File f : files) total += f.length();
        }
        return total / 1024;
    }
}
