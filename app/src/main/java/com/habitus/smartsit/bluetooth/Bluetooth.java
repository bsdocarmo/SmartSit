package com.habitus.smartsit.bluetooth;

/**
 * Created by Arthur on 2/6/2018.
 */

public class Bluetooth {
    private String name;
    private String MAC;
    private String UUID;

    public Bluetooth(String name, String mac, String uuid) {
        this.name = name;
        this.MAC = mac;
        this.UUID = uuid;
    }


    public String getMAC() {
        return this.MAC;
    }

    public String getName() {
        return this.name;
    }

    public String getUUID() {
        return this.UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }
}
