package com.syncplant.model;

import com.syncplant.Main;

public class OutboundWarehouse {
    private final int packageCapacity;
    private int unpackedProductCount;
    private int packageCount;
    private Main gui;

    public OutboundWarehouse(int packageCapacity, Main Main) {
        this.packageCapacity = packageCapacity;
        this.unpackedProductCount = 0;
        this.packageCount = 0;
        this.gui = Main;
    }

    // store products in warehouse
    public synchronized void store() {
        unpackedProductCount++;
        gui.updateProducts(unpackedProductCount);
        System.out.println("OW: unpacked products-" + unpackedProductCount);
        if (unpackedProductCount == packageCapacity) {
            unpackedProductCount = 0;
            gui.updateProducts(unpackedProductCount);
            packageCount++;
            gui.updatePackages(packageCount);
            System.out.println("OW: packages-" + packageCount);
            System.out.println("OW: unpacked products-" + unpackedProductCount);
        }

    }

    // take packages from warehouse
    public synchronized void take() {
        if (packageCount == 0) {
            System.out.println("OT: there are no packages to take");
            return;
        }
        packageCount = 0;
        System.out.println("OT: took packages from outbound warehouse");
        System.out.println("OW: packages-" + packageCount);
    }

}
