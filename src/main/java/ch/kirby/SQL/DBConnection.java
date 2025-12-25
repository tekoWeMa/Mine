package ch.kirby.SQL;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DBConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBConnection.class);
    private static final HikariDataSource dataSource;

    static {
        String dbHost = System.getenv("DB_HOST_MINE");
        String dbUsername = System.getenv("DB_USERNAME_MINE");
        String dbPassword = System.getenv("DB_PASSWORD_MINE");
        String dbName = "sugu";
        int dbPort = 3306;

        String connectionUrl = String.format("jdbc:mariadb://%s:%d/%s", dbHost, dbPort, dbName);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(connectionUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        dataSource = new HikariDataSource(config);
        LOGGER.info("HikariCP connection pool initialized");
    }

    public Connection SQLDBConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            LOGGER.error("Error getting connection from pool", e);
            return null;
        }
    }
}
