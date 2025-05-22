package com.syncplant.model;

import com.syncplant.Main;
import javafx.application.Platform;
import java.util.concurrent.ThreadLocalRandom;

public class ProductionLine implements Runnable {
    private final int lineNumber;
    private final Warehouse warehouseA;
    private final Warehouse warehouseB;
    private int requiredAAmount;    //required amount of raw material A
    private int requiredBAmount;    //required amount of raw material B
    private OutboundWarehouse outboundWarehouse;
    private Main gui;


    public ProductionLine(int lineNumber, Warehouse warehouseA, Warehouse warehouseB, int requiredAAmount, int requiredBAmount, OutboundWarehouse outboundWarehouse, Main gui) {
        this.lineNumber = lineNumber;
        this.requiredAAmount = requiredAAmount;
        this.requiredBAmount = requiredBAmount;
        this.warehouseA = warehouseA;
        this.warehouseB = warehouseB;
        this.outboundWarehouse = outboundWarehouse;
        this.gui = gui;
    }

    @Override
    public void run() {
        while (true) {
            //CountDownLatch materialsTaken = new CountDownLatch(2);

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

            // show in GUI info about taking materials
            Platform.runLater(() -> gui.updateLineStatus(lineNumber, "taking " + requiredAAmount + "xA, " + requiredBAmount + "xB"));
            long messageStart = System.currentTimeMillis();

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

            // check if the second elapsed
            long elapsed = System.currentTimeMillis() - messageStart;
            if (elapsed < 1000) {
                try {
                    Thread.sleep(1000 - elapsed);
                } catch (InterruptedException e) {
                    System.out.println("PL " + lineNumber + " interrupted during message delay.");
                    continue;
                }
            }

            // producing
            Platform.runLater(() -> gui.updateLineStatus(lineNumber, "producing"));

            // delay simulating random time needed to produce
            try {
                int sleepTime = ThreadLocalRandom.current().nextInt(500, 2000);
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                System.out.println("Production line number " + lineNumber + " interrupted while producing");
            }
            System.out.println("PL " + lineNumber + ": produced product");

            // product produced - notify the GUI
            Platform.runLater(() -> {
                gui.updateLineStatus(lineNumber, "produced product");
            });

            // make product icon which goes to the outbound warehouse
            gui.makeProductIcon(lineNumber);

            // storing products in the outbound warehouse
            outboundWarehouse.store();


        }
    }

}
