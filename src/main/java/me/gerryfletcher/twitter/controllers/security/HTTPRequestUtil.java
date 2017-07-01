package me.gerryfletcher.twitter.controllers.security;

public class HTTPRequestUtil {
    /**
     * Takes an Authorization header and extracts the JWT.
     *
     * @param auth The full authorization <b>value</b>, e.g: <i>Bearer erahasodh...asdhaoishd...hasodihh</i>
     * @return The JWT
     */
    public static String getJWT(String auth) {
        String[] authArray = auth.split(" ");
        if (authArray.length == 2) {
            return authArray[1];
        } else {
            return null;
        }
    }
}
