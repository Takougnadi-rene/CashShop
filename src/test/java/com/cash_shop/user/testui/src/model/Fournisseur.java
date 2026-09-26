package model;

import java.util.ArrayList;

public class Fournisseur {
    private String code;
    private String nom;
    private String telephone;
    private String adresse;
    private ArrayList<Produit> produitsFournis;

    public Fournisseur(String code, String nom, String telephone, String adresse) {
        this.code = code;
        this.nom = nom;
        this.telephone = telephone;
        this.adresse = adresse;
        this.produitsFournis = new ArrayList<>();
    }

    public void ajouterProduit(Produit p) {
        produitsFournis.add(p);
    }

    public void afficher() {
        System.out.println(toString());
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Tél: %s | Adresse: %s", code, nom, telephone, adresse);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public ArrayList<Produit> getProduitsFournis() {
        return produitsFournis;
    }
}
