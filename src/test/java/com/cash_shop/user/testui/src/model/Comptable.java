package model;

public class Comptable extends Employe {
    public Comptable(int matricule, String nom, String prenom, double salaire, String login, String motDePasse) {
        super(matricule, nom, prenom, salaire, login, motDePasse);
    }

    public void genererRapport() { System.out.println("Rapport financier généré."); }

    @Override
    public String afficherRole() { return "Comptable"; }
}