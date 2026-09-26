package model;

import java.util.ArrayList;

public class Rayon {
    private int codeRayon;
    private String nomRayon;
    private ChefRayon responsable;
    private ArrayList<Produit> listeProduits;

    public Rayon(int codeRayon, String nomRayon, ChefRayon responsable) {
        this.codeRayon = codeRayon;
        this.nomRayon = nomRayon;
        this.responsable = responsable;
        this.listeProduits = new ArrayList<>();
    }

    public void ajouterProduit(Produit p) {
        listeProduits.add(p);
    }

    public boolean retirerProduit(int reference) {
        return listeProduits.removeIf(p -> p.getReference() == reference);
    }

    public Produit rechercherProduit(int reference) {
        return listeProduits.stream().filter(p -> p.getReference() == reference).findFirst().orElse(null);
    }

    public void afficherProduits() {
        System.out.println("=== Rayon: " + nomRayon + " ===");
        for (Produit p : listeProduits)
            System.out.println(p);
    }

    public double calculerValeurStock() {
        double total = 0;
        for (Produit p : listeProduits)
            total += p.getPrixVente() * p.getQuantiteStock();
        return total;
    }

    @Override
    public String toString() {
        return String.format("[RAY-%03d] %s | Responsable: %s | Produits: %d",
                codeRayon, nomRayon,
                responsable != null ? responsable.getNomComplet() : "N/A",
                listeProduits.size());
    }

    public int getCodeRayon() {
        return codeRayon;
    }

    public void setCodeRayon(int codeRayon) {
        this.codeRayon = codeRayon;
    }

    public String getNomRayon() {
        return nomRayon;
    }

    public void setNomRayon(String nomRayon) {
        this.nomRayon = nomRayon;
    }

    public ChefRayon getResponsable() {
        return responsable;
    }

    public void setResponsable(ChefRayon responsable) {
        this.responsable = responsable;
    }

    public ArrayList<Produit> getListeProduits() {
        return listeProduits;
    }
}
