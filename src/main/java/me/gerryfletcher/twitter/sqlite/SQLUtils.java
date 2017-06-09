package me.gerryfletcher.twitter.sqlite;

import java.sql.*;

/**
 * Created by Gerry on 09/06/2017.
 */
public class SQLUtils {

    public static void connectToDatabase() {
        Connection conn = SQLUtils.connect();

        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static Connection connect() {
        String url = "jdbc:sqlite:E:/sqlite/temp";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
            System.out.println("DB connected.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }

    public static void createTable(String tableName) {

        //String deleteTable = "DROP TABLE [IF EXISTS] " + tableName + ";";

        // SQL Statement to create users table
        String createTable = "CREATE TABLE IF NOT EXISTS " + tableName +" (\n"
                + " id integer PRIMARY KEY AUTOINCREMENT,\n"
                + " username varchar NOT NULL,\n"
                + " password varchar NOT NULL\n"
                + ");";

        try (Connection conn = connect();
             Statement st = conn.createStatement()) {

            //st.execute(deleteTable);
            st.execute(createTable);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public static void populateUsersTable() {
        String[] names = {"Gerry", "Lauren", "Cathy", "Rob", "Ruben", "Istannen", "Bosco", "Ben", "Dan", "Lucy"};

        String sql = "INSERT INTO users(username,password) VALUES(?,?)";

        try (Connection conn = connect();
             PreparedStatement st = conn.prepareStatement(sql)) {
            for(int i = 0; i < 10; i++) {
                String username = names[i].toLowerCase();
                int id = i + 1;
                String password = "password" + id;

                st.setString(1, username);
                st.setString(2, password);
                st.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void selectAllFromUsers() {
        String sql = "SELECT * FROM users";
        try (Connection conn = SQLUtils.connect();
             Statement st = conn.createStatement()){

            ResultSet rs = st.executeQuery(sql);

            while(rs.next()) {
                System.out.println(rs.getInt("id") + "  |  "
                        + rs.getString("username")
                        + "  |  "
                        + rs.getString("password"));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
