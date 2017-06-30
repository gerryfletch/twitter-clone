package me.gerryfletcher.twitter.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Gerry on 30/06/2017.
 */
public class DBHandler {

    private static HikariDataSource ds;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:E:/sqlite/twitter/twitter_db.db");
        config.addDataSourceProperty("dataSourceClassName", "org.sqlite.SQLiteDataSource");
        config.addDataSourceProperty("cachePrepStmts", "true");

        ds = new HikariDataSource(config);
    }

    public static HikariDataSource getDataSource() {
        return ds;
    }

}
