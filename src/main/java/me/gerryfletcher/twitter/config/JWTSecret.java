package me.gerryfletcher.twitter.config;

import javax.crypto.SecretKey;

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
}
