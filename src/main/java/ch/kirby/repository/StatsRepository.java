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
import java.util.Set;
import java.util.stream.Collectors;


public class StatsRepository implements AutoCloseable {
    private final Connection connection;

    public StatsRepository(Connection connection) {
        this.connection = connection;
    }

    public GameStats getStatsForUser(long userId, String displayName, int dayspan) throws SQLException {
        String sql = """
            SELECT
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
                AND U.user_id = ?
            GROUP BY
                App.name
            ORDER BY
                total_hours_played DESC
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, dayspan);
            ps.setLong(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                Map<String, Double> breakdown = new LinkedHashMap<>();
                double total = 0.0;
                while (rs.next()) {
                    String game = rs.getString("App.name");
                    double hours = rs.getDouble("total_hours_played");
                    breakdown.put(game, hours);
                    total += hours;
                }
                return new GameStats(displayName, total, breakdown);
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


    public List<GameStats> fetchServerGameLeaderboard(Set<Long> memberIds, String game, int dayspan) {
        List<GameStats> results = new ArrayList<>();
        if (memberIds.isEmpty()) return results;

        String placeholders = memberIds.stream().map(id -> "?").collect(Collectors.joining(", "));

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
            AND U.user_id IN (%s)
    """.formatted(placeholders));

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
            int paramIndex = 1;
            stmt.setInt(paramIndex++, dayspan);
            for (Long memberId : memberIds) {
                stmt.setLong(paramIndex++, memberId);
            }
            if (hasGame) {
                stmt.setString(paramIndex, game);
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

    public List<GameStats> fetchServerSpotifyLeaderboard(Set<Long> memberIds, int dayspan) {
        List<GameStats> results = new ArrayList<>();
        if (memberIds.isEmpty()) return results;

        String placeholders = memberIds.stream().map(id -> "?").collect(Collectors.joining(", "));

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
            AND U.user_id IN (%s)
        GROUP BY U.username, App.name
        ORDER BY total_hours_played DESC
        LIMIT 10;
    """.formatted(placeholders);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int paramIndex = 1;
            stmt.setInt(paramIndex++, dayspan);
            for (Long memberId : memberIds) {
                stmt.setLong(paramIndex++, memberId);
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

    public List<SpotifyStats> getTopSongsForUser(long userId, String displayName, int dayspan) {
        List<SpotifyStats> results = new ArrayList<>();

        String sql = """
        SELECT
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
            AND U.user_id = ?
            AND App.name = 'Spotify'
        GROUP BY
            AppS.details
        ORDER BY
            total_minutes_played DESC
        LIMIT 10;
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, dayspan);
            stmt.setLong(2, userId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(new SpotifyStats(
                        displayName,
                        rs.getString("details"),
                        rs.getDouble("total_minutes_played")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    public List<SpotifyStats> getTopArtistsForUser(long userId, String displayName, int dayspan) {
        List<SpotifyStats> results = new ArrayList<>();

        String sql = """
        SELECT
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
            AND U.user_id = ?
            AND App.name = 'Spotify'
        GROUP BY
            AppS.state
        ORDER BY
            total_minutes_played DESC
        LIMIT 10;
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, dayspan);
            stmt.setLong(2, userId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(new SpotifyStats(
                        displayName,
                        rs.getString("state"),
                        rs.getDouble("total_minutes_played")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }






    public List<String> searchGameNames(String prefix, int limit) {
        List<String> results = new ArrayList<>();

        String sql = """
            SELECT DISTINCT App.name
            FROM Application App
            JOIN Activity A ON App.auto_app_id = A.auto_app_id
            JOIN Type T ON A.auto_type_id = T.auto_type_id
            WHERE T.type = 'playing'
              AND App.name LIKE ?
            ORDER BY App.name
            LIMIT ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, prefix + "%");
            stmt.setInt(2, limit);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    @Override
    public void close() {}
}