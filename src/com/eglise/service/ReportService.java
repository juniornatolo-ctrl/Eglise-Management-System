package com.eglise.service;
package com.eglise.service;

import com.eglise.dao.*;
import com.eglise.dao.SacrementDAO;
import com.eglise.dao.PresenceDAO;
import com.eglise.model.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReportService {

    private static ReportService instance;
    private final MembreDAO membreDAO = new MembreDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final EvenementDAO evenementDAO = new EvenementDAO();

    public static ReportService getInstance() {
        if (instance == null) instance = new ReportService();
        return instance;
    }

    /**
     * Rapport financier mensuel en texte formaté.
     */
    public String rapportFinancierMensuel(int mois, int annee) {
        String[] moisNoms = {"", "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        StringBuilder sb = new StringBuilder();
        String sep = "═".repeat(60);

        sb.append(sep).append("\n");
        sb.append("  RAPPORT FINANCIER - ").append(moisNoms[mois]).append(" ").append(annee).append("\n");
        sb.append("  Église Apostolique « Source de Grâce »\n");
        sb.append(sep).append("\n\n");

        List<Transaction> transactions = transactionDAO.findDuMois(mois, annee);
        double entrees = transactions.stream().filter(t -> !t.isDepense()).mapToDouble(Transaction::getMontant).sum();
        double depenses = transactions.stream().filter(Transaction::isDepense).mapToDouble(Transaction::getMontant).sum();
        double solde = entrees - depenses;

        sb.append(String.format("  %-30s %,15.0f FCFA%n", "TOTAL ENTRÉES:", entrees));
        sb.append(String.format("  %-30s %,15.0f FCFA%n", "TOTAL DÉPENSES:", depenses));
        sb.append("  ").append("─".repeat(50)).append("\n");
        sb.append(String.format("  %-30s %,15.0f FCFA%n", "SOLDE DU MOIS:", solde));
        sb.append("\n");

        sb.append("  DÉTAIL DES TRANSACTIONS:\n");
        sb.append("  ").append("─".repeat(58)).append("\n");
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Transaction t : transactions) {
            String signe = t.isDepense() ? "-" : "+";
            sb.append(String.format("  [%s] %-12s %-25s %s%,.0f FCFA%n",
                    t.getDate().format(df),
                    t.getType(),
                    t.getDescription() != null ? t.getDescription() : "",
                    signe,
                    t.getMontant()));
        }

        sb.append("\n").append(sep).append("\n");
        sb.append("  Généré le: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        sb.append(sep).append("\n");

        return sb.toString();
    }

    /**
     * Rapport des membres.
     */
    public String rapportMembres() {
        StringBuilder sb = new StringBuilder();
        String sep = "═".repeat(60);
        List<Membre> membres = membreDAO.findActifs();

        sb.append(sep).append("\n");
        sb.append("  RAPPORT DES MEMBRES\n");
        sb.append("  Église Apostolique « Source de Grâce »\n");
        sb.append(sep).append("\n\n");
        sb.append("  Total membres actifs: ").append(membres.size()).append("\n\n");

        Map<String, Long> parRole = membreDAO.compterParRole();
        sb.append("  PAR RÔLE:\n");
        parRole.forEach((r, n) -> sb.append(String.format("    %-20s %d%n", r + ":", n)));
        sb.append("\n");

        Map<String, Long> parDept = membreDAO.compterParDepartement();
        if (!parDept.isEmpty()) {
            sb.append("  PAR DÉPARTEMENT:\n");
            parDept.forEach((d, n) -> sb.append(String.format("    %-20s %d%n", d + ":", n)));
            sb.append("\n");
        }

        sb.append("  LISTE COMPLÈTE:\n");
        sb.append("  ").append("─".repeat(58)).append("\n");
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Membre m : membres) {
            sb.append(String.format("  [%s] %-25s %-15s %s%n",
                    m.getId(), m.getNomComplet(), m.getRole(), m.getTelephone()));
        }

        sb.append("\n").append(sep).append("\n");
        return sb.toString();
    }

    /**
     * Statistiques du tableau de bord.
     */
    public Map<String, Object> getStatsDashboard() {
        Map<String, Object> stats = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        stats.put("totalMembres", membreDAO.findActifs().size());
        stats.put("evenementsAVenir", evenementDAO.findAVenir().size());
        stats.put("soldeCaisse", transactionDAO.solde());
        stats.put("entreesTotal", transactionDAO.totalEntrees());
        stats.put("depensesTotal", transactionDAO.totalDepenses());
        stats.put("entreeMois", transactionDAO.totalMois(today.getMonthValue(), today.getYear()));
        stats.put("depenseMois", transactionDAO.depensesMois(today.getMonthValue(), today.getYear()));
        stats.put("totalTransactions", transactionDAO.count());
        stats.put("totalSacrements", new SacrementDAO().count());
        stats.put("totalPresences", new PresenceDAO().count());

        return stats;
    }

    /**
     * Sauvegarde un rapport dans un fichier texte.
     */
    public String sauvegarderRapport(String contenu, String destinationDir, String nomFichier) throws IOException {
        File dir = new File(destinationDir);
        if (!dir.exists()) dir.mkdirs();
        String path = destinationDir + File.separator + nomFichier + ".txt";
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"))) {
            pw.print(contenu);
        }
        return path;
    }
}
