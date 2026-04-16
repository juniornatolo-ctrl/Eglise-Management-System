package com.eglise.model;
package com.eglise.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type { OFFRANDE, DIME, DON, CONTRIBUTION, DEPENSE, AUTRE }

    private String id;
    private Type type;
    private double montant;
    private String description;
    private LocalDate date;
    private String membreId; // peut être null pour dépenses
    private String membreNom;
    private String categorie;
    private String modePaiement; // ESPECES, MOBILE_MONEY, VIREMENT, CHEQUE

    public Transaction() {
        this.id = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.date = LocalDate.now();
        this.modePaiement = "ESPECES";
    }

    public Transaction(Type type, double montant, String description,
                       String membreId, String membreNom, String modePaiement) {
        this();
        this.type = type;
        this.montant = montant;
        this.description = description;
        this.membreId = membreId;
        this.membreNom = membreNom;
        this.modePaiement = modePaiement;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getMembreId() { return membreId; }
    public void setMembreId(String membreId) { this.membreId = membreId; }
    public String getMembreNom() { return membreNom; }
    public void setMembreNom(String membreNom) { this.membreNom = membreNom; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public String getModePaiement() { return modePaiement; }
    public void setModePaiement(String modePaiement) { this.modePaiement = modePaiement; }

    public boolean isDepense() { return type == Type.DEPENSE; }

    @Override
    public String toString() {
        return "[" + type + "] " + montant + " FCFA - " + description + " (" + date + ")";
    }
}
