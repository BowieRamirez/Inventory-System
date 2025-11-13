package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBManager - central place for obtaining JDBC connections.
 *
 * Connection order of precedence:
 * 1) Environment variables: DB_URL, DB_USER, DB_PASSWORD
 * 2) Properties file: src/database/data/db.properties (keys: url,user,password)
 */
public class DBManager {
    private static String url;
    private static String user;
    private static String password;
    private static boolean initialized = false;

    private static void init() {
        if (initialized) return;
        initialized = true;
        // 1) env vars
        url = System.getenv("DB_URL");
        user = System.getenv("DB_USER");
        password = System.getenv("DB_PASSWORD");

        if (url != null && !url.isBlank()) {
            return;
        }
        // 2) properties file
        Properties p = new Properties();
        try (FileInputStream fis = new FileInputStream("src/database/data/db.properties")) {
            p.load(fis);
            url = p.getProperty("url", url);
            user = p.getProperty("user", user);
            password = p.getProperty("password", password);
        } catch (IOException ignored) {
            // No properties file; remain null
        }
    }

    public static boolean isConfigured() {
        init();
        return url != null && !url.isBlank();
    }

    public static Connection getConnection() throws SQLException {
        init();
        if (!isConfigured()) {
            throw new SQLException("Database not configured. Set DB_URL/DB_USER/DB_PASSWORD or create db.properties");
        }
        return DriverManager.getConnection(url, user, password);
    }
}
