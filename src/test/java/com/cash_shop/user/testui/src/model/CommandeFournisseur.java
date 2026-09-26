package model;

import java.util.ArrayList;
import java.util.Date;

public class CommandeFournisseur {
    public enum EtatCommande {
        EN_ATTENTE, VALIDEE, LIVREE, ANNULEE
    }

    private int numeroCommande;
    private Date date;
    private Fournisseur fournisseur;
    private ArrayList<Produit> listeProduits;
    private EtatCommande etatCommande;

    public CommandeFournisseur(int numeroCommande, Fournisseur fournisseur) {
        this.numeroCommande = numeroCommande;
        this.date = new Date();
        this.fournisseur = fournisseur;
        this.listeProduits = new ArrayList<>();
        this.etatCommande = EtatCommande.EN_ATTENTE;
    }

    public void ajouterProduit(Produit p) {
        listeProduits.add(p);
    }

    public void valider() {
        this.etatCommande = EtatCommande.VALIDEE;
    }

    public void livrer() {
        this.etatCommande = EtatCommande.LIVREE;
        for (Produit p : listeProduits) {
            p.ajouterStock(10); // quantité par défaut
        }
    }

    @Override
    public String toString() {
        return String.format("Commande #%03d | %s | Fournisseur: %s | État: %s",
                numeroCommande, new java.text.SimpleDateFormat("dd/MM/yyyy").format(date),
                fournisseur.getNom(), etatCommande);
    }

    public int getNumeroCommande() {
        return numeroCommande;
    }

    public Date getDate() {
        return date;
    }

    public Fournisseur getFournisseur() {
        return fournisseur;
    }

    public ArrayList<Produit> getListeProduits() {
        return listeProduits;
    }

    public EtatCommande getEtatCommande() {
        return etatCommande;
    }

    public void setEtatCommande(EtatCommande etat) {
        this.etatCommande = etat;
    }
}
