package model;

import java.util.ArrayList;


public class Supermarche {
    private String nom;
    private ArrayList<Produit> produits;
    private ArrayList<Client> clients;
    private ArrayList<Employe> employes;
    private ArrayList<Rayon> rayons;
    private ArrayList<Vente> ventes;
    private ArrayList<Fournisseur> fournisseurs;

    private static Supermarche instance;

    private Supermarche(String nom) {
        this.nom = nom;
        this.produits = new ArrayList<>();
        this.clients = new ArrayList<>();
        this.employes = new ArrayList<>();
        this.rayons = new ArrayList<>();
        this.ventes = new ArrayList<>();
        this.fournisseurs = new ArrayList<>();
    }

    public static Supermarche getInstance() {
        if (instance == null)
            instance = new Supermarche("SUPER MARKET PLUS");
        return instance;
    }

    public void ajouterProduit(Produit p) {
        produits.add(p);
    }

    public void supprimerProduit(int ref) {
        produits.removeIf(p -> p.getReference() == ref);
    }

    public Produit rechercherProduit(int ref) {
        return produits.stream().filter(p -> p.getReference() == ref).findFirst().orElse(null);
    }

    public Produit rechercherProduitParNom(String nom) {
        return produits.stream().filter(p -> p.getDesignation().toLowerCase().contains(nom.toLowerCase())).findFirst()
                .orElse(null);
    }

    public void ajouterClient(Client c) {
        clients.add(c);
    }

    public void ajouterEmploye(Employe e) {
        employes.add(e);
    }

    public void ajouterRayon(Rayon r) {
        rayons.add(r);
    }

    public void ajouterFournisseur(Fournisseur f) {
        fournisseurs.add(f);
    }

    public void enregistrerVente(Vente v) {
        ventes.add(v);
    }

    public String getNom() {
        return nom;
    }

    public ArrayList<Produit> getProduits() {
        return produits;
    }

    public ArrayList<Client> getClients() {
        return clients;
    }

    public ArrayList<Employe> getEmployes() {
        return employes;
    }

    public ArrayList<Rayon> getRayons() {
        return rayons;
    }

    public ArrayList<Vente> getVentes() {
        return ventes;
    }

    public ArrayList<Fournisseur> getFournisseurs() {
        return fournisseurs;
    }
}