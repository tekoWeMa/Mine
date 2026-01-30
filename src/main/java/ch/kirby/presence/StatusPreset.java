package ch.kirby.presence;

import discord4j.core.object.presence.ClientActivity;

import java.util.function.Function;

public enum StatusPreset {
    LEADERBOARDS("leaderboards", "Playing", stats -> ClientActivity.playing("preparing the Leaderboards")),
    GOONING("gooning", "Playing", stats -> ClientActivity.playing("Gooning")),
    HELP("help", "Playing", stats -> ClientActivity.playing("/stats /spotify")),
    TRACKED("tracked", "Watching", stats -> ClientActivity.watching(stats.statsCheckedToday() + " stats checked today"));

    private final String name;
    private final String typeName;
    private final Function<BotStats, ClientActivity> activityFactory;

    StatusPreset(String name, String typeName, Function<BotStats, ClientActivity> activityFactory) {
        this.name = name;
        this.typeName = typeName;
        this.activityFactory = activityFactory;
    }

    public String getName() {
        return name;
    }

    public String getTypeName() {
        return typeName;
    }

    public ClientActivity createActivity(BotStats stats) {
        return activityFactory.apply(stats);
    }

    public static StatusPreset fromName(String name) {
        for (StatusPreset preset : values()) {
            if (preset.name.equalsIgnoreCase(name)) {
                return preset;
            }
        }
        return null;
    }

    public record BotStats(long statsCheckedToday) {}
}
