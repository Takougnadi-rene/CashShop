package model;

import java.util.Date;

public class ProduitFrais extends Produit {
    private Date datePeremption;
    private double temperatureConservation;

    public ProduitFrais(int reference, String designation, double prixAchat, double prixVente,
            int quantiteStock, Date datePeremption, double temperatureConservation) {
        super(reference, designation, prixAchat, prixVente, quantiteStock, "Frais");
        this.datePeremption = datePeremption;
        this.temperatureConservation = temperatureConservation;
    }

    public boolean estPerime() {
        return datePeremption != null && datePeremption.before(new Date());
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Péremption:%s | Temp:%.1f°C",
                datePeremption != null ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(datePeremption) : "N/A",
                temperatureConservation);
    }

    public Date getDatePeremption() {
        return datePeremption;
    }

    public void setDatePeremption(Date datePeremption) {
        this.datePeremption = datePeremption;
    }

    public double getTemperatureConservation() {
        return temperatureConservation;
    }

    public void setTemperatureConservation(double temperatureConservation) {
        this.temperatureConservation = temperatureConservation;
    }
}
