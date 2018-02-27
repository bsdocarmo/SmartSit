package com.habitus.smartsit.singleton;

/**
 * Created by bruno on 2/26/2018.
 */

class sensor {
    private static final sensor ourInstance = new sensor();

    private sensor() {
    }

    static sensor getInstance() {
        return ourInstance;
    }
}
