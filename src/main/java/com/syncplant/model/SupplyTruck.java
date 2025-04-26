package com.syncplant.model;

public class SupplyTruck extends Thread {
    private final Warehouse[] warehouses;
    private final int deliveryAmount;

    public SupplyTruck(Warehouse[] warehouses, int deliveryAmount) {
        this.warehouses = warehouses;
        this.deliveryAmount = deliveryAmount;
    }

    @Override
    public void run() {
        while (true) {
            boolean delivered = false;
            for (Warehouse warehouse : warehouses) {
                if (warehouse.needsRefill()) {
                    synchronized (warehouse) {
                        warehouse.refill(deliveryAmount);
                        System.out.println("Supply truck delivered raw materials to " + warehouse.getName());
                        delivered = true;
                    }
                }
            }
            if (!delivered) {
                try {
                    synchronized (warehouses) {
                        warehouses.wait();
                    }
                } catch (InterruptedException e) {
                    System.out.println("Supply truck interrupted");
                    return;
                }
            }
        }
    }

    public void notifyTruck() {
        synchronized (warehouses) {
            warehouses.notifyAll();
        }
    }
}
