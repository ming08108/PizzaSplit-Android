package com.example.pizzasplit;

/**
 * Created by a on 2/7/15.
 */
public class LatLong {

    @com.google.gson.annotations.SerializedName("lat")
    public double lat;

    @com.google.gson.annotations.SerializedName("lon")
    public double lon;

    public LatLong(double lat, double lon){
        this.lat = lat;
        this.lon = lon;
    }

}
