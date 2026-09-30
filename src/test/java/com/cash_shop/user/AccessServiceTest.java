package com.cash_shop.user;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import com.cash_shop.employee.Employee.Role;
import com.cash_shop.user.AccessService.Module;

public class AccessServiceTest {
    private final AccessService accessService = new AccessService();

    @Test
    public void counterHasAccountantPermissionsButNotCheckout() {
        assertEquals(EnumSet.of(Module.EMPLOYEES, Module.PRODUCTS, Module.STOCK,
                Module.SUPPLIERS, Module.SALES_HISTORY), accessService.getAllowedModules(Role.COUNTER));
        org.junit.Assert.assertFalse(accessService.canAccess(Role.COUNTER, Module.SALES));
    }

    @Test
    public void administratorCanViewSalesButCannotOpenCheckout() {
        assertEquals(EnumSet.of(Module.DASHBOARD, Module.EMPLOYEES, Module.USERS,
                Module.PRODUCTS, Module.STOCK, Module.AISLES, Module.SUPPLIERS, Module.SALES_HISTORY),
                accessService.getAllowedModules(Role.ADMIN));
        org.junit.Assert.assertFalse(accessService.canAccess(Role.ADMIN, Module.SALES));
    }

    @Test
    public void salesHistoryIsNotAvailableToCashiers() {
        org.junit.Assert.assertFalse(accessService.canAccess(Role.CASHIER, Module.SALES_HISTORY));
        org.junit.Assert.assertTrue(accessService.canAccess(Role.CASHIER, Module.SALES));
    }

    @Test
    public void managerCanCreateOrdersButCannotReviewOrViewSales() {
        assertEquals(EnumSet.of(Module.PRODUCTS, Module.STOCK, Module.SUPPLIERS, Module.AISLES),
                accessService.getAllowedModules(Role.MANAGER));
        org.junit.Assert.assertFalse(accessService.canAccess(Role.MANAGER, Module.SALES_HISTORY));
    }

    @Test
    public void managerCanManageInventoryAndSupplierOrders() {
        org.junit.Assert.assertTrue(accessService.canAccess(Role.MANAGER, Module.PRODUCTS));
        org.junit.Assert.assertTrue(accessService.canAccess(Role.MANAGER, Module.STOCK));
        org.junit.Assert.assertTrue(accessService.canAccess(Role.MANAGER, Module.SUPPLIERS));
        org.junit.Assert.assertTrue(accessService.canAccess(Role.MANAGER, Module.AISLES));
    }
}