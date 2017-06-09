package me.gerryfletcher.twitter.resources.AccountFunctions;


import org.springframework.security.crypto.bcrypt.BCrypt;

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
}
