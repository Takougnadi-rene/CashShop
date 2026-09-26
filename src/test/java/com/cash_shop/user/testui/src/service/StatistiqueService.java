package service;

import model.*;
import java.util.ArrayList;

/**
 * Règles de gestion et calculs liés au tableau de bord de la Direction
 * (chiffre d'affaires, valeur du stock, alertes...).
 */
public class StatistiqueService {
    private static final int SEUIL_MINIMUM = 5;
    private final Supermarche supermarche = Supermarche.getInstance();

    public double calculerChiffreAffaires() {
        double ca = 0;
        for (Vente v : supermarche.getVentes()) {
            if (v.isValidee())
                ca += v.calculerTotal();
        }
        return ca;
    }

    public ArrayList<Produit> getProduitsStockFaible() {
        ArrayList<Produit> liste = new ArrayList<>();
        for (Produit p : supermarche.getProduits()) {
            if (p.getQuantiteStock() <= SEUIL_MINIMUM)
                liste.add(p);
        }
        return liste;
    }

    public ArrayList<Produit> getProduitsExpires() {
        ArrayList<Produit> liste = new ArrayList<>();
        for (Produit p : supermarche.getProduits()) {
            if (p instanceof ProduitFrais && ((ProduitFrais) p).estPerime())
                liste.add(p);
        }
        return liste;
    }

    public double calculerValeurTotaleStock() {
        double val = 0;
        for (Produit p : supermarche.getProduits()) {
            val += p.getPrixVente() * p.getQuantiteStock();
        }
        return val;
    }

    public int getSeuilMinimum() {
        return SEUIL_MINIMUM;
    }
}