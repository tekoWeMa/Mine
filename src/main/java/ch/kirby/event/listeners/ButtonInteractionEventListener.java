package ch.kirby.event.listeners;

import ch.kirby.SQL.DBConnection;
import ch.kirby.commands.LeaderboardCommand;
import ch.kirby.commands.SpotifyCommand;
import ch.kirby.model.GameStats;
import ch.kirby.model.SpotifyStats;
import ch.kirby.service.StatsService;
import ch.kirby.util.SharedFormatter;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static ch.kirby.util.SharedFormatter.*;

public class ButtonInteractionEventListener {

    public static Mono<Void> handle(ButtonInteractionEvent event) {
        String[] parts = event.getCustomId().split("_");
        if (parts.length < 3) {
            return event.reply("Invalid button ID").withEphemeral(true);
        }

        String commandPrefix = parts[0]; // "stats", "leaderboard", "spotify"
        String key = parts[1];           // "dayspan"
        int dayspan = Integer.parseInt(parts[2]);

        if (!"dayspan".equals(key)) {
            return event.reply("Unsupported button key: " + key).withEphemeral(true);
        }

        Mono<Void> inner = switch (commandPrefix) {
            case "stats" -> handleStatsDayspan(event, dayspan);
//            case "leaderboard" -> handleLeaderboardDayspan(event, dayspan);TODO Implement Leaderboarddayspan
//            case "spotify" -> handleSpotifyDayspan(event, dayspan); TODO Implement SpotifyDayspan
            default -> event.reply("Unsupported command: " + commandPrefix).withEphemeral(true);
        };

        return event.deferEdit().timeout(Duration.ofMinutes(5)).then(inner).then();
    }

    private static Mono<Void> handleStatsDayspan(ButtonInteractionEvent event, int dayspan) {
        return Mono.defer(() -> event.getMessage().map(message -> {
            var embed = message.getEmbeds().get(0);
            var title = embed.getTitle().orElse("");
            var requestedUsername = title.replace("Stats for ", "").trim();

            GameStats stats;
            try {
                stats = getStats(requestedUsername, dayspan);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            var messageEditSpec = MessageEditSpec.builder()
                    .embeds(defaultStatsEmbed(stats, dayspan))
                    .addComponent(defaultStatsComponents("stats", dayspan))
                    .build();

            return message.edit(messageEditSpec).then();
        }).get());
    }
//    private static Mono<Void> handleLeaderboardDayspan(ButtonInteractionEvent event, int dayspan) {
//       TODO
//    }

//    private static Mono<Void> handleSpotifyDayspan(ButtonInteractionEvent event, int dayspan) {
//       TODO
//    }

    private static GameStats getStats(String username, int dayspan) throws SQLException {
        try (Connection conn = new DBConnection().SQLDBConnection()) {
            StatsService service = new StatsService(conn);
            return service.getStats(username, dayspan);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
