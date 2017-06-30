package me.gerryfletcher.twitter.models;


import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Password {

    private static final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,20})";
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
    private static int workload = 12;

    /* Passwords must:
        1) Contain one digit from 0-9
        2) Contain one lowercase character
        3) Contain one uppercase character
        4) Be at least 8 characters long, and less than 20
    */

    /**
     * @param password_plainText Unmodified plaintext password.
     * @return Salted password (using BCrypt)
     */
    public static String hashPassword(String password_plainText) {
        String salt = BCrypt.gensalt(workload);
        return BCrypt.hashpw(password_plainText, salt);
    }

    /**
     * @param password_plaintext Unmodified plaintext password
     * @param stored_hash        The existing hashed password
     * @return True/False if the passwords match
     */
    public static boolean checkPassword(String password_plaintext, String stored_hash) {
        boolean password_verified;

        if (null == stored_hash || !stored_hash.startsWith("$2a$"))
            throw new java.lang.IllegalArgumentException("Invalid hash provided for comparison");

        password_verified = BCrypt.checkpw(password_plaintext, stored_hash);

        return (password_verified);
    }

    public static boolean isPasswordValid(String password) {
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}
