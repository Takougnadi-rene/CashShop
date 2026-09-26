package model;

public class Caissier extends Employe {
    private int numeroCaisse;
    private boolean caisseOuverte;

    public Caissier(int matricule, String nom, String prenom, double salaire, int numeroCaisse,
            String login, String motDePasse) {
        super(matricule, nom, prenom, salaire, login, motDePasse);
        this.numeroCaisse = numeroCaisse;
        this.caisseOuverte = false;
    }

    public void ouvrirCaisse() {
        this.caisseOuverte = true;
    }

    public void effectuerVente() {
        System.out.println("Vente en cours - Caisse " + numeroCaisse);
    }

    public void encaisserPaiement() {
        System.out.println("Paiement encaissé.");
    }

    @Override
    public String afficherRole() {
        return "Caissier";
    }

    public int getNumeroCaisse() {
        return numeroCaisse;
    }

    public void setNumeroCaisse(int numeroCaisse) {
        this.numeroCaisse = numeroCaisse;
    }

    public boolean isCaisseOuverte() {
        return caisseOuverte;
    }
}