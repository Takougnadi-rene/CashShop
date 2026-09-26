package com.cash_shop.product;

import com.cash_shop.product.ArtisanalProduct.TypeArtisanal;

public class ProduitService {
    private Product product;
    private final ProductDAO productDAO = new ProductDAO();

    public void addProduct(int reference, String designation, double purchasePrice, double sellingPrice,
            int stockQuantity) {
        Product product = new Product(reference, designation, purchasePrice, sellingPrice, stockQuantity);
        productDAO.insertProduct(product);
        this.product = product;
    }

    public void addFreshProduct(int reference, String designation, double purchasePrice, double sellingPrice,
            int stockQuantity, String expirationDate, double storageTemperature) {
        Product product = new Product(reference, designation, purchasePrice, sellingPrice, stockQuantity);
        productDAO.insertProduct(product);
        FreshProduct fp = new FreshProduct(reference, designation, purchasePrice, sellingPrice, stockQuantity,
                expirationDate,
                storageTemperature);
        productDAO.insertFreshProduct(fp);
        this.product = fp;
    }

    public void addArtisanalProduct(int reference, String designation, double purchasePrice, double sellingPrice,
            int stockQuantity, String type) {
        Product product = new Product(reference, designation, purchasePrice, sellingPrice, stockQuantity);
        productDAO.insertProduct(product);
        TypeArtisanal artisanalType = TypeArtisanal.valueOf(type.trim().toUpperCase());
        ArtisanalProduct ap = new ArtisanalProduct(reference, designation, purchasePrice, sellingPrice, stockQuantity,
                artisanalType);
        productDAO.insertArtisanalProduct(ap);
        this.product = ap;
    }

    public void addElectronicProduct(int reference, String designation, double purchasePrice, double sellingPrice,
            int stockQuantity, String brand, int warranty) {
        Product p = new Product(reference, designation, purchasePrice, sellingPrice, stockQuantity);
        productDAO.insertProduct(p);
        ElectronicProduct ep = new ElectronicProduct(reference, designation, purchasePrice, sellingPrice, stockQuantity,
                brand, warranty);
        productDAO.insertElectronicProduct(ep);
        this.product = ep;
    }

    public void removeProduct(Product product) {
        productDAO.deleteProduct(product);
    }

    public void removeElectronicProduct(ElectronicProduct electronicProduct) {
        productDAO.deleteElectronicProduct(electronicProduct);
        productDAO.deleteProduct(electronicProduct);
    }

    public void removeFreshProduct(FreshProduct freshProduct) {
        productDAO.deleteFreshProduct(freshProduct);
        productDAO.deleteProduct(freshProduct);
    }

    public void removeArtisanalProduct(ArtisanalProduct artisanalProduct) {
        productDAO.deleteArtisanalProduct(artisanalProduct);
        productDAO.deleteProduct(artisanalProduct);
    }

    public void modifyProduct(Product product) {
        productDAO.updateProduct(product);
    }

    public void modifyElectronicProduct(ElectronicProduct electronicProduct) {
        productDAO.updateElectronicProduct(electronicProduct);
        productDAO.updateProduct(electronicProduct);
    }

    public void modifyFreshProduct(FreshProduct freshProduct) {
        productDAO.updateFreshProduct(freshProduct);
        productDAO.updateProduct(freshProduct);
    }

    public void modifyArtisanalProduct(ArtisanalProduct artisanalProduct) {
        productDAO.updateArtisanalProduct(artisanalProduct);
        productDAO.updateProduct(artisanalProduct);
    }

    public void addStock(Product product, int quantity) {
        product.setStockQuantity(product.getStockQuantity() + quantity);
        productDAO.updateProduct(product);
    }

    public void removeStock(Product product, int quantity) {
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productDAO.updateProduct(product);
    }

    public void modifySellingPrice(Product product, double newPrice) {
        product.setSellingPrice(newPrice);
        productDAO.updateProduct(product);
    }

    public void calculateMargin(Product product) {
        double margin = product.getSellingPrice() - product.getPurchasePrice();
        System.out.println("Margin for product " + product.getDesignation() + ": " + margin);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ProduitService{");
        sb.append("product=").append(product);
        sb.append('}');
        return sb.toString();
    }

}
