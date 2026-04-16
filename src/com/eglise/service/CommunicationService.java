package com.eglise.service;

import com.eglise.dao.MessageDAO;
import com.eglise.dao.MembreDAO;
import com.eglise.model.Membre;
import com.eglise.model.Message;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de communication — annonces internes, SMS simulé.
 * Pour un vrai SMS: intégrer l'API Orange SMS ou Twilio.
 */
public class CommunicationService {

    private static CommunicationService instance;
    private final MessageDAO messageDAO = new MessageDAO();
    private final MembreDAO membreDAO = new MembreDAO();
    private final List<String> smsLog = new ArrayList<>();

    public static CommunicationService getInstance() {
        if (instance == null) instance = new CommunicationService();
        return instance;
    }

    /**
     * Envoie (simule) un SMS à tous les membres ou un groupe.
     * En production: remplacer par appel API SMS (Orange, Twilio, etc.)
     */
    public Message envoyerAnnonce(String sujet, String contenu, String cible,
                                   Message.Canal canal, String expediteur) {
        Message msg = new Message(sujet, contenu, canal, expediteur, cible);

        // Résoudre les destinataires
        List<String> destinataires = resoudreDestinataires(cible);
        msg.setDestinataires(destinataires);

        // Simuler l'envoi SMS
        if (canal == Message.Canal.SMS) {
            for (String dest : destinataires) {
                String log = "[SMS] → " + dest + " : " + contenu;
                smsLog.add(log);
                System.out.println(log);
            }
        }

        msg.setStatut(Message.Statut.ENVOYE);
        msg.setDateEnvoi(LocalDateTime.now());
        messageDAO.add(msg);

        return msg;
    }

    /**
     * Sauvegarder comme brouillon.
     */
    public Message sauvegarderBrouillon(String sujet, String contenu,
                                         String cible, Message.Canal canal, String expediteur) {
        Message msg = new Message(sujet, contenu, canal, expediteur, cible);
        msg.setStatut(Message.Statut.BROUILLON);
        messageDAO.add(msg);
        return msg;
    }

    /**
     * Résout la liste de destinataires selon la cible.
     */
    private List<String> resoudreDestinataires(String cible) {
        if ("TOUS".equals(cible)) {
            return membreDAO.findActifs().stream()
                    .map(m -> m.getNomComplet() + " (" + m.getTelephone() + ")")
                    .collect(Collectors.toList());
        }
        if (cible != null && cible.startsWith("DEPARTEMENT:")) {
            String dept = cible.substring(12);
            return membreDAO.findByDepartement(dept).stream()
                    .map(m -> m.getNomComplet() + " (" + m.getTelephone() + ")")
                    .collect(Collectors.toList());
        }
        if (cible != null && cible.startsWith("ROLE:")) {
            String role = cible.substring(5);
            return membreDAO.findByRole(role).stream()
                    .map(m -> m.getNomComplet() + " (" + m.getTelephone() + ")")
                    .collect(Collectors.toList());
        }
        return Collections.singletonList(cible);
    }

    /**
     * Nombre total d'envois SMS simulés.
     */
    public int getNbSMSEnvoyes() { return smsLog.size(); }

    /**
     * Journal SMS (pour affichage dans l'UI).
     */
    public List<String> getSmsLog() { return Collections.unmodifiableList(smsLog); }

    /**
     * Génère un modèle de message pour les annonces courantes.
     */
    public String getTemplate(String type) {
        switch (type) {
            case "RAPPEL_CULTE":
                return "Chers frères et sœurs,\n\nNous vous rappelons que le culte de ce dimanche aura lieu comme d'habitude.\nSoyez les bienvenus !\n\nL'Église Apostolique « Source de Grâce »";
            case "CONVOCATION":
                return "Chers membres,\n\nVous êtes cordialement invités à [NOM_EVENEMENT] le [DATE] à [HEURE] au [LIEU].\n\nMerci de confirmer votre présence.\n\nLa Direction";
            case "ANNONCE_BAPTEME":
                return "Gloire à Dieu !\n\nNous avons le plaisir de vous annoncer que [NOM] sera baptisé(e) le [DATE].\n\nVenez nombreux partager ce moment de joie !\n\nL'Église";
            case "RAPPEL_DIME":
                return "Chers fidèles,\n\nNous vous rappelons l'importance de la dîme dans la vie de l'Église.\n\"Apportez toutes les dîmes dans la maison du trésor\" (Mal 3:10)\n\nMerci pour votre fidélité.";
            case "PRIERE_URGENCE":
                return "APPEL À LA PRIÈRE\n\nFrères et sœurs, nous avons besoin de vos prières urgentes pour [SITUATION].\n\nSoyons unis dans la prière !\n\nLe Pasteur";
            default:
                return "";
        }
    }

    public int resoudreDestinatairesCount(String cible) { return resoudreDestinataires(cible).size(); }

    public MessageDAO getMessageDAO() { return messageDAO; }
}
// NOTE: méthode ajoutée pour CommunicationPanel
