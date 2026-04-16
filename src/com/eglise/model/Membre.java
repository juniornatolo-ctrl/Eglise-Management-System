package com.eglise.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Membre implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private String adresse;
    private String departement;
    private String role; // PASTEUR, SECRETAIRE, TRESORIER, RESPONSABLE, MEMBRE
    private LocalDate dateNaissance;
    private LocalDate dateAdhesion;
    private boolean actif;
    private String photoPath;

    public Membre() {
        this.id = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.dateAdhesion = LocalDate.now();
        this.actif = true;
        this.role = "MEMBRE";
    }

    public Membre(String nom, String prenom, String telephone, String email,
                  String adresse, String departement, String role, LocalDate dateNaissance) {
        this();
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.email = email;
        this.adresse = adresse;
        this.departement = departement;
        this.role = role;
        this.dateNaissance = dateNaissance;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getNomComplet() { return prenom + " " + nom; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }
    public LocalDate getDateAdhesion() { return dateAdhesion; }
    public void setDateAdhesion(LocalDate dateAdhesion) { this.dateAdhesion = dateAdhesion; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    @Override
    public String toString() {
        return getNomComplet() + " [" + role + "] - " + telephone;
    }
}
