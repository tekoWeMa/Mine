package ch.kirby.model;

import java.util.Map;

public class GameStats {
    private final String username;
    private final int totalHours;
    private final Map<String, Integer> gameBreakdown;

    public GameStats(String username, int totalHours, Map<String, Integer> gameBreakdown) {
        this.username = username;
        this.totalHours = totalHours;
        this.gameBreakdown = gameBreakdown;
    }

    public String getUsername() { return username; }
    public int getTotalHours() { return totalHours; }
    public Map<String, Integer> getGameBreakdown() { return gameBreakdown; }
}
