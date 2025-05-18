package com.syncplant.model;

import com.syncplant.gui.MainApp;

public class OutboundWarehouse {
    private final int packageCapacity;
    private int unpackedProductCount;
    private int packageCount;
    private MainApp mainApp;

    public OutboundWarehouse(int packageCapacity, MainApp mainApp) {
        this.packageCapacity = packageCapacity;
        this.unpackedProductCount = 0;
        this.packageCount = 0;
        this.mainApp = mainApp;
    }

    // store products in warehouse
    public synchronized void store() {
        unpackedProductCount++;
        System.out.println("OW: unpacked products-" + unpackedProductCount);
        if (unpackedProductCount == packageCapacity) {
            packageCount++;
            unpackedProductCount = 0;
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
