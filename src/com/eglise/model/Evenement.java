package com.eglise.model;
package com.eglise.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Evenement implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum TypeEvenement {
        CULTE, PRIERE, CONFERENCE, BAPTEME, MARIAGE, EVANGELISATION, FORMATION, AUTRE
    }

    private String id;
    private String titre;
    private TypeEvenement type;
    private String description;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String lieu;
    private String responsable;
    private List<String> participants;
    private boolean recurrent;
    private String frequence; // HEBDOMADAIRE, MENSUEL, etc.
    private String statut; // PLANIFIE, EN_COURS, TERMINE, ANNULE

    public Evenement() {
        this.id = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.participants = new ArrayList<>();
        this.statut = "PLANIFIE";
        this.date = LocalDate.now();
    }

    public Evenement(String titre, TypeEvenement type, String description,
                     LocalDate date, LocalTime heureDebut, LocalTime heureFin,
                     String lieu, String responsable) {
        this();
        this.titre = titre;
        this.type = type;
        this.description = description;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.lieu = lieu;
        this.responsable = responsable;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public TypeEvenement getType() { return type; }
    public void setType(TypeEvenement type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }
    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }
    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
    public boolean isRecurrent() { return recurrent; }
    public void setRecurrent(boolean recurrent) { this.recurrent = recurrent; }
    public String getFrequence() { return frequence; }
    public void setFrequence(String frequence) { this.frequence = frequence; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    @Override
    public String toString() {
        return titre + " - " + date + " " + heureDebut + " @ " + lieu;
    }
}
