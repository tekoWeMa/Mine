package ch.kirby.model;

public class SpotifyStats {
    private final String username;
    private final String value; // either artist or song title
    private final double minutesPlayed;

    public SpotifyStats(String username, String value, double minutesPlayed) {
        this.username = username;
        this.value = value;
        this.minutesPlayed = minutesPlayed;
    }

    public String getUsername() {
        return username;
    }

    public String getValue() {
        return value;
    }

    public double getMinutesPlayed() {
        return minutesPlayed;
    }
}
