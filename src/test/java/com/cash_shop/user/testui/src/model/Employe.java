package model;

/**
 * Classe mère de tous les employés du supermarché.
 *
 * Conformément au besoin métier, il n'existe plus de classe "Utilisateur"
 * séparée : chaque employé EST directement un utilisateur du système grâce
 * aux attributs login/motDePasse portés ici. Le rôle (donc les droits
 * d'accès) est déterminé par la sous-classe concrète (Caissier, Magasinier,
 * ChefRayon, Comptable, Directeur) et exploité par service.AccesService.
 */
public abstract class Employe {
    private int matricule;
    private String nom;
    private String prenom;
    private double salaire;
    private String login;
    private String motDePasse;

    public Employe(int matricule, String nom, String prenom, double salaire, String login, String motDePasse) {
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.salaire = salaire;
        this.login = login;
        this.motDePasse = motDePasse;
    }

    public void afficherInformations() {
        System.out.println(toString());
    }

    public double calculerPrime() {
        return salaire * 0.10;
    }

    public abstract String afficherRole();

    /**
     * Vérifie les identifiants de connexion de cet employé.
     */
    public boolean seConnecter(String login, String motDePasse) {
        return this.login != null && this.login.equals(login)
                && this.motDePasse != null && this.motDePasse.equals(motDePasse);
    }

    @Override
    public String toString() {
        return String.format("[%03d] %s %s | Rôle: %s | Salaire: %.0f F CFA", matricule, nom, prenom, afficherRole(),
                salaire);
    }

    public int getMatricule() {
        return matricule;
    }

    public void setMatricule(int matricule) {
        this.matricule = matricule;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public double getSalaire() {
        return salaire;
    }

    public void setSalaire(double salaire) {
        this.salaire = salaire;
    }

    public String getNomComplet() {
        return nom + " " + prenom;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }
}