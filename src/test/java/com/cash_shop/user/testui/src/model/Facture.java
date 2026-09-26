package model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Facture {
    private int numeroFacture;
    private Date dateFacture;
    private Vente vente;
    private Paiement paiement;

    public Facture(int numeroFacture, Vente vente, Paiement paiement) {
        this.numeroFacture = numeroFacture;
        this.dateFacture = new Date();
        this.vente = vente;
        this.paiement = paiement;
    }

    public String genererFacture() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        StringBuilder sb = new StringBuilder();
        sb.append("================================================\n");
        sb.append("         \uD83C\uDFEA SUPER MARKET PLUS\n");
        sb.append("         Rapport d'Achat - Ticket\n");
        sb.append("================================================\n\n");
        sb.append(String.format("Facture N° : %04d        Date : %s\n", numeroFacture, sdf.format(dateFacture)));
        sb.append(String.format("Caissier   : %-15s Client : %s\n",
                vente.getCaissier() != null ? vente.getCaissier().getNomComplet() : "N/A",
                vente.getClient() != null ? vente.getClient().getNom() : "Tout-Venant"));
        sb.append("\n------------------------------------------------\n");
        sb.append(String.format("%-20s %5s %15s\n", "PRODUIT", "QTÉ", "PRIX (FCFA)"));
        sb.append("------------------------------------------------\n");
        for (model.Produit p : vente.getPanier()) {
            int qte = vente.getQuantite(p);
            sb.append(String.format("%-20s %5d %15.0f F\n", p.getDesignation(), qte, p.getPrixVente() * qte));
        }
        sb.append("\n------------------------------------------------\n");
        sb.append(String.format("\uD83D\uDD35 TOTAL À PAYER :         %15.0f FCFA\n", vente.calculerTotal()));
        sb.append(String.format("\nRèglement : %s\n", paiement.getModePaiement()));
        if (paiement.getMonnaie() > 0)
            sb.append(String.format("Monnaie    : %.0f F CFA\n", paiement.getMonnaie()));
        sb.append("\n================================================\n");
        sb.append("         Merci de votre confiance !\n");
        sb.append("         À bientôt dans nos rayons.\n");
        sb.append("================================================\n");
        return sb.toString();
    }

    public void imprimerFacture() {
        System.out.println(genererFacture());
    }

    public void afficherFacture() {
        System.out.println(genererFacture());
    }

    public int getNumeroFacture() {
        return numeroFacture;
    }

    public Date getDateFacture() {
        return dateFacture;
    }

    public Vente getVente() {
        return vente;
    }

    public Paiement getPaiement() {
        return paiement;
    }
}
