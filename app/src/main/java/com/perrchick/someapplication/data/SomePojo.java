package com.perrchick.someapplication.data;

import com.google.gson.GsonBuilder;

import java.util.Date;

/**
 * Created by perrchick on 29/03/2016.
 */

public class SomePojo {
    String name;
    String phoneNumber;
    double longitude;
    double latitude;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public SomePojo() {
    }

    static public SomePojo createFromJson(String jsonString) {
        SomePojo somePojo = (new GsonBuilder()).create().fromJson(jsonString, SomePojo.class);
        return somePojo;
    }

    public String toJson() {
        return (new GsonBuilder()).create().toJson(this);
    }
}
