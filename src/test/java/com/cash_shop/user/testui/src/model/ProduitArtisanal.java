package model;

public class ProduitArtisanal extends Produit {
    public enum TypeArtisanal {
        BOULANGERIE, POISSONNERIE, BOUCHERIE
    }

    private TypeArtisanal typeArtisanal;

    public ProduitArtisanal(int reference, String designation, double prixAchat, double prixVente,
            int quantiteStock, TypeArtisanal typeArtisanal) {
        super(reference, designation, prixAchat, prixVente, quantiteStock, typeArtisanal.toString());
        this.typeArtisanal = typeArtisanal;
    }

    @Override
    public String toString() {
        return super.toString() + " | Type:" + typeArtisanal;
    }

    public TypeArtisanal getTypeArtisanal() {
        return typeArtisanal;
    }

    public void setTypeArtisanal(TypeArtisanal typeArtisanal) {
        this.typeArtisanal = typeArtisanal;
    }
}
