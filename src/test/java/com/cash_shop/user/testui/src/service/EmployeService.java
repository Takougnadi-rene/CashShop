package service;

import model.*;

/**
 * Règle de gestion de la création du personnel : instancie la bonne
 * sous-classe d'Employe selon le rôle choisi et l'enregistre dans le
 * supermarché. Comme chaque employé est aussi un utilisateur, le login et
 * le mot de passe sont fixés dès la création.
 */
public class EmployeService {
    private final Supermarche supermarche = Supermarche.getInstance();

    public Employe creerEmploye(String role, int matricule, String nom, String prenom,
            double salaire, String login, String motDePasse) {
        Employe emp;
        switch (role) {
            case "Caissier":
                emp = new Caissier(matricule, nom, prenom, salaire, matricule, login, motDePasse);
                break;
            case "Magasinier":
                emp = new Magasinier(matricule, nom, prenom, salaire, login, motDePasse);
                break;
            case "ChefRayon":
                emp = new ChefRayon(matricule, nom, prenom, salaire, "", login, motDePasse);
                break;
            case "Comptable":
                emp = new Comptable(matricule, nom, prenom, salaire, login, motDePasse);
                break;
            default:
                emp = new Directeur(matricule, nom, prenom, salaire, login, motDePasse);
        }
        supermarche.ajouterEmploye(emp);
        return emp;
    }
}