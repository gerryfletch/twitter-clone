package me.gerryfletcher.twitter;

import me.gerryfletcher.twitter.config.CustomConfig;
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

        connectToDatabase();
        createTable();
        selectAllFromTable();

        final HttpServer server = startServer();

        final StaticHttpHandler httpHandler = new StaticHttpHandler("static/");
        server.getServerConfiguration().addHttpHandler(httpHandler, "/");

        System.out.println(String.format("Jersey app started. \nHit enter to stop it...", BASE_URI));
        System.in.read();
        server.stop();
    }

    private static void connectToDatabase() {
        Connection conn = connect();
        System.out.println("Connected to DB.");

        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static Connection connect() {
        String url = "jdbc:sqlite:E:/sqlite:memory";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }

    private static void createTable() {
        // SQL Statement to create users table
        String query = "CREATE TABLE IF NOT EXISTS users (\n"
                + " id integer PRIMARY KEY AUTOINCREMENT,\n"
                + " name varchar NOT NULL,\n"
                + " password varchar NOT NULL\n"
                + ");";

        try (Connection conn = connect();
            Statement st = conn.createStatement()) {

            st.execute(query);
            System.out.println("New table `users` created.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void selectAllFromTable() {
        String sql = "SELECT * FROM users";
        try (Connection conn = connect();
             Statement st = conn.createStatement()){

            ResultSet rs = st.executeQuery(sql);

            System.out.println(rs.getMetaData());

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}