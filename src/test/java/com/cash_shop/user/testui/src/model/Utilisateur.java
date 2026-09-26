package model;

/**
 * Compte de connexion à l'application.
 *
 * Un Utilisateur EST toujours associé à un Employe (l'attribut employe),
 * mais l'inverse n'est pas vrai : un Employe peut très bien ne jamais
 * recevoir de compte Utilisateur (ex. personnel qui n'utilise pas
 * l'application). Cette classe reste donc volontairement distincte
 * d'Employe, avec une simple référence vers lui.
 */
public class Utilisateur {
    private String login;
    private String motDePasse;
    private Employe employe;
    private String role;

    public Utilisateur(String login, String motDePasse, Employe employe) {
        this.login = login;
        this.motDePasse = motDePasse;
        this.employe = employe;
        this.role = employe.afficherRole();
    }

    public boolean seConnecter(String login, String motDePasse) {
        return this.login.equals(login) && this.motDePasse.equals(motDePasse);
    }

    public boolean verifierRole(String roleAttendu) {
        return this.role.equalsIgnoreCase(roleAttendu);
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

    public Employe getEmploye() {
        return employe;
    }

    public void setEmploye(Employe employe) {
        this.employe = employe;
        this.role = employe.afficherRole();
    }

    public String getRole() {
        return role;
    }
}