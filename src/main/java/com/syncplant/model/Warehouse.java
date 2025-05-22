package com.syncplant.model;

import com.syncplant.Main;

public class Warehouse {
    private final String warehouseSymbol;
    private int stock;
    private int threshold;
    private boolean needsSupply;
    private SupplyTruck truck;
    private Main gui;

    public Warehouse(String symbol, int initialStock, int threshold, SupplyTruck truck, Main gui) {
        warehouseSymbol = symbol;
        stock = initialStock;
        this.threshold = threshold;
        needsSupply = false;
        this.truck = truck;
        this.gui = gui;
    }

    // take raw material from warehouse
    public synchronized boolean take(int amount) {
        if (amount <= stock) {
            // delay simulating the time needed to take materials from warehouse
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println("Warehouse " + warehouseSymbol + " interrupted while materials retrieval");
            }
            stock -= amount;
            System.out.println("WH " + warehouseSymbol + ": stock-" + stock + ", " + amount + " units of material was taken");

            // warehouse update in the GUI
            if (warehouseSymbol.equals("A")) {
                gui.updateWarehouseA(stock);
            } else {
                gui.updateWarehouseB(stock);
            }

            if (stock < threshold && !needsSupply) {
                needsSupply = true;
                truck.notifyTruck();
            }
            return true;
        }
        return false;
    }

    public synchronized boolean needsRefill() {
        return needsSupply;
    }

    public String getSymbol() {
        return warehouseSymbol;
    }

    // deliver raw materials to warehouse
    public void refill(int amount) {
        // delay simulating the time needed to replenish materials
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            System.out.println("Warehouse " + warehouseSymbol + " interrupted while materials replenishing");
        }

        stock += amount;
        System.out.println("WH " + warehouseSymbol + ": stock-" + stock + ", " + amount + " units of material was delivered");

        // warehouse update in the GUI
        if (warehouseSymbol.equals("A")) {
            gui.updateWarehouseA(stock);
        } else {
            gui.updateWarehouseB(stock);
        }

        if (stock >= threshold) {
            needsSupply = false;
        }
    }

    public boolean isSufficientAmount(int amount) {
        return stock >= amount;
    }

    public void setTruck(SupplyTruck truck) {
        this.truck = truck;
    }

}
