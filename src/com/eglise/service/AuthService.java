package com.eglise.service;

import com.eglise.db.DatabaseManager;
import java.io.*;
import java.security.*;
import java.util.*;

/**
 * Service d'authentification avec gestion des rôles.
 */
public class AuthService {

    private static AuthService instance;
    private final String credFile;
    private Map<String, String[]> users; // username -> [passwordHash, role]
    private String currentUser;
    private String currentRole;

    public static AuthService getInstance() {
        if (instance == null) instance = new AuthService();
        return instance;
    }

    @SuppressWarnings("unchecked")
    private AuthService() {
        this.credFile = DatabaseManager.getInstance().getDbDir() + File.separator + "users.dat";
        this.users = new HashMap<>();
        loadUsers();
        createDefaultUsers();
    }

    private void createDefaultUsers() {
        if (users.isEmpty()) {
            // Compte administrateur par défaut
            users.put("admin", new String[]{hashPassword("admin123"), "ADMIN"});
            users.put("pasteur", new String[]{hashPassword("pasteur123"), "PASTEUR"});
            users.put("secretaire", new String[]{hashPassword("secret123"), "SECRETAIRE"});
            users.put("tresorier", new String[]{hashPassword("tresorier123"), "TRESORIER"});
            saveUsers();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadUsers() {
        File f = new File(credFile);
        if (f.exists() && f.length() > 0) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                users = (Map<String, String[]>) ois.readObject();
            } catch (Exception ignored) {}
        }
    }

    private void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(credFile))) {
            oos.writeObject(users);
        } catch (IOException ignored) {}
    }

    public boolean login(String username, String password) {
        String[] creds = users.get(username.toLowerCase());
        if (creds != null && creds[0].equals(hashPassword(password))) {
            this.currentUser = username;
            this.currentRole = creds[1];
            return true;
        }
        return false;
    }

    public void logout() {
        this.currentUser = null;
        this.currentRole = null;
    }

    public boolean isLoggedIn() { return currentUser != null; }
    public String getCurrentUser() { return currentUser; }
    public String getCurrentRole() { return currentRole; }

    public boolean isAdmin() { return "ADMIN".equals(currentRole); }
    public boolean isPasteur() { return "PASTEUR".equals(currentRole) || isAdmin(); }
    public boolean isSecrétaire() { return "SECRETAIRE".equals(currentRole) || isAdmin(); }
    public boolean isTrésorier() { return "TRESORIER".equals(currentRole) || isAdmin(); }

    public boolean changerMotDePasse(String username, String ancienMdp, String nouveauMdp) {
        String[] creds = users.get(username.toLowerCase());
        if (creds != null && creds[0].equals(hashPassword(ancienMdp))) {
            creds[0] = hashPassword(nouveauMdp);
            saveUsers();
            return true;
        }
        return false;
    }

    public boolean ajouterUtilisateur(String username, String password, String role) {
        if (!isAdmin()) return false;
        users.put(username.toLowerCase(), new String[]{hashPassword(password), role.toUpperCase()});
        saveUsers();
        return true;
    }

    public Set<String> getUtilisateurs() { return users.keySet(); }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            return password; // fallback
        }
    }
}
