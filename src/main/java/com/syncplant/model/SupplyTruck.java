package com.syncplant.model;

import com.syncplant.Main;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class SupplyTruck extends Thread {
    private final Warehouse[] warehouses;
    private final int deliveryAmount;
    private final ProductionLine[] productionLines;
    private Main gui;

    public SupplyTruck(Warehouse[] warehouses, int deliveryAmount, ProductionLine[] productionLines, Main gui) {
        this.warehouses = warehouses;
        this.deliveryAmount = deliveryAmount;
        this.productionLines = productionLines;
        this.gui = gui;
    }

    @Override
    public void run() {
        // deliver raw materials to warehouses
        while (true) {
            AtomicBoolean delivered = new AtomicBoolean(false);
            for (Warehouse warehouse : warehouses) {
                if (warehouse.needsRefill()) {
                        Semaphore deliveryDone = new Semaphore(0);

                        gui.animateDelivery(warehouse.getSymbol(),
                                () -> {
                                    //callback after delivery
                                    warehouse.refill(deliveryAmount);
                                    System.out.println("ST: delivered " + deliveryAmount + " units of materials to warehouse " + warehouse.getSymbol());
                                    delivered.set(true);
                                    synchronized (warehouse) {
                                        warehouse.notifyAll();
                                    }
                                },
                                () -> {
                                    //callback after return of truck
                                    deliveryDone.release();
                                });

                        try {
                            deliveryDone.acquire();
                        } catch (InterruptedException e) {
                            System.out.println("Supply truck interrupted during delivery animation");
                            return;
                        }
                }
            }

            // wait for warehouses demand
            if (!delivered.get()) {
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
