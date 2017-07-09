package me.gerryfletcher.twitter.models;

public class Tweet {
    public static boolean isTweetValid(String tweet) {
        return (tweet.length() > 0 && tweet.length() < 240);
    }
}
