package com.cash_shop.user;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.cash_shop.employee.Employee.Role;

public class AccessService {
    public enum Module {
        DASHBOARD,
        EMPLOYEES,
        CUSTOMERS,
        PRODUCTS,
        STOCK,
        AISLES,
        SALES,
        SUPPLIERS
    }

    public Set<Module> getAllowedModules(Role role) {
        if (role == null) {
            return Collections.emptySet();
        }
        switch (role) {
            case ADMIN:
                return Collections.unmodifiableSet(EnumSet.of(Module.DASHBOARD, Module.EMPLOYEES,
                        Module.CUSTOMERS, Module.PRODUCTS, Module.STOCK, Module.AISLES, Module.SUPPLIERS));
            case MANAGER:
                return Collections.unmodifiableSet(EnumSet.of(Module.DASHBOARD, Module.EMPLOYEES,
                        Module.PRODUCTS, Module.STOCK));
            case COUNTER:
            case CASHIER:
                return Collections.unmodifiableSet(EnumSet.of(Module.SALES));
            case AISLE_MANAGER:
                return Collections.unmodifiableSet(EnumSet.of(Module.AISLES));
            case SECURITY:
            case CLEANER:
            default:
                return Collections.emptySet();
        }
    }

    public boolean canAccess(Role role, Module module) {
        return getAllowedModules(role).contains(module);
    }
}
