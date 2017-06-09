package me.gerryfletcher.twitter;

import me.gerryfletcher.twitter.config.CustomConfig;
import me.gerryfletcher.twitter.sqlite.SQLUtils;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;


import java.sql.*;

import java.io.IOException;
import java.net.URI;

/**
 * Main class.
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8080/api";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     *
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.example package
        final ResourceConfig rc = new CustomConfig();

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /**
     * Main method.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        SQLUtils.selectAllFromUsers();

        final HttpServer server = startServer();

        final StaticHttpHandler httpHandler = new StaticHttpHandler("static/");
        server.getServerConfiguration().addHttpHandler(httpHandler, "/");

        System.out.println(String.format("Jersey app started. \nHit enter to stop it...", BASE_URI));
        System.in.read();

        server.stop();
    }
}