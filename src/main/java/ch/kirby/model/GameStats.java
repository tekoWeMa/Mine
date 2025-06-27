package ch.kirby.model;

import java.util.Map;

public class GameStats {
    private final String username;
    private final Map<String, Double> gameBreakdown;
    private final double totalHours;

    private String gamename;
    private double hoursPlayed;

    public GameStats(String username, double totalHours, Map<String, Double> gameBreakdown) {
        this.username = username;
        this.totalHours = totalHours;
        this.gameBreakdown = gameBreakdown;
    }

    public GameStats(String username, String gamename, double hoursPlayed) {
        this.username = username;
        this.gamename = gamename;
        this.hoursPlayed = hoursPlayed;
        this.totalHours = (int) hoursPlayed;
        this.gameBreakdown = null;
    }

    public String getUsername() { return username; }
    public double getTotalHours() { return totalHours; }
    public Map<String, Double> getGameBreakdown() { return gameBreakdown; }

    public String getGamename() { return gamename; }
    public double getHoursPlayed() { return hoursPlayed; }
}
