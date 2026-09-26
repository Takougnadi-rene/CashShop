package service;

import model.*;

/**
 * Règles de gestion liées au processus de vente en caisse :
 * ouverture d'une vente, ajout d'articles avec vérification du stock,
 * puis enregistrement définitif de la vente validée.
 */
public class VenteService {
    private final Supermarche supermarche = Supermarche.getInstance();
    private int compteurVente;

    public VenteService() {
        this.compteurVente = supermarche.getVentes().size() + 1;
    }

    public Vente nouvelleVente(Client client, Caissier caissier) {
        return new Vente(compteurVente++, client, caissier);
    }

    // Ajoute un produit au panier en vérifiant la disponibilité en stock.
    public boolean ajouterProduitAuPanier(Vente vente, Produit produit, int quantite) {
        if (quantite <= 0 || produit.getQuantiteStock() < quantite)
            return false;
        return vente.ajouterProduit(produit, quantite);
    }

    // Finalise la vente

    public void finaliserVente(Vente vente) {
        vente.validerVente();
        supermarche.enregistrerVente(vente);
    }
}