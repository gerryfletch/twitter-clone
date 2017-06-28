package me.gerryfletcher.twitter.models;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Handle {

    /* Usernames must:
        1) Match characters and symbols in the list: a-z, A-Z, 0-9, underscore, hyphen
        2) Be at least 3 characters long, and a maximum of 15
    */
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_-]{3,15}$";

    private static Pattern pattern;
    private static Matcher matcher;

    public static boolean isHandleValid(String username) {
        pattern = Pattern.compile(USERNAME_PATTERN);
        matcher = pattern.matcher(username);

        return matcher.matches();
    }


}
