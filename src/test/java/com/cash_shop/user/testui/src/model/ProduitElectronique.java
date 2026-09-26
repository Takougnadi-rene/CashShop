package model;

public class ProduitElectronique extends Produit {
    private String marque;
    private int garantie;

    public ProduitElectronique(int reference, String designation, double prixAchat, double prixVente,
            int quantiteStock, String marque, int garantie) {
        super(reference, designation, prixAchat, prixVente, quantiteStock, "Electronique");
        this.marque = marque;
        this.garantie = garantie;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Marque:%s | Garantie:%d mois", marque, garantie);
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public int getGarantie() {
        return garantie;
    }

    public void setGarantie(int garantie) {
        this.garantie = garantie;
    }
}
