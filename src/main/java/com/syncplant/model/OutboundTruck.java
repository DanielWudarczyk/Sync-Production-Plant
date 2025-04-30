package com.syncplant.model;

import java.util.concurrent.ThreadLocalRandom;

public class OutboundTruck extends Thread{
    OutboundWarehouse outboundWarehouse;

    public OutboundTruck(OutboundWarehouse outboundWarehouse) {
        this.outboundWarehouse = outboundWarehouse;
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
