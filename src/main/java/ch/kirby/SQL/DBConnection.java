package ch.kirby.SQL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    public java.sql.Connection SQLDBConnection() {
        final var dbHost = System.getenv("DB_HOST_MINE"); // DB Host, e.g., "localhost" or an IP address
        final var dbUsername = System.getenv("DB_USERNAME_MINE"); // DB Username
        final var dbPassword = System.getenv("DB_PASSWORD_MINE"); // DB Password

        String dbName = "sugu";
        int dbPort = 3306; // Default MariaDB port
        // Construct the connection URL
        String connectionUrl = String.format("jdbc:mariadb://%s:%d/%s", dbHost, dbPort, dbName);

        try {
            // Establish and return the database connection
            return DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
        } catch (SQLException e) {
            LOGGER.error("Error trying Establish DB Connection", e);
            e.printStackTrace();
            return null;
        }
    }
}