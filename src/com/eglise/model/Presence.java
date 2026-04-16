package com.eglise.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Presence implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private LocalDate date;
    private String typeService;     // CULTE_DIMANCHE, PRIERE, CONFERENCE, etc.
    private List<String> membresPresents;  // IDs des membres
    private List<String> nomsPresents;     // Noms (pour visiteurs sans compte)
    private int visiteurs;          // Nombre de visiteurs non-membres
    private String notes;
    private String officiants;

    public Presence() {
        this.id = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.date = LocalDate.now();
        this.membresPresents = new ArrayList<>();
        this.nomsPresents = new ArrayList<>();
        this.typeService = "CULTE_DIMANCHE";
        this.visiteurs = 0;
    }

    public Presence(LocalDate date, String typeService) {
        this();
        this.date = date;
        this.typeService = typeService;
    }

    public int getTotalPresents() {
        return nomsPresents.size() + visiteurs;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getTypeService() { return typeService; }
    public void setTypeService(String typeService) { this.typeService = typeService; }
    public List<String> getMembresPresents() { return membresPresents; }
    public void setMembresPresents(List<String> membresPresents) { this.membresPresents = membresPresents; }
    public List<String> getNomsPresents() { return nomsPresents; }
    public void setNomsPresents(List<String> nomsPresents) { this.nomsPresents = nomsPresents; }
    public int getVisiteurs() { return visiteurs; }
    public void setVisiteurs(int visiteurs) { this.visiteurs = visiteurs; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getOfficiants() { return officiants; }
    public void setOfficiants(String officiants) { this.officiants = officiants; }

    @Override
    public String toString() {
        return date + " [" + typeService + "] - " + getTotalPresents() + " présents";
    }
}
