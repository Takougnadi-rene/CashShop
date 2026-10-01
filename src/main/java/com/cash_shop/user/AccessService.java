package com.cash_shop.user;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.cash_shop.employee.Employee.Role;

/** Role-based access control: tells which application modules each role may open. */
public class AccessService {
/** Application modules whose access is controlled. */
public enum Module {
        DASHBOARD,
        EMPLOYEES,
        USERS,
        CUSTOMERS,
        PRODUCTS,
        STOCK,
        AISLES,
        SALES,
        SALES_HISTORY,
        SUPPLIERS
    }

    /** Returns the modules the role may open (empty for SECURITY, CLEANER or an unknown role). */
    public Set<Module> getAllowedModules(Role role) {
        if (role == null) {
            return Collections.emptySet();
        }
        switch (role) {
            case ADMIN:
                return Collections.unmodifiableSet(EnumSet.of(Module.DASHBOARD, Module.EMPLOYEES, Module.USERS,
                        Module.PRODUCTS, Module.STOCK, Module.AISLES, Module.SUPPLIERS, Module.SALES_HISTORY));
            case MANAGER:
                return Collections.unmodifiableSet(EnumSet.of(
                        Module.PRODUCTS, Module.STOCK, Module.SUPPLIERS, Module.AISLES));
            case COUNTER:
                return Collections.unmodifiableSet(EnumSet.of(Module.EMPLOYEES,
                        Module.PRODUCTS, Module.STOCK, Module.SUPPLIERS, Module.SALES_HISTORY));
            case CASHIER:
                return Collections.unmodifiableSet(EnumSet.of(Module.SALES));
            case AISLE_MANAGER:
                return Collections.unmodifiableSet(EnumSet.of(Module.AISLES));
            default:
                return Collections.emptySet();
        }
    }

    /** Tells whether the role may open the module. */
    public boolean canAccess(Role role, Module module) {
        return getAllowedModules(role).contains(module);
    }
}
