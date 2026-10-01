package com.cash_shop.product;

import com.cash_shop.product.ArtisanalProduct.TypeArtisanal;

/**
 * Business operations on products (add, remove, modify, stock and margin). Each add/remove/modify also handles
 * the specialised table of the product type.
 */
public class ProduitService {
    private Product product;
    private final ProductDAO productDAO = new ProductDAO();

    /** Creates a standard product. */
    public void addProduct(int reference, String designation, double purchasePrice, double sellingPrice,
            int stockQuantity) {
        Product baseProduct = new Product(reference, designation, purchasePrice, sellingPrice, stockQuantity);
        productDAO.insertProduct(baseProduct);
        this.product = baseProduct;
    }

    /** Creates a fresh product (base row + fresh data). */
    public void addFreshProduct(int reference, String designation, double purchasePrice, double sellingPrice,
            int stockQuantity, String expirationDate, double storageTemperature) {
        Product baseProduct = new Product(reference, designation, purchasePrice, sellingPrice, stockQuantity);
        productDAO.insertProduct(baseProduct);
        FreshProduct fp = new FreshProduct(reference, designation, purchasePrice, sellingPrice, stockQuantity,
                expirationDate,
                storageTemperature);
        productDAO.insertFreshProduct(fp);
        this.product = fp;
    }

    /** Creates an artisanal product (base row + artisanal type). */
    public void addArtisanalProduct(int reference, String designation, double purchasePrice, double sellingPrice,
            int stockQuantity, String type) {
        Product baseProduct = new Product(reference, designation, purchasePrice, sellingPrice, stockQuantity);
        productDAO.insertProduct(baseProduct);
        TypeArtisanal artisanalType = TypeArtisanal.from(type);
        ArtisanalProduct ap = new ArtisanalProduct(reference, designation, purchasePrice, sellingPrice, stockQuantity,
                artisanalType);
        productDAO.insertArtisanalProduct(ap);
        this.product = ap;
    }

    /** Creates an electronic product (base row + brand and warranty). */
    public void addElectronicProduct(int reference, String designation, double purchasePrice, double sellingPrice,
            int stockQuantity, String brand, int warranty) {
        Product p = new Product(reference, designation, purchasePrice, sellingPrice, stockQuantity);
        productDAO.insertProduct(p);
        ElectronicProduct ep = new ElectronicProduct(reference, designation, purchasePrice, sellingPrice, stockQuantity,
                brand, warranty);
        productDAO.insertElectronicProduct(ep);
        this.product = ep;
    }

    /** Deletes a standard product. */
    public void removeProduct(Product product) {
        productDAO.deleteProduct(product);
    }

    /** Deletes an electronic product and its base row. */
    public void removeElectronicProduct(ElectronicProduct electronicProduct) {
        productDAO.deleteElectronicProduct(electronicProduct);
        productDAO.deleteProduct(electronicProduct);
    }

    /** Deletes a fresh product and its base row. */
    public void removeFreshProduct(FreshProduct freshProduct) {
        productDAO.deleteFreshProduct(freshProduct);
        productDAO.deleteProduct(freshProduct);
    }

    /** Deletes an artisanal product and its base row. */
    public void removeArtisanalProduct(ArtisanalProduct artisanalProduct) {
        productDAO.deleteArtisanalProduct(artisanalProduct);
        productDAO.deleteProduct(artisanalProduct);
    }

    /** Updates a standard product. */
    public void modifyProduct(Product product) {
        productDAO.updateProduct(product);
    }

    /** Updates an electronic product and its base data. */
    public void modifyElectronicProduct(ElectronicProduct electronicProduct) {
        productDAO.updateElectronicProduct(electronicProduct);
        productDAO.updateProduct(electronicProduct);
    }

    /** Updates a fresh product and its base data. */
    public void modifyFreshProduct(FreshProduct freshProduct) {
        productDAO.updateFreshProduct(freshProduct);
        productDAO.updateProduct(freshProduct);
    }

    /** Updates an artisanal product and its base data. */
    public void modifyArtisanalProduct(ArtisanalProduct artisanalProduct) {
        productDAO.updateArtisanalProduct(artisanalProduct);
        productDAO.updateProduct(artisanalProduct);
    }

    /** Adds units to the stock of a product. */
    public void addStock(Product product, int quantity) {
        product.setStockQuantity(product.getStockQuantity() + quantity);
        productDAO.updateProduct(product);
    }

    /** Removes units from the stock of a product. */
    public void removeStock(Product product, int quantity) {
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productDAO.updateProduct(product);
    }

    /** Changes the selling price of a product. */
    public void modifySellingPrice(Product product, double newPrice) {
        product.setSellingPrice(newPrice);
        productDAO.updateProduct(product);
    }

    /** Prints the margin (selling price - purchase price) of a product. */
    public void calculateMargin(Product product) {
        double margin = product.getSellingPrice() - product.getPurchasePrice();
        System.out.println("Margin for product " + product.getDesignation() + ": " + margin);
    }

    // Readable representation used for logging and debugging.
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ProduitService{");
        sb.append("product=").append(product);
        sb.append('}');
        return sb.toString();
    }

}
