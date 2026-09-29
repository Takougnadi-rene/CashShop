package com.cash_shop.user;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import com.cash_shop.employee.Employee.Role;
import com.cash_shop.user.AccessService.Module;

public class AccessServiceTest {
    private final AccessService accessService = new AccessService();

    @Test
    public void accountantCanOnlyOpenReadOnlyBusinessModules() {
        assertEquals(EnumSet.of(Module.DASHBOARD, Module.EMPLOYEES, Module.PRODUCTS, Module.STOCK,
                Module.SUPPLIERS), accessService.getAllowedModules(Role.ACCOUNTANT));
    }

    @Test
    public void storekeeperCanOpenProductsStockSuppliersAndAisles() {
        assertEquals(EnumSet.of(Module.PRODUCTS, Module.STOCK, Module.SUPPLIERS, Module.AISLES),
                accessService.getAllowedModules(Role.STOREKEEPER));
    }

    @Test
    public void managerUsesStorekeeperAccessInThisApplication() {
        assertEquals(accessService.getAllowedModules(Role.STOREKEEPER),
                accessService.getAllowedModules(Role.MANAGER));
    }
}