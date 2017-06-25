package me.gerryfletcher.twitter.controllers.sqlite;

import me.gerryfletcher.twitter.controllers.user.Password;

import java.sql.*;

/**
 * Created by Gerry on 09/06/2017.
 */
public class SQLUtils {

    public static Connection connect() {
        String url = "jdbc:sqlite:E:/sqlite/twitter/twitter_db.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return conn;
    }

    public static void createTable(String tableName) {

        //String deleteTable = "DROP TABLE [IF EXISTS] " + tableName + ";";

        // SQL Statement to create users table
        String createTable = "CREATE TABLE IF NOT EXISTS " + tableName +" (\n"
                + " id integer PRIMARY KEY AUTOINCREMENT,\n"
                + " handle varchar NOT NULL,\n"
                + " display_name varchar NOT NULL,\n"
                + " email varchar NOT NULL,\n"
                + " password CHAR(60) NOT NULL,\n"
                + " role varchar NOT NULL"
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

        String sql = "INSERT INTO users(handle, display_name, email, password, role) VALUES(?,?,?,?,?)";

        try (Connection conn = connect();
             PreparedStatement st = conn.prepareStatement(sql)) {
            for(int i = 0; i < 10; i++) {
                String role = "UserResource";
                String handle = names[i].toLowerCase();
                String display_name = "_" + names[i];
                String email = names[i].toLowerCase() + "@gmail.com";
                int id = i + 1;
                String password = "Passw0rd" + id;
                String hashedPassword = Password.hashPassword(password);

                if(i == 3) {
                    role = "Admin";
                }

                st.setString(1, handle);
                st.setString(2, display_name);
                st.setString(3, email);
                st.setString(4, hashedPassword);
                st.setString(5, role);
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
                        + rs.getString("handle")
                        + "  |  "
                        + rs.getString("display_name")
                        + "  |  "
                        + rs.getString("email")
                        + "  |  "
                        + rs.getString("password")
                        + "  |  "
                        + rs.getString("role"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
