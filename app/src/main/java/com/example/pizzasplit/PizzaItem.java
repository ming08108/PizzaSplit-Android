package com.example.pizzasplit;

/**
 * Created by a on 2/7/15.
 */
public class PizzaItem {


    /**
     * Item Id
     */
    @com.google.gson.annotations.SerializedName("id")
    public String mId;


    @com.google.gson.annotations.SerializedName("type")
    public String type;

    @com.google.gson.annotations.SerializedName("brand")
    public String brand;

    @com.google.gson.annotations.SerializedName("time")
    public int time;

    @com.google.gson.annotations.SerializedName("lat")
    public double lat;

    @com.google.gson.annotations.SerializedName("lon")
    public double lon;

    @com.google.gson.annotations.SerializedName("userId")
    public String userId;


    @Override
    public String toString(){
        return type + " " + brand + " " + time;
    }

}
