package service;

import model.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Règle de gestion des accès aux modules de l'application, selon le rôle
 * (donc le type concret) de l'employé connecté.
 *
 * Répartition demandée :
 * - Directeur : accès à tout, SAUF la Caisse
 * - Comptable : Dashboard, Stock, Produits, Employés
 * - Magasinier : Fournisseurs, Stock, Produits
 * - Caissier : Caisse uniquement
 * - Chef Rayon : Rayons uniquement
 */
public class AccesService {

    public enum Module {
        PRODUITS("🛒 Produits"),
        STOCK("📦 Stock"),
        FOURNISSEURS("🚚 Fournisseurs"),
        CAISSE("💰 Caisse"),
        CLIENTS("👥 Clients"),
        EMPLOYES("👔 Employés"),
        RAYONS("🏪 Rayons"),
        STATISTIQUES("📊 Statistiques");

        private final String libelle;

        Module(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    /** Retourne la liste des modules auxquels l'employé a droit d'accès. */
    public List<Module> getModulesAutorises(Employe employe) {
        List<Module> modules = new ArrayList<>();
        if (employe instanceof Directeur) {
            modules.addAll(Arrays.asList(
                    Module.PRODUITS, Module.STOCK, Module.FOURNISSEURS,
                    Module.CLIENTS, Module.EMPLOYES, Module.RAYONS, Module.STATISTIQUES));
        } else if (employe instanceof Comptable) {
            modules.addAll(Arrays.asList(
                    Module.STATISTIQUES, Module.STOCK, Module.PRODUITS, Module.EMPLOYES));
        } else if (employe instanceof Magasinier) {
            modules.addAll(Arrays.asList(
                    Module.FOURNISSEURS, Module.STOCK, Module.PRODUITS));
        } else if (employe instanceof Caissier) {
            modules.add(Module.CAISSE);
        } else if (employe instanceof ChefRayon) {
            modules.add(Module.RAYONS);
        }
        return modules;
    }

    public boolean peutAcceder(Employe employe, Module module) {
        return getModulesAutorises(employe).contains(module);
    }

    /**
     * Certains rôles n'ont besoin que d'une seule fonctionnalité et sont donc
     * redirigés directement sur leur fenêtre métier après connexion, sans
     * passer par l'écran d'accueil général.
     */
    public boolean doitEtreRedirigeDirectement(Employe employe) {
        return employe instanceof Caissier || employe instanceof ChefRayon;
    }
}