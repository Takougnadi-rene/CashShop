package com.cash_shop.supplier;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import com.cash_shop.product.Product;

public class SupplierOrderServiceTest {
    private final SupplierOrderService service = new SupplierOrderService();

    @Test
    public void calculatesTotalFromOrderedQuantityAndPurchasePrice() {
        SupplierOrder order = newOrder();
        Product product = new Product(12, "Coffee", 2.50, 4.00, 500);

        service.addProduct(order, product, 3);

        assertEquals(7.50, service.calculateTotal(order), 0.001);
    }

    @Test
    public void addingSameProductIncreasesQuantityWithoutChangingPurchasePrice() {
        SupplierOrder order = newOrder();
        service.addProduct(order, new Product(12, "Coffee", 2.50, 4.00, 0), 2);
        service.addProduct(order, new Product(12, "Coffee", 3.00, 4.50, 0), 3);

        assertEquals(12.50, service.calculateTotal(order), 0.001);
        assertEquals(5, order.getListOfProducts().get(0).getStockQuantity());
    }

    private SupplierOrder newOrder() {
        return new SupplierOrder(1, new Date(), 1, "Supplier", BigDecimal.ZERO,
                SupplierOrder.OrderStatus.PENDING);
    }
}