package com.syncplant;

import com.syncplant.model.*;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("SyncProductionPlant is starting...");

        Map<String, Object> config = ConfigLoader.loadConfig();
        System.out.println("Loaded config:");
        System.out.println(config);
        System.out.println();

        // warehouses
        Map<String, Object> warehouseAConfig = (Map<String, Object>) config.get("warehouseA");
        Map<String, Object> warehouseBConfig = (Map<String, Object>) config.get("warehouseB");
        Warehouse warehouseA = new Warehouse("A", (Integer) warehouseAConfig.get("initial_stock"), (Integer) warehouseAConfig.get("threshold"), null);
        Warehouse warehouseB = new Warehouse("B", (Integer) warehouseBConfig.get("initial_stock"), (Integer) warehouseBConfig.get("threshold"), null);

        // supply truck
        SupplyTruck supplyTruck = new SupplyTruck(new Warehouse[]{warehouseA, warehouseB}, (Integer) config.get("delivery_amount"), new ProductionLine[]{});

        // assign supply truck to warehouses
        warehouseA.setTruck(supplyTruck);
        warehouseB.setTruck(supplyTruck);

        int numOfLines = (Integer) config.get("number_of_production_lines");
        ProductionLine[] productionLines = new ProductionLine[numOfLines];
        for (int i = 0; i < numOfLines; i++) {
            productionLines[i] = new ProductionLine(i + 1, warehouseA, warehouseB, (Integer) config.get("required_A_amount"), (Integer) config.get("required_B_amount"));
        }

        // start the production
        for (ProductionLine line : productionLines) {
            line.start();
        }

        supplyTruck.start();

    }
}
