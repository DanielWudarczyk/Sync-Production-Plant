package com.syncplant.model;

public class Warehouse {
    private String name;
    private int stock;
    private int threshold;
    private boolean needsSupply;
    private SupplyTruck truck;

    public Warehouse(String name, int initialStock, int threshold, SupplyTruck truck) {
        this.name = name;
        stock = initialStock;
        this.threshold = threshold;
        needsSupply = false;
        this.truck = truck;
    }

    public synchronized boolean take(int amount) {
        if (amount <= stock) {
            stock -= amount;
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

    public String getName() {
        return name;
    }

    public void refill(int amount) {
        stock += amount;
        if (stock >= threshold) {
            needsSupply = false;
        }
    }

}
