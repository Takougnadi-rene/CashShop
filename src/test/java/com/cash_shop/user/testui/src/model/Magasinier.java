package model;

public class Magasinier extends Employe {
    public Magasinier(int matricule, String nom, String prenom, double salaire, String login, String motDePasse) {
        super(matricule, nom, prenom, salaire, login, motDePasse);
    }

    public void receptionnerLivraison() {
        System.out.println("Livraison réceptionnée.");
    }

    public void mettreAJourStock() {
        System.out.println("Stock mis à jour.");
    }

    @Override
    public String afficherRole() {
        return "Magasinier";
    }
}