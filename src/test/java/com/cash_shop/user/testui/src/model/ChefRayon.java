package model;

public class ChefRayon extends Employe {
    private String rayonResponsable;

    public ChefRayon(int matricule, String nom, String prenom, double salaire, String rayonResponsable,
            String login, String motDePasse) {
        super(matricule, nom, prenom, salaire, login, motDePasse);
        this.rayonResponsable = rayonResponsable;
    }

    @Override
    public String afficherRole() {
        return "Chef de Rayon";
    }

    public String getRayonResponsable() {
        return rayonResponsable;
    }

    public void setRayonResponsable(String rayonResponsable) {
        this.rayonResponsable = rayonResponsable;
    }
}