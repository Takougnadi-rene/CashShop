package service;

import model.Produit;
import model.Supermarche;
import java.util.ArrayList;

public class StockService {
    private static final int SEUIL_MINIMUM = 5;
    private Supermarche supermarche;

    public StockService() {
        this.supermarche = Supermarche.getInstance();
    }

    public void entreeStock(int refProduit, int quantite) {
        Produit p = supermarche.rechercherProduit(refProduit);
        if (p != null)
            p.ajouterStock(quantite);
    }

    public boolean sortieStock(int refProduit, int quantite) {
        Produit p = supermarche.rechercherProduit(refProduit);
        if (p != null)
            return p.retirerStock(quantite);
        return false;
    }

    public ArrayList<Produit> verifierStockMinimum() {
        ArrayList<Produit> liste = new ArrayList<>();
        for (Produit p : supermarche.getProduits()) {
            if (p.getQuantiteStock() <= SEUIL_MINIMUM)
                liste.add(p);
        }
        return liste;
    }

    public boolean estStockFaible(Produit p) {
        return p.getQuantiteStock() <= SEUIL_MINIMUM;
    }
}