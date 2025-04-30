package com.syncplant.model;

public class SupplyTruck extends Thread {
    private final Warehouse[] warehouses;
    private final int deliveryAmount;
    private final ProductionLine[] productionLines;

    public SupplyTruck(Warehouse[] warehouses, int deliveryAmount, ProductionLine[] productionLines) {
        this.warehouses = warehouses;
        this.deliveryAmount = deliveryAmount;
        this.productionLines = productionLines;
    }

    @Override
    public void run() {
        // deliver raw materials to warehouses
        while (true) {
            boolean delivered = false;
            for (Warehouse warehouse : warehouses) {
                if (warehouse.needsRefill()) {
                    synchronized (warehouse) {
                        // delay simulating the time needed to deliver materials
                        try {
                            Thread.sleep(1200);
                        } catch (InterruptedException e) {
                            System.out.println("Supply truck interrupted while delivering");
                        }
                        System.out.println("ST: delivered " + deliveryAmount + " units of materials to warehouse " + warehouse.getSymbol());
                        warehouse.refill(deliveryAmount);
                        delivered = true;
                        warehouse.notifyAll();
                    }
                }
            }

            // wait for warehouses demand
            if (!delivered) {
                try {
                    synchronized (warehouses) {
                        System.out.println("ST: waiting for the warehouses demand");
                        warehouses.wait();
                    }
                } catch (InterruptedException e) {
                    System.out.println("Supply truck interrupted while waiting for the warehouses demand");
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
