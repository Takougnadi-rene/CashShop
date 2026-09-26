package service;

import model.Employe;
import model.Supermarche;

/**
 * Règle de gestion de l'authentification.
 * Chaque employé étant lui-même l'utilisateur (login/motDePasse portés par
 * model.Employe), ce service se contente de parcourir les employés
 * enregistrés pour trouver celui qui correspond aux identifiants saisis.
 */
public class AuthentificationService {
    private final Supermarche supermarche = Supermarche.getInstance();

    public Employe authentifier(String login, String motDePasse) {
        for (Employe e : supermarche.getEmployes()) {
            if (e.seConnecter(login, motDePasse)) return e;
        }
        return null;
    }
}