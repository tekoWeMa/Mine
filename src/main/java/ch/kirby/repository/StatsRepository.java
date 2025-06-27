package ch.kirby.repository;

import ch.kirby.model.GameStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class StatsRepository implements AutoCloseable {
    private final Connection connection;

    public StatsRepository(Connection connection) {
        this.connection = connection;
    }

    public GameStats getStatsForUser(String username, int dayspan) throws SQLException {
        String sql = """
            SELECT
                U.username,
                App.name,
                ROUND(SUM(TIMESTAMPDIFF(SECOND, A.starttime, COALESCE(A.endtime, NOW()))) / 3600, 2) AS total_hours_played
            FROM
                Activity A
            JOIN
                User U ON A.auto_user_id = U.auto_user_id
            JOIN
                Type T ON A.auto_type_id = T.auto_type_id
            JOIN
                Application App ON A.auto_app_id = App.auto_app_id
            WHERE
                T.type = 'playing'
                AND A.starttime >= NOW() - INTERVAL ? DAY
                AND U.username = ?
            GROUP BY
                U.username, App.name
            ORDER BY
                total_hours_played DESC
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, dayspan);
            ps.setString(2, username);

            try (ResultSet rs = ps.executeQuery()) {
                Map<String, Integer> breakdown = new LinkedHashMap<>();
                double total = 0.0;
                while (rs.next()) {
                    String game = rs.getString("App.name");
                    double hours = rs.getDouble("total_hours_played");
                    breakdown.put(game, (int) Math.round(hours));
                    total += hours;
                }
                return new GameStats(username, (int) Math.round(total), breakdown);
            }
        }
    }

    @Override
    public void close() {}
}