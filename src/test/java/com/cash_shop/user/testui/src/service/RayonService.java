package service;

import model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Règles de gestion des rayons.
 *
 * Règle métier demandée :
 *  - le Directeur voit la liste de tous les rayons, leur responsable et
 *    leur contenu ; il peut créer un rayon et nommer/changer son
 *    responsable ;
 *  - le Chef de Rayon ne voit que les informations de son propre rayon
 *    (lecture seule).
 */
public class RayonService {
    private final Supermarche supermarche = Supermarche.getInstance();

    public void creerRayon(int code, String nom) {
        supermarche.ajouterRayon(new Rayon(code, nom, null));
    }

    public void changerResponsable(Rayon rayon, ChefRayon responsable) {
        rayon.setResponsable(responsable);
    }

    public void affecterProduit(Rayon rayon, Produit produit) {
        rayon.ajouterProduit(produit);
    }

    public double calculerValeurStock(Rayon rayon) {
        return rayon.calculerValeurStock();
    }

    /** Rayons visibles pour l'employé connecté, selon la règle d'accès ci-dessus. */
    public List<Rayon> getRayonsVisibles(Employe employe) {
        List<Rayon> tous = supermarche.getRayons();
        if (employe instanceof ChefRayon) {
            List<Rayon> mesRayons = new ArrayList<>();
            for (Rayon r : tous) {
                if (r.getResponsable() == employe) mesRayons.add(r);
            }
            return mesRayons;
        }
        return tous;
    }
}