package me.gerryfletcher.twitter.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Gerry on 09/06/2017.
 */
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

    public boolean validateToken(String token) {
        try{
            Algorithm algorithm = Algorithm.HMAC256(this.key);
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

    public String generateToken(int userId, String role) {
        try {
            long now = System.currentTimeMillis();
            Algorithm algorithm = Algorithm.HMAC256(this.key);
            String token = JWT.create()
                    .withIssuer("auth0")
                    .withClaim("uid", userId) //ID from DB
                    .withClaim("role", role) //role from DB  (alt: permission int? )
                    .withExpiresAt(new Date( now + TimeUnit.HOURS.toMillis(2)))
                    .sign(algorithm);
            return token;
        } catch (UnsupportedEncodingException exception) {
            System.out.println("UTF 8 encoding not supported.");
        } catch (JWTCreationException exception) {
            System.out.println("Invalid signature configuration.");
        }

        return null;
    }

    public String getClaim(String token, String claim) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(claim).asString();
        } catch (JWTDecodeException e) {
            System.out.println("Invalid token.");
            return null;
        }
    }
}
