package me.gerryfletcher.twitter.controllers.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JWTSecret implements SecretKey{

    private static final String key = "xpxopjj1gp%%08*@+xt1m&8ypg1n8emp@jt5f5d)okev=nz)jo(%k8gerreh";

    @Override
    public String getAlgorithm() {
        return "HMAC256";
    }

    @Override
    public String getFormat() {
        return "HS256";
    }

    @Override
    public byte[] getEncoded() {
        return null;
    }

    public static String getKey() {
        return key;
    }

    /**
     * Checks if the JWT is valid.
     * @param token The JSON Web token
     * @return true/false
     */
    public boolean validateToken(String token) {
        try{
            Algorithm algorithm = Algorithm.HMAC256(key);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("auth0")
                    .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);
            return true;
        }  catch (UnsupportedEncodingException exception) {
            System.out.println("UTF-8 encoding not supported.");
            return false;
        } catch (JWTVerificationException exception) {
            System.out.println("Invalid signature/claims");
            return false;
        }
    }

    /**
     * Generates a salted JW token with user id and role included.
     * @param userId Integer unique user ID
     * @param role   User/Admin etc
     * @return       The token
     */
    public String generateToken(int userId, String handle, String role) {
        try {
            long now = System.currentTimeMillis();
            Algorithm algorithm = Algorithm.HMAC256(key);
            return JWT.create()
                    .withIssuer("auth0")
                    .withClaim("uid", userId) //ID from DB
                    .withClaim("handle", handle)
                    .withClaim("role", role) //role from DB  (alt: permission int? )
                    .withExpiresAt(new Date( now + TimeUnit.HOURS.toMillis(2)))
                    .sign(algorithm);
        } catch (UnsupportedEncodingException exception) {
            System.out.println("UTF 8 encoding not supported.");
        } catch (JWTCreationException exception) {
            System.out.println("Invalid signature configuration.");
        }

        return null;
    }

    public Map<String, Claim> getClaims(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaims();
        } catch (JWTDecodeException e) {
            System.out.println("Invalid token.");
            return null;
        }
    }

    public Claim getClaim(String token, String claim) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(claim);
        } catch (JWTDecodeException e) {
            System.out.println("Invalid token.");
            return null;
        }
    }
}
