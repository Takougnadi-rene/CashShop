package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Vente {
    private int numeroVente;
    private Date date;
    private Client client;
    private Caissier caissier;
    private ArrayList<Produit> panier;
    private Map<Integer, Integer> quantites; // ref -> quantité
    private boolean validee;

    public Vente(int numeroVente, Client client, Caissier caissier) {
        this.numeroVente = numeroVente;
        this.date = new Date();
        this.client = client;
        this.caissier = caissier;
        this.panier = new ArrayList<>();
        this.quantites = new HashMap<>();
        this.validee = false;
    }

    public boolean ajouterProduit(Produit p, int quantite) {
        if (p.getQuantiteStock() >= quantite) {
            panier.add(p);
            quantites.put(p.getReference(), quantite);
            return true;
        }
        return false;
    }

    public void supprimerProduit(Produit p) {
        panier.remove(p);
        quantites.remove(p.getReference());
    }

    public double calculerTotal() {
        double total = 0;
        for (Produit p : panier) {
            int qte = quantites.getOrDefault(p.getReference(), 1);
            total += p.getPrixVente() * qte;
        }
        return total;
    }

    public void validerVente() {
        for (Produit p : panier) {
            int qte = quantites.getOrDefault(p.getReference(), 1);
            p.retirerStock(qte);
        }
        if (client != null) {
            int points = (int) (calculerTotal() / 1000);
            client.ajouterPoints(points);
            client.ajouterVente(this);
        }
        this.validee = true;
    }

    public int getQuantite(Produit p) {
        return quantites.getOrDefault(p.getReference(), 1);
    }

    @Override
    public String toString() {
        return String.format("Vente #%04d | %s | Client: %s | Total: %.0f F CFA",
                numeroVente,
                new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(date),
                client != null ? client.getNom() : "Tout-Venant",
                calculerTotal());
    }

    public int getNumeroVente() {
        return numeroVente;
    }

    public Date getDate() {
        return date;
    }

    public Client getClient() {
        return client;
    }

    public Caissier getCaissier() {
        return caissier;
    }

    public ArrayList<Produit> getPanier() {
        return panier;
    }

    public boolean isValidee() {
        return validee;
    }

    public Map<Integer, Integer> getQuantites() {
        return quantites;
    }
}
