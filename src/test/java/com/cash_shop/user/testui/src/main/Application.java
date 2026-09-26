package main;

import model.*;
import service.EmployeService;
import interfaceGraphique.FenetreConnexion;
import javax.swing.*;
import java.util.Calendar;

public class Application {

    public static void main(String[] args) {
        // Look & Feel système
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialisation des données de test
        initialiserDonnees();

        // Lancement de la fenêtre de connexion
        SwingUtilities.invokeLater(() -> {
            FenetreConnexion connexion = new FenetreConnexion();
            connexion.setVisible(true);
        });
    }

    private static void initialiserDonnees() {
        Supermarche sm = Supermarche.getInstance();
        EmployeService employeService = new EmployeService();

        // === PRODUITS DE TEST ===
        sm.ajouterProduit(new Produit(1, "Riz Parfumé 5kg", 4000, 5000, 20, "Alimentaire"));
        sm.ajouterProduit(new Produit(2, "Lait Gloria 500ml", 800, 1000, 50, "Alimentaire"));
        sm.ajouterProduit(new Produit(3, "Huile Végétale 1L", 1500, 2000, 30, "Alimentaire"));
        sm.ajouterProduit(new Produit(4, "Pain de mie", 200, 250, 546, "Boulangerie"));
        sm.ajouterProduit(new Produit(5, "Coca-Cola 1.5L", 600, 800, 45, "Alimentaire"));
        sm.ajouterProduit(new Produit(6, "Savon Promotex", 150, 200, 100, "Entretien"));
        sm.ajouterProduit(new Produit(7, "Poulet frais 1kg", 1800, 2500, 334, "Boucherie"));
        sm.ajouterProduit(new Produit(8, "Téléphone TECNO", 35000, 45000, 98, "Electronique"));
        sm.ajouterProduit(new Produit(9, "Boisson Gazo 1,5L", 600, 800, 45, "Boissons"));
        sm.ajouterProduit(new Produit(10, "Boisson Gazo 1,5L", 600, 800, 45, "Boissons"));
        sm.ajouterProduit(new Produit(11, "Boisson Gazo 1,5L", 600, 800, 45, "Boissons"));

        // Produit frais avec date de péremption
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 3); // péremption dans 3 jours
        sm.ajouterProduit(new ProduitFrais(12, "Yaourt Nature", 300, 400, 102, cal.getTime(), 4.0));
        sm.ajouterProduit(new ProduitFrais(13, "Jus de fruit", 300, 400, 210, cal.getTime(), 4.0));
        sm.ajouterProduit(new ProduitFrais(14, "Lait frais", 300, 400, 32, cal.getTime(), 4.0));
        sm.ajouterProduit(new ProduitFrais(15, "Beurre", 300, 400, 92, cal.getTime(), 4.0));

        // Produit expiré (pour tester les alertes)
        Calendar calExp = Calendar.getInstance();
        calExp.add(Calendar.DAY_OF_MONTH, -2); // expiré il y a 2 jours
        sm.ajouterProduit(new ProduitFrais(16, "Lait frais périmé", 500, 650, 5, calExp.getTime(), 6.0));
        sm.ajouterProduit(new ProduitFrais(17, "Yaourt nature périmé", 500, 650, 5, calExp.getTime(), 6.0));
        sm.ajouterProduit(new ProduitFrais(18, "Jus de fruit périmé", 500, 650, 5, calExp.getTime(), 6.0));
        sm.ajouterProduit(new ProduitFrais(19, "Beurre périmé", 500, 650, 5, calExp.getTime(), 6.0));
        sm.ajouterProduit(new ProduitFrais(20, "Lait frais périmé", 500, 650, 5, calExp.getTime(), 6.0));

        // EMPLOYÉS DE TEST
        employeService.creerEmploye("Directeur", 1, "AMEGAN", "Paul", 500000, "admin", "admin");
        employeService.creerEmploye("Comptable", 6, "DOSSA", "Eléonore", 220000, "comptable", "1234");
        employeService.creerEmploye("Magasinier", 5, "AMAVI", "Marc", 180000, "magasinier", "1234");
        employeService.creerEmploye("Caissier", 2, "KOFFI", "Jean", 150000, "caissier1", "1234");
        employeService.creerEmploye("Caissier", 3, "AGBEKO", "Marie", 150000, "caissier2", "1234");
        ChefRayon chef1 = (ChefRayon) employeService.creerEmploye("ChefRayon", 7, "DUPONT", "Gad", 200000, "chef1",
                "1234");
        ChefRayon chef2 = (ChefRayon) employeService.creerEmploye("ChefRayon", 8, "DUPUIT", "Jacky", 200000, "chef2",
                "1234");
        ChefRayon chef3 = (ChefRayon) employeService.creerEmploye("ChefRayon", 9, "DORIAN", "Jean", 200000, "chef3",
                "1234");

        // === CLIENTS DE TEST ===
        Client c1 = new Client(1, "KOFFI Mensah", "+228 90 00 00 01");
        c1.ajouterPoints(150);
        Client c2 = new Client(2, "AGBE Kofi", "+228 90 00 00 02");
        c2.ajouterPoints(50);
        Client c3 = new Client(3, "ABLAVI Ama", "+228 90 00 00 03");
        c3.ajouterPoints(600);
        Client c4 = new Client(4, "KOFFI Mensah", "+228 90 00 00 01");
        c4.ajouterPoints(150);
        Client c5 = new Client(5, "AGBE Kofi", "+228 90 00 00 02");
        c5.ajouterPoints(50);
        Client c6 = new Client(6, "ADZOVI Ama", "+228 90 00 00 03");
        c6.ajouterPoints(600);
        Client c7 = new Client(7, "ADZO Amavi", "+228 90 00 00 03");
        c7.ajouterPoints(600);
        Client c8 = new Client(8, "AMEGNE cerveau", "+228 90 00 00 03");
        c8.ajouterPoints(600);
        Client c9 = new Client(9, "AMEGAN Komla", "+228 90 00 00 03");
        c9.ajouterPoints(600);
        Client c10 = new Client(10, "FOFOVI Yawovi", "+228 90 00 00 03");
        c10.ajouterPoints(600);

        sm.ajouterClient(c1);
        sm.ajouterClient(c2);
        sm.ajouterClient(c3);
        sm.ajouterClient(c4);
        sm.ajouterClient(c5);
        sm.ajouterClient(c6);
        sm.ajouterClient(c7);
        sm.ajouterClient(c8);
        sm.ajouterClient(c9);
        sm.ajouterClient(c10);

        // === FOURNISSEURS DE TEST ===
        sm.ajouterFournisseur(new Fournisseur("FRN-001", "SODIGAZ TOGO", "+228 22 11 00 00", "Lomé, Togo"));
        sm.ajouterFournisseur(new Fournisseur("FRN-002", "CDPA Kara", "+228 26 10 00 00", "Kara, Togo"));

        // === RAYONS DE TEST ===
        Rayon rayonAlim = new Rayon(1, "Alimentaire Epicerie", chef1);
        Rayon rayonBoucherie = new Rayon(2, "Boucherie", chef2);
        Rayon rayonBOisson = new Rayon(3, "Boissons", chef3);

        rayonAlim.ajouterProduit(sm.rechercherProduit(1));
        rayonAlim.ajouterProduit(sm.rechercherProduit(2));
        rayonBoucherie.ajouterProduit(sm.rechercherProduit(7));
        rayonBOisson.ajouterProduit(sm.rechercherProduit(9));
        rayonBOisson.ajouterProduit(sm.rechercherProduit(10));
        rayonBOisson.ajouterProduit(sm.rechercherProduit(11));
        sm.ajouterRayon(rayonAlim);
        sm.ajouterRayon(rayonBoucherie);
        sm.ajouterRayon(rayonBOisson);

        // confirmations d'initialisation des donnees
        System.out.println("Données initialisées avec succès !");
        System.out.println("Nombre de produits: " + sm.getProduits().size());
        System.out.println("Nombre de clients: " + sm.getClients().size());
        System.out.println("Nombre de fournisseurs: " + sm.getFournisseurs().size());
        System.out.println("Nombre de rayons: " + sm.getRayons().size());
        System.out.println("Nombre d'employés: " + sm.getEmployes().size());

        System.out.println("____________________________________________________________");

        //mot de passe et login des employes
        System.out.println("Mot de passe et login des employes:");
        for (Employe employe : sm.getEmployes()) {
            System.out.println("Employe: " + employe.getNom() + " " + employe.getPrenom() + " - "
                    + employe.getLogin() + " - " + employe.getMotDePasse());
        }
    }
}