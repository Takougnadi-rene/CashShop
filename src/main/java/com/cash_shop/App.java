package com.cash_shop;
import java.util.ArrayList;

import com.cash_shop.model.Aisle;
import com.cash_shop.model.Product;
import com.cash_shop.service.AisleService;

public class App 
{
    public static void main( String[] args )
    {   
        System.out.println("-------------------------Products-------------------------");
        ArrayList<Product> products = new ArrayList<>();
        Product product1 = new Product(234, "Coca Cola", 1.0, 1.5, 100);
        Product product2 = new Product(235, "Pepsi", 0.9, 1.4, 150);
        products.add(product1);
        products.add(product2);
        products.add(new Product(236, "Fanta", 0.8, 1.3, 200));
        for (Product product : products) {
            System.out.println(product);
        }

        System.out.println("-------------------------Aisles-------------------------");

        AisleService aisleService = new AisleService();
        Aisle aisle1 = new Aisle(1, "Beverages", "Beverage products");
        aisleService.addProductToAisle(aisle1, product1);
        aisleService.addProductToAisle(aisle1, product2);
        aisleService.addProductToAisle(aisle1, new Product(236, "Fanta", 0.8, 1.3, 200));
        System.out.println("Products in aisle " + aisle1.getAisleName() + ":");
        aisleService.displayProductsInAisle(aisle1);

    }
}
