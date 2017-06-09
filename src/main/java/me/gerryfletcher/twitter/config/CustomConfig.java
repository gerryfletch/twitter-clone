package me.gerryfletcher.twitter.config;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Created by Gerry on 08/06/2017.
 */
public class CustomConfig  extends ResourceConfig{
    public CustomConfig() {
        packages("me.gerryfletcher.twitter");
        register(LoggingFilter.class);

        // Auth Filter registration
        register(AuthenticationFilter.class);
    }
}
