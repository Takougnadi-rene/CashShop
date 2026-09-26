package model;

import java.util.Date;

public class Paiement {
    public enum ModePaiement {
        ESPECES, CARTE_BANCAIRE, MOBILE_MONEY
    }

    private int numeroPaiement;
    private double montant;
    private ModePaiement modePaiement;
    private Date datePaiement;
    private Vente vente;
    private boolean valide;

    public Paiement(int numeroPaiement, double montant, ModePaiement modePaiement, Vente vente) {
        this.numeroPaiement = numeroPaiement;
        this.montant = montant;
        this.modePaiement = modePaiement;
        this.datePaiement = new Date();
        this.vente = vente;
        this.valide = false;
    }

    public boolean validerPaiement() {
        if (montant >= vente.calculerTotal()) {
            this.valide = true;
            return true;
        }
        return false;
    }

    public double getMonnaie() {
        return montant - vente.calculerTotal();
    }

    public void afficherPaiement() {
        System.out.println(toString());
    }

    @Override
    public String toString() {
        return String.format("Paiement #%04d | Montant: %.0f F | Mode: %s | Statut: %s",
                numeroPaiement, montant, modePaiement, valide ? "VALIDÉ" : "EN ATTENTE");
    }

    public int getNumeroPaiement() {
        return numeroPaiement;
    }

    public double getMontant() {
        return montant;
    }

    public ModePaiement getModePaiement() {
        return modePaiement;
    }

    public Date getDatePaiement() {
        return datePaiement;
    }

    public Vente getVente() {
        return vente;
    }

    public boolean isValide() {
        return valide;
    }
}
