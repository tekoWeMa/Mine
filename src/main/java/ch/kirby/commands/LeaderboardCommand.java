package ch.kirby.commands;

import ch.kirby.SQL.DBConnection;
import ch.kirby.model.GameStats;
import ch.kirby.service.StatsService;
import ch.kirby.core.command.Command;
import ch.kirby.util.SharedFormatter;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.sql.Connection;
import java.util.List;

import static ch.kirby.util.SharedFormatter.defaultStatsComponents;

public class LeaderboardCommand implements Command {

    @Override
    public String getName() {
        return "leaderboard";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        return event.deferReply()
                .then(Mono.fromCallable(() -> {
                    try (Connection conn = new DBConnection().SQLDBConnection()) {
                        StatsService service = new StatsService(conn);

                        String game = event.getOption("game")
                                .flatMap(opt -> opt.getValue())
                                .map(v -> v.getRaw())
                                .orElse(null);

                        int dayspan = event.getOption("dayspan")
                                .flatMap(opt -> opt.getValue())
                                .map(v -> Integer.parseInt(v.getRaw()))
                                .orElse(7);

                        List<GameStats> leaderboard = "spotify".equalsIgnoreCase(game)
                                ? service.getSpotifyLeaderboard(dayspan)
                                : service.getGameLeaderboard(game, dayspan);
                        String commandPrefix = "leaderboard";
                        EmbedCreateSpec embed = SharedFormatter.formatLeaderboard(leaderboard, game, dayspan);
                        return InteractionFollowupCreateSpec.builder()
                                .addEmbed(embed)
//                                .addComponent(defaultStatsComponents(commandPrefix, dayspan))
                                .build();
                    }
                }).subscribeOn(Schedulers.boundedElastic()))
                .flatMap(event::createFollowup).then();
    }
}
