package me.gerryfletcher.twitter.config;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * The Custom Config loaded. Includes the HTTP filter on startup.
 */
public class CustomConfig  extends ResourceConfig{
    public CustomConfig() {
        packages("me.gerryfletcher.twitter");
        register(LoggingFilter.class);

        // Auth Filter registration
        register(AuthenticationFilter.class);
    }
}
