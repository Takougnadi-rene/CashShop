package service;

import model.Paiement;
import model.Vente;

public class PaiementService {
    private int compteurPaiement = 1;

    public boolean necessiteMontantRecu(Paiement.ModePaiement mode) {
        return mode == Paiement.ModePaiement.ESPECES;
    }

    /** Crée un paiement pour un montant reçu explicite (cas Espèces). */
    public Paiement creerPaiement(double montantRecu, Paiement.ModePaiement mode, Vente vente) {
        return new Paiement(compteurPaiement++, montantRecu, mode, vente);
    }

    /**
     * Crée un paiement "exact" (carte bancaire / mobile money) : pas de monnaie à
     * rendre.
     */
    public Paiement creerPaiementExact(Paiement.ModePaiement mode, Vente vente) {
        return creerPaiement(vente.calculerTotal(), mode, vente);
    }
}