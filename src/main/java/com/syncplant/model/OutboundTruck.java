package com.syncplant.model;

import com.syncplant.gui.MainApp;

import java.util.concurrent.ThreadLocalRandom;

public class OutboundTruck extends Thread{
    OutboundWarehouse outboundWarehouse;
    private MainApp gui;

    public OutboundTruck(OutboundWarehouse outboundWarehouse, MainApp gui) {
        this.outboundWarehouse = outboundWarehouse;
        this.gui = gui;
    }

    @Override
    public void run() {
        while (true) {
            // take packages at random times
            try {
                int sleepTime = ThreadLocalRandom.current().nextInt(3000, 6000);
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                System.out.println("Outbound truck interrupted while waiting for next picking up packages");
            }

            outboundWarehouse.take();
        }
    }
}
