package me.gerryfletcher.twitter.resources.AccountFunctions;


import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Gerry on 10/06/2017.
 */
public class Password {

    private static int workload = 12;

    public static String hashPassword(String password_plainText) {
        String salt = BCrypt.gensalt(workload);
        String hashed_password = BCrypt.hashpw(password_plainText, salt);

        return(hashed_password);
    }

    public static boolean checkPassword(String password_plaintext, String stored_hash) {
        boolean password_verified = false;

        if(null == stored_hash || !stored_hash.startsWith("$2a$"))
            throw new java.lang.IllegalArgumentException("Invalid hash provided for comparison");

        password_verified = BCrypt.checkpw(password_plaintext, stored_hash);

        return(password_verified);
    }

    /* Passwords must:
        1) Contain one digit from 0-9
        2) Contain one lowercase character
        3) Contain one uppercase character
        4) Be at least 8 characters long, and less than 20
    */

    private static final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,20})";

    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    public static boolean isPasswordValid(String password) {
        Matcher matcher = pattern.matcher(password);

        if(! matcher.matches()) {
            return false;
        }

        return true;
    }
}
