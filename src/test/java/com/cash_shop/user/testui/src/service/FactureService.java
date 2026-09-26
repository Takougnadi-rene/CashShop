package service;

import model.Paiement;
import model.Vente;

/**
 * Règle de gestion de la numérotation et de la génération des factures.
 */
public class FactureService {
    private int compteurFacture = 1;

    public model.Facture genererFacture(Vente vente, Paiement paiement) {
        return new model.Facture(compteurFacture++, vente, paiement);
    }
}