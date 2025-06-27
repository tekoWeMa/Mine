package ch.kirby.util;

import ch.kirby.model.GameStats;
import discord4j.core.spec.EmbedCreateSpec;

import java.util.List;
import java.util.Map;

public class SharedFormatter {

    public static String formatBreakdown(Map<String, Integer> breakdown) {
        StringBuilder sb = new StringBuilder();
        breakdown.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue())) // Optional: sort descending
                .forEach(entry -> {
                    String game = entry.getKey();
                    double hours = entry.getValue();
                    sb.append(String.format("• %-20s: `%,.2f h`\n", game, hours));
                });
        return sb.toString();
    }

    public static EmbedCreateSpec formatLeaderboard(List<GameStats> stats, String game, int dayspan) {
        String title = (game == null)
                ? "🏆 Global Game Leaderboard"
                : ("spotify".equalsIgnoreCase(game)
                ? "🎧 Spotify Listener Leaderboard"
                : "🎮 Leaderboard for " + game);

        StringBuilder description = new StringBuilder();
        int rank = 1;
        for (GameStats stat : stats) {
            description.append(String.format(
                    "`#%02d` **%s** - %s: `%.2f hours`\n",
                    rank++, stat.getUsername(), stat.getGamename(), stat.getHoursPlayed()
            ));
        }

        return EmbedCreateSpec.builder()
                .title(title)
                .description(description.toString())
                .footer("Timespan: " + dayspan + " days", null)
                .build();
    }

}
