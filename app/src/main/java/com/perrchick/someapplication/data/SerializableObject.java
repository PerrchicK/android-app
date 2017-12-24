package com.perrchick.someapplication.data;

import java.io.Serializable;

/**
 * Created by perrchick on 26/11/2017.
 */

public class SerializableObject implements Serializable {
    private int x;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }
}
