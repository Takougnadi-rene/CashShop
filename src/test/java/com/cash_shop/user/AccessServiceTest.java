package com.cash_shop.user;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import com.cash_shop.employee.Employee.Role;
import com.cash_shop.user.AccessService.Module;

public class AccessServiceTest {
    private final AccessService accessService = new AccessService();

    @Test
    public void adminCanManageModulesButCannotOpenSales() {
        assertEquals(EnumSet.of(Module.DASHBOARD, Module.EMPLOYEES, Module.CUSTOMERS, Module.PRODUCTS,
                Module.STOCK, Module.AISLES, Module.SUPPLIERS), accessService.getAllowedModules(Role.ADMIN));
        assertFalse(accessService.canAccess(Role.ADMIN, Module.SALES));
    }

    @Test
    public void managerGetsAccountingModules() {
        assertEquals(EnumSet.of(Module.DASHBOARD, Module.EMPLOYEES, Module.PRODUCTS, Module.STOCK),
                accessService.getAllowedModules(Role.MANAGER));
    }

    @Test
    public void cashierAndCounterOnlyGetSales() {
        assertEquals(EnumSet.of(Module.SALES), accessService.getAllowedModules(Role.CASHIER));
        assertEquals(EnumSet.of(Module.SALES), accessService.getAllowedModules(Role.COUNTER));
    }

    @Test
    public void aisleManagerOnlyGetsAisles() {
        assertEquals(EnumSet.of(Module.AISLES), accessService.getAllowedModules(Role.AISLE_MANAGER));
    }

    @Test
    public void unsupportedRolesHaveNoAccess() {
        assertTrue(accessService.getAllowedModules(Role.SECURITY).isEmpty());
        assertTrue(accessService.getAllowedModules(Role.CLEANER).isEmpty());
        assertTrue(accessService.getAllowedModules(null).isEmpty());
    }
}
