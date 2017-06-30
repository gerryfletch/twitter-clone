package me.gerryfletcher.twitter.controllers.security;

import me.gerryfletcher.twitter.exceptions.BadDataException;

/**
 * Created by Gerry on 25/06/2017.
 */
public class HTTPRequestUtil {
    /**
     * Takes an Authorization header and extracts the JWT.
     *
     * @param auth The full authorization <b>value</b>, e.g: <i>Bearer erahasodh...asdhaoishd...hasodihh</i>
     * @return The JWT
     */
    public static String getJWT(String auth) throws BadDataException {
        String[] authArray = auth.split(" ");
        if (authArray.length == 2) {
            return authArray[1];
        }

        throw new BadDataException("Badly formatted authorization header.");
    }
}
