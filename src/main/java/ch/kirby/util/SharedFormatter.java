package ch.kirby.util;

import ch.kirby.model.GameStats;
import discord4j.core.spec.EmbedCreateSpec;

import java.util.List;
import java.util.Map;

import ch.kirby.model.SpotifyStats;


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

    public static EmbedCreateSpec formatSpotifyStats(String username, List<SpotifyStats> songs, List<SpotifyStats> artists, int dayspan) {
        StringBuilder songSection = new StringBuilder();
        int rank = 1;
        for (SpotifyStats song : songs) {
            songSection.append(String.format("`#%02d` %-30s: `%,.2f min`\n",
                    rank++, truncate(song.getValue(), 30), song.getMinutesPlayed()));
        }

        StringBuilder artistSection = new StringBuilder();
        rank = 1;
        for (SpotifyStats artist : artists) {
            artistSection.append(String.format("`#%02d` %-30s: `%,.2f min`\n",
                    rank++, truncate(artist.getValue(), 30), artist.getMinutesPlayed()));
        }

        return EmbedCreateSpec.builder()
                .title("🎧 Spotify Stats for " + username)
                .addField("🎵 Top Songs", songSection.length() > 0 ? songSection.toString() : "No data found", false)
                .addField("🎤 Top Artists", artistSection.length() > 0 ? artistSection.toString() : "No data found", false)
                .footer("Timespan: " + dayspan + " days", null)
                .build();
    }

    private static String truncate(String text, int maxLength) {
        return (text.length() <= maxLength) ? text : text.substring(0, maxLength - 1) + "…";
    }



}
