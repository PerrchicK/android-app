package com.perrchick.someapplication.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.google.gson.GsonBuilder;

/**
 * POJO stands for "Some Plain Java Object"
 * Created by perrchick on 29/03/2016.
 */
public class SomePojo implements Parcelable {
    private String name;
    private String phoneNumber;
    @Nullable
    private String imageUrl;
    private double latitude;
    private double longitude;

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

    @Nullable
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(@Nullable String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public SomePojo() {
    }

    private SomePojo(Parcel in) {
        this.name = in.readString();
        this.phoneNumber = in.readString();
        this.imageUrl = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
    }

    static public SomePojo createFromJson(String jsonString) {
        SomePojo somePojo = (new GsonBuilder()).create().fromJson(jsonString, SomePojo.class);
        return somePojo;
    }

    public String toJson() {
        return (new GsonBuilder()).create().toJson(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(phoneNumber);
        dest.writeString(imageUrl);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    public static final Creator<SomePojo> CREATOR = new Creator<SomePojo>() {
        @Override
        public SomePojo createFromParcel(Parcel in) {
            return new SomePojo(in);
        }

        @Override
        public SomePojo[] newArray(int size) {
            return new SomePojo[size];
        }
    };
}
