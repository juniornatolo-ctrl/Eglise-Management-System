package com.eglise.model;
package com.eglise.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Sacrement implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum TypeSacrement {
        BAPTEME_EAU, BAPTEME_SAINT_ESPRIT, SAINTE_CENE,
        MARIAGE, DEDICACE_ENFANT, GUERISON, AUTRE
    }

    private String id;
    private TypeSacrement type;
    private String membreId;
    private String membreNom;
    private LocalDate date;
    private String officiant;  // Pasteur/responsable
    private String temoins;
    private String lieu;
    private String notes;
    private String conjointNom;      // Pour mariage
    private String parentNom;        // Pour dédicace enfant

    public Sacrement() {
        this.id = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.date = LocalDate.now();
    }

    public Sacrement(TypeSacrement type, String membreId, String membreNom,
                     LocalDate date, String officiant, String lieu) {
        this();
        this.type = type;
        this.membreId = membreId;
        this.membreNom = membreNom;
        this.date = date;
        this.officiant = officiant;
        this.lieu = lieu;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public TypeSacrement getType() { return type; }
    public void setType(TypeSacrement type) { this.type = type; }
    public String getMembreId() { return membreId; }
    public void setMembreId(String membreId) { this.membreId = membreId; }
    public String getMembreNom() { return membreNom; }
    public void setMembreNom(String membreNom) { this.membreNom = membreNom; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getOfficiant() { return officiant; }
    public void setOfficiant(String officiant) { this.officiant = officiant; }
    public String getTemoins() { return temoins; }
    public void setTemoins(String temoins) { this.temoins = temoins; }
    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getConjointNom() { return conjointNom; }
    public void setConjointNom(String conjointNom) { this.conjointNom = conjointNom; }
    public String getParentNom() { return parentNom; }
    public void setParentNom(String parentNom) { this.parentNom = parentNom; }

    @Override
    public String toString() {
        return "[" + type + "] " + membreNom + " - " + date;
    }
}
