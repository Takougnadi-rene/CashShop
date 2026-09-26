package model;

public class Produit {
    private int reference;
    private String designation;
    private double prixAchat;
    private double prixVente;
    private int quantiteStock;
    private String categorie;

    public Produit(int pReference, String pDesignation, double pPrixAchat, double pPrixVente, int pQuantiteStock, String pPategorie) {
        pReference = reference;
        pDesignation = designation;
        pPrixAchat = prixAchat;
        pPrixVente = prixVente;
        pQuantiteStock = quantiteStock;
        pPategorie = categorie;
    }

    // Getters & Setters
    public int getReference() {
        return reference;
    }

    public void setReference(int reference) {
        this.reference = reference;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public double getPrixAchat() {
        return prixAchat;
    }

    public void setPrixAchat(double prixAchat) {
        this.prixAchat = prixAchat;
    }

    public double getPrixVente() {
        return prixVente;
    }

    public void setPrixVente(double prixVente) {
        this.prixVente = prixVente;
    }

    public int getQuantiteStock() {
        return quantiteStock;
    }

    public void setQuantiteStock(int quantiteStock) {
        this.quantiteStock = quantiteStock;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public void ajouterStock(int quantite) {
        this.quantiteStock += quantite;
    }

    public boolean retirerStock(int quantite) {
        if (quantite <= quantiteStock) {
            this.quantiteStock -= quantite;
            return true;
        }
        return false;
    }

    public void modifierPrix(double nouveauPrixVente) {
        this.prixVente = nouveauPrixVente;
    }

    public double calculerMarge() {
        return prixVente - prixAchat;
    }

    @Override
    public String toString() {
        return String.format("REF:%03d | %-20s | Achat:%.0f F | Vente:%.0f F | Stock:%d | %s",
                reference, designation, prixAchat, prixVente, quantiteStock, categorie);
    }
}
