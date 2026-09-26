package model;

public class Directeur extends Employe {
    public Directeur(int matricule, String nom, String prenom, double salaire, String login, String motDePasse) {
        super(matricule, nom, prenom, salaire, login, motDePasse);
    }

    @Override
    public String afficherRole() {
        return "Directeur";
    }
}