package com.syncplant.model;

import com.syncplant.Main;

import java.util.concurrent.ThreadLocalRandom;

public class OutboundTruck extends Thread{
    OutboundWarehouse outboundWarehouse;
    private Main gui;

    public OutboundTruck(OutboundWarehouse outboundWarehouse, Main gui) {
        this.outboundWarehouse = outboundWarehouse;
        this.gui = gui;
    }

    @Override
    public void run() {
        while (true) {
            // take packages at random times
            try {
                int sleepTime = ThreadLocalRandom.current().nextInt(12000, 20000);
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                System.out.println("Outbound truck interrupted while waiting for next picking up packages");
            }

            gui.takePackages(() -> outboundWarehouse.take());
        }
    }
}
