package ch.kirby.repository;

import ch.kirby.model.GameStats;
import ch.kirby.model.SpotifyStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
                Map<String, Double> breakdown = new LinkedHashMap<>();
                double total = 0.0;
                while (rs.next()) {
                    String game = rs.getString("App.name");
                    double hours = rs.getDouble("total_hours_played");
                    breakdown.put(game, hours);
                    total += hours;
                }
                return new GameStats(username, total, breakdown);
            }
        }
    }

    public List<GameStats> fetchGameLeaderboard(String game, int dayspan) {
        List<GameStats> results = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
        SELECT
            U.username,
            App.name,
            ROUND(SUM(TIMESTAMPDIFF(SECOND, A.starttime, COALESCE(A.endtime, NOW()))) / 3600, 2) AS total_hours_played
        FROM
            Activity A
        JOIN User U ON A.auto_user_id = U.auto_user_id
        JOIN Type T ON A.auto_type_id = T.auto_type_id
        JOIN Application App ON A.auto_app_id = App.auto_app_id
        WHERE
            T.type = 'playing'
            AND A.starttime >= NOW() - INTERVAL ? DAY
    """);

        boolean hasGame = game != null && !game.isEmpty();
        if (hasGame) {
            sql.append(" AND App.name = ?");
        }

        sql.append("""
        GROUP BY U.username, App.name
        ORDER BY total_hours_played DESC
        LIMIT 10;
    """);

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            stmt.setInt(1, dayspan);
            if (hasGame) {
                stmt.setString(2, game);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(new GameStats(
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getDouble("total_hours_played")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }


    public List<GameStats> fetchSpotifyLeaderboard(int dayspan) {
        List<GameStats> results = new ArrayList<>();

        String sql = """
        SELECT
            U.username,
            App.name,
            ROUND(SUM(TIMESTAMPDIFF(SECOND, A.starttime, COALESCE(A.endtime, NOW()))) / 3600, 2) AS total_hours_played
        FROM
            Activity A
        JOIN User U ON A.auto_user_id = U.auto_user_id
        JOIN Type T ON A.auto_type_id = T.auto_type_id
        JOIN Application App ON A.auto_app_id = App.auto_app_id
        WHERE
            T.type = 'listening'
            AND A.starttime >= NOW() - INTERVAL ? DAY
            AND App.name = 'Spotify'
        GROUP BY U.username, App.name
        ORDER BY total_hours_played DESC
        LIMIT 10;
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, dayspan);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(new GameStats(
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getDouble("total_hours_played")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    public List<SpotifyStats> getTopSongsForUser(String username, int dayspan) {
        List<SpotifyStats> results = new ArrayList<>();

        String sql = """
        SELECT
            U.username,
            AppS.details,
            ROUND(SUM(TIMESTAMPDIFF(SECOND, A.starttime, COALESCE(A.endtime, NOW()))) / 60, 2) AS total_minutes_played
        FROM
            Activity A
        JOIN User U ON A.auto_user_id = U.auto_user_id
        JOIN Type T ON A.auto_type_id = T.auto_type_id
        JOIN Application App ON A.auto_app_id = App.auto_app_id
        JOIN AppState AppS ON A.auto_app_state_id = AppS.auto_app_state_id
        WHERE
            T.type = 'listening'
            AND A.endtime IS NOT NULL
            AND A.starttime >= NOW() - INTERVAL ? DAY
            AND U.username = ?
            AND App.name = 'Spotify'
        GROUP BY
            AppS.details, U.username
        ORDER BY
            total_minutes_played DESC
        LIMIT 10;
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, dayspan);
            stmt.setString(2, username);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(new SpotifyStats(
                        rs.getString("username"),
                        rs.getString("details"),
                        rs.getDouble("total_minutes_played")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    public List<SpotifyStats> getTopArtistsForUser(String username, int dayspan) {
        List<SpotifyStats> results = new ArrayList<>();

        String sql = """
        SELECT
            U.username,
            AppS.state,
            ROUND(SUM(TIMESTAMPDIFF(SECOND, A.starttime, COALESCE(A.endtime, NOW()))) / 60, 2) AS total_minutes_played
        FROM
            Activity A
        JOIN User U ON A.auto_user_id = U.auto_user_id
        JOIN Type T ON A.auto_type_id = T.auto_type_id
        JOIN Application App ON A.auto_app_id = App.auto_app_id
        JOIN AppState AppS ON A.auto_app_state_id = AppS.auto_app_state_id
        WHERE
            T.type = 'listening'
            AND A.endtime IS NOT NULL
            AND A.starttime >= NOW() - INTERVAL ? DAY
            AND U.username = ?
            AND App.name = 'Spotify'
        GROUP BY
            AppS.state, U.username
        ORDER BY
            total_minutes_played DESC
        LIMIT 10;
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, dayspan);
            stmt.setString(2, username);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(new SpotifyStats(
                        rs.getString("username"),
                        rs.getString("state"),
                        rs.getDouble("total_minutes_played")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }






    @Override
    public void close() {}
}