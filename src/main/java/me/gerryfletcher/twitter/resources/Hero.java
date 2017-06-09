package me.gerryfletcher.twitter.resources;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Gerry on 05/06/2017.
 */
public class Hero {

    @SerializedName("name")
    private String name;

    @SerializedName("id")
    private int id;

    @SerializedName("superPower")
    private String superPower;

    Hero(String name, String superPower, int id) {
        this.name = name;
        this.superPower = superPower;
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public String getSuperPower() {
        return this.superPower;
    }

    public int getId() {
        return this.id;
    }

}
