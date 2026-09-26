package model;

import java.util.ArrayList;

public class Client {
    private int numeroClient;
    private String nom;
    private String telephone;
    private int pointsFidelite;
    private ArrayList<Vente> historiqueAchats;

    public Client(int numeroClient, String nom, String telephone) {
        this.numeroClient = numeroClient;
        this.nom = nom;
        this.telephone = telephone;
        this.pointsFidelite = 0;
        this.historiqueAchats = new ArrayList<>();
    }

    public void ajouterPoints(int points) {
        this.pointsFidelite += points;
    }

    public ArrayList<Vente> consulterHistorique() {
        return historiqueAchats;
    }

    public void ajouterVente(Vente v) {
        historiqueAchats.add(v);
    }

    public String getNiveauFidelite() {
        if (pointsFidelite >= 500)
            return "Client VIP";
        if (pointsFidelite >= 200)
            return "Client Privilège";
        if (pointsFidelite >= 100)
            return "Client Régulier";
        return "Client Standard";
    }

    public void afficherClient() {
        System.out.println(toString());
    }

    @Override
    public String toString() {
        return String.format("[%03d] %s | Tél: %s | Points: %d (%s)",
                numeroClient, nom, telephone, pointsFidelite, getNiveauFidelite());
    }

    public int getNumeroClient() {
        return numeroClient;
    }

    public void setNumeroClient(int numeroClient) {
        this.numeroClient = numeroClient;
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

    public int getPointsFidelite() {
        return pointsFidelite;
    }

    public void setPointsFidelite(int pointsFidelite) {
        this.pointsFidelite = pointsFidelite;
    }

    public ArrayList<Vente> getHistoriqueAchats() {
        return historiqueAchats;
    }
}
