package ch.kirby.SQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReadFromSQL implements AutoCloseable {

    private Connection connection;

    public ReadFromSQL(Connection connection) {
        this.connection = connection;
    }
    /**
     * Example method: count the total rows in 'User'.
     * @return a human-readable String of the row count.
     * @throws SQLException if something goes wrong with the query.
     */
    public String readStats() throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM User"; // TODO: form correct statements from table
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int count = rs.getInt("cnt");
                return "Total rows in User: " + count; // TODO: form correct statements from table
            } else {
                return "No data returned.";
            }
        }
    }

    @Override
    public void close() throws Exception {

    }
}
