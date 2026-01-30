package ch.kirby.commands;

import ch.kirby.SQL.DBConnection;
import ch.kirby.core.command.ButtonHandler;
import ch.kirby.model.GameStats;
import ch.kirby.service.StatsService;
import ch.kirby.core.command.Command;
import ch.kirby.util.SharedFormatter;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.sql.Connection;
import java.util.List;

import static ch.kirby.util.SharedFormatter.*;

public class LeaderboardCommand implements Command, ButtonHandler {

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
                                .addComponent(defaultStatsComponents(commandPrefix, dayspan, 0L))
                                .build();
                    }
                }).subscribeOn(Schedulers.boundedElastic()))
                .flatMap(event::createFollowup).then();
    }

    @Override
    public String getCommandPrefix() {
        return "leaderboard";
    }

    @Override
    public Mono<Void> handleButton(ButtonInteractionEvent event) {
        int dayspan = Integer.parseInt(event.getCustomId().split("_")[2]);

        return Mono.justOrEmpty(event.getMessage()).flatMap(message -> {
            var embed = message.getEmbeds().get(0);
            var title = embed.getTitle().orElse("");

            String game;
            if (title.contains("Global")) {
                game = null;
            } else if (title.contains("Spotify Listener")) {
                game = "spotify";
            } else {
                game = title.replace("🎮 Leaderboard for ", "");
            }

            var loadingSpec = MessageEditSpec.builder()
                    .embeds(List.of(loadingEmbed()))
                    .components(List.of(disabledStatsComponents("leaderboard", dayspan, 0L)))
                    .build();

            return message.edit(loadingSpec)
                    .then(Mono.fromCallable(() -> {
                        try (Connection conn = new DBConnection().SQLDBConnection()) {
                            StatsService service = new StatsService(conn);
                            return "spotify".equalsIgnoreCase(game)
                                    ? service.getSpotifyLeaderboard(dayspan)
                                    : service.getGameLeaderboard(game, dayspan);
                        }
                    }).subscribeOn(Schedulers.boundedElastic()))
                    .flatMap(leaderboard -> {
                        EmbedCreateSpec newEmbed = formatLeaderboard(leaderboard, game, dayspan);
                        var resultSpec = MessageEditSpec.builder()
                                .embeds(List.of(newEmbed))
                                .components(List.of(defaultStatsComponents("leaderboard", dayspan, 0L)))
                                .build();
                        return message.edit(resultSpec);
                    });
        }).then();
    }
}
