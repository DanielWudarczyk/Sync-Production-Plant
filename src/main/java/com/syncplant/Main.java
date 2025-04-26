package com.syncplant;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("SyncProductionPlant is staring...");

        Map<String, Object> config = ConfigLoader.loadConfig();
        System.out.println("Loaded config:");
        System.out.println(config);
    }
}
