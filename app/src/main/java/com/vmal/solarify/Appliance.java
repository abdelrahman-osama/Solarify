package com.vmal.solarify;

/**
 * Created by markr on 4/30/2017.
 */

public class Appliance {
    String name;
    int value;
    int pos;

    public Appliance(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public Appliance(String name, int value,int pos) {
        this.name = name;
        this.value = value;
        this.pos = pos;
    }
}
