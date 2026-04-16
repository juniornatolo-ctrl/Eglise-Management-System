package com.eglise.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Canal { SMS, ANNONCE_INTERNE, EMAIL, AFFICHAGE }
    public enum Statut { BROUILLON, ENVOYE, PROGRAMME, ECHOUE }

    private String id;
    private String sujet;
    private String contenu;
    private Canal canal;
    private Statut statut;
    private String expediteur;
    private List<String> destinataires;   // noms ou numéros
    private LocalDateTime dateCreation;
    private LocalDateTime dateEnvoi;
    private String cible;   // TOUS, DEPARTEMENT:X, ROLE:X, INDIVIDUEL

    public Message() {
        this.id = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.dateCreation = LocalDateTime.now();
        this.destinataires = new ArrayList<>();
        this.statut = Statut.BROUILLON;
        this.canal = Canal.ANNONCE_INTERNE;
        this.cible = "TOUS";
    }

    public Message(String sujet, String contenu, Canal canal, String expediteur, String cible) {
        this();
        this.sujet = sujet;
        this.contenu = contenu;
        this.canal = canal;
        this.expediteur = expediteur;
        this.cible = cible;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSujet() { return sujet; }
    public void setSujet(String sujet) { this.sujet = sujet; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public Canal getCanal() { return canal; }
    public void setCanal(Canal canal) { this.canal = canal; }
    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }
    public String getExpediteur() { return expediteur; }
    public void setExpediteur(String expediteur) { this.expediteur = expediteur; }
    public List<String> getDestinataires() { return destinataires; }
    public void setDestinataires(List<String> destinataires) { this.destinataires = destinataires; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }
    public String getCible() { return cible; }
    public void setCible(String cible) { this.cible = cible; }

    @Override
    public String toString() {
        return "[" + canal + "] " + sujet + " → " + cible + " (" + statut + ")";
    }
}
