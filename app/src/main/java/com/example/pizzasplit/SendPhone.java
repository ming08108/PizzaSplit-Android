package com.example.pizzasplit;

/**
 * Created by a on 2/8/15.
 */
public class SendPhone {

    public SendPhone(String id, String phone){
        this.listerId = id;
        this.number = phone;
    }


    @com.google.gson.annotations.SerializedName("listerId")
    public String listerId;

    @com.google.gson.annotations.SerializedName("number")
    public String number;

}
