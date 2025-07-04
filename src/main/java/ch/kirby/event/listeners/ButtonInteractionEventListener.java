package ch.kirby.event.listeners;

import ch.kirby.SQL.DBConnection;
import ch.kirby.model.GameStats;
import ch.kirby.service.StatsService;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.spec.MessageEditSpec;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;

import static ch.kirby.util.SharedFormatter.defaultStatsComponents;
import static ch.kirby.util.SharedFormatter.defaultStatsEmbed;

public class ButtonInteractionEventListener {

    public static Mono<Void> handle(ButtonInteractionEvent event) {
        if (!event.getCustomId().startsWith("stats_dayspan_")) {
            throw new UnsupportedOperationException("Not implemented");
        }

        var inner = Mono.defer(() -> event.getMessage().map(
                message -> {

                    var parts = event.getCustomId().split("_");
                    var part = parts[parts.length - 1];
                    var dayspan = Integer.parseInt(part);  // This can panic but we dont care right now!

                    GameStats stats = null;
                    try {
                        stats = getStats(event, dayspan);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    var messageEditSpec = MessageEditSpec.builder()
                            .embeds(defaultStatsEmbed(stats, dayspan))
                            .addComponent(defaultStatsComponents(dayspan))
                            .build();
                    return message.edit(messageEditSpec).then();
                }
        ).get()
        );

        return event.deferEdit().timeout(Duration.ofMinutes(5)).then(Mono.defer(() -> inner)).then();
    }

    private static GameStats getStats(ButtonInteractionEvent event, int dayspan) throws SQLException {
        try (Connection conn = new DBConnection().SQLDBConnection()) {
            StatsService service = new StatsService(conn);
            User commandUser = event.getInteraction().getUser();
            return service.getStats(commandUser.getUsername(), dayspan);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
