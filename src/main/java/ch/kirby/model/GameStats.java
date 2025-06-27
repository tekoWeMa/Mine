package ch.kirby.model;

import java.util.Map;

public class GameStats {
    private final String username;
    private final int totalHours;
    private final Map<String, Integer> gameBreakdown;
    private String gamename;
    private double hoursPlayed;

    public GameStats(String username, int totalHours, Map<String, Integer> gameBreakdown) {
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
    public int getTotalHours() { return totalHours; }
    public Map<String, Integer> getGameBreakdown() { return gameBreakdown; }

    public String getGamename() { return gamename; }
    public double getHoursPlayed() { return hoursPlayed; }
}
