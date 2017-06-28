package me.gerryfletcher.twitter.models;

import me.gerryfletcher.twitter.exceptions.BadDataException;

/**
 * A DisplayName can be any characters, between the length of 3 and 15.
 */
public class DisplayName {

    public static boolean isDisplayNameValid(String displayName) {
        return (displayName.length() < 3 || displayName.length() > 15);
    }

}
