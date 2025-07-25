package ch.kirby.commands;

import ch.kirby.SQL.DBConnection;
import ch.kirby.core.command.Command;
import ch.kirby.model.SpotifyStats;
import ch.kirby.service.StatsService;
import ch.kirby.util.SharedFormatter;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.sql.Connection;
import java.util.List;

public class SpotifyCommand implements Command {

    @Override
    public String getName() {
        return "spotify";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        return event.deferReply()
                .then(Mono.fromCallable(() -> {
                    try (Connection conn = new DBConnection().SQLDBConnection()) {
                        User commandUser = event.getInteraction().getUser();
                        User targetUser = event.getOption("user")
                                .flatMap(opt -> opt.getValue().map(v -> v.asUser().block()))
                                .orElse(null);

                        int dayspan = event.getOption("dayspan")
                                .flatMap(opt -> opt.getValue())
                                .map(v -> Integer.parseInt(v.getRaw()))
                                .orElse(7);

                        String username = (targetUser != null)
                                ? targetUser.getUsername()
                                : commandUser.getUsername();

                        StatsService service = new StatsService(conn);
                        List<SpotifyStats> topSongs = service.getTopSongsForUser(username, dayspan);
                        List<SpotifyStats> topArtists = service.getTopArtistsForUser(username, dayspan);

                        EmbedCreateSpec embed = SharedFormatter.formatSpotifyStats(username, topSongs, topArtists, dayspan);
                        return InteractionFollowupCreateSpec.builder().addEmbed(embed).build();
                    }
                }).subscribeOn(Schedulers.boundedElastic()))
                .flatMap(event::createFollowup).then();
    }
}
