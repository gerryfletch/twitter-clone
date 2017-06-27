package me.gerryfletcher.twitter.models;

import com.google.gson.annotations.SerializedName;

/**
 *  The User class models user data for quick access.
 *  The User class can be provided by the UserService
 *  singleton. It is created from a JsonObject, so
 *  the SerializedName's are important.
 */
public class User{

    private int uid;
    private String handle;

    @SerializedName("display_name")
    private String displayName;

    @SerializedName("profile_picture")
    private String profilePicture;

    private String bio;

    @SerializedName("number_of_tweets")
    private int numberOfTweets;

    @SerializedName("number_of_followers")
    private int numberOfFollowers;

    @SerializedName("number_of_following")
    private int numberOfFollowing;

    User() {}

    public int getId() { return uid; }

    public String getHandle() { return handle; }
    public String getDisplayName() { return displayName; }
    public String getProfilePicture() { return profilePicture; }
    public String getBio() { return bio; }

    public int getNumberOfTweets() { return numberOfTweets; }
    public int getNumberOfFollowers() { return numberOfFollowers; }
    public int getNumberOfFollowing() { return numberOfFollowing; }

}
