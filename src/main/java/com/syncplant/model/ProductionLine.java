package com.syncplant.model;

public class ProductionLine extends Thread {
    private final int lineNumber;
    private final Warehouse warehouseA;
    private final Warehouse warehouseB;
    private int requiredAAmount;    //required amount of raw material A
    private int requiredBAmount;    //required amount of raw material B


    public ProductionLine(int lineNumber, Warehouse warehouseA, Warehouse warehouseB, int requiredAAmount, int requiredBAmount) {
        this.lineNumber = lineNumber;
        this.requiredAAmount = requiredAAmount;
        this.requiredBAmount = requiredBAmount;
        this.warehouseA = warehouseA;
        this.warehouseB = warehouseB;
    }

    @Override
    public void run() {
        while (true) {
            // thread to take raw material A from and warehouse
            Thread takeMaterialA = new Thread(() -> {
                if(!warehouseA.take(requiredAAmount)) {
                    System.out.println("PL " + lineNumber + ": waiting for material A delivery.");
                    while(!warehouseA.isSufficientAmount(requiredAAmount)) {
                        try {
                            synchronized (warehouseA) {
                                warehouseA.wait();
                            }
                        } catch (InterruptedException e) {
                            System.out.println("Production line number " + lineNumber + " interrupted while waiting for material A delivery.");
                            return;
                        }
                    }

                    System.out.println("PL " + lineNumber + ": took " + requiredBAmount + " units of material A");
                }
            });

            // thread to take raw material B from and warehouse
            Thread takeMaterialB = new Thread(() -> {
                if(!warehouseB.take(requiredBAmount)) {
                    System.out.println("PL " + lineNumber + ": waiting for material B delivery.");
                    while(!warehouseB.isSufficientAmount(requiredBAmount)) {
                        try
                        {
                            synchronized (warehouseB) {
                                warehouseB.wait();
                            }
                        } catch (InterruptedException e) {
                            System.out.println("Production line number " + lineNumber + " interrupted while waiting for material B delivery.");
                            return;
                        }
                    }

                    System.out.println("PL " + lineNumber + ": took " + requiredBAmount + "units of material B");
                }
            });

            // start of parallel material taking
            System.out.println("PL " + lineNumber + ": starts taking materials.");
            takeMaterialA.start();
            takeMaterialB.start();

            // wait for material taking
            try {
                takeMaterialA.join();
                takeMaterialB.join();
            } catch (InterruptedException e) {
                System.out.println("Production line number " + lineNumber + " interrupted while waiting for the materials.");
            }

            // producing
            // delay simulating the time needed to produce
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                System.out.println("Production line number " + lineNumber + " interrupted while producing");
            }
            System.out.println("PL " + lineNumber + ": produced product");

        }
    }

}
