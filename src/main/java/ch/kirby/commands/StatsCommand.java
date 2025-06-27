package ch.kirby.commands;

import ch.kirby.SQL.DBConnection;
import ch.kirby.model.GameStats;
import ch.kirby.service.StatsService;
import ch.kirby.core.command.Command;
import ch.kirby.util.SharedFormatter;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.sql.Connection;

public class StatsCommand implements Command {

    @Override
    public String getName() {
        return "stats";
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

                        StatsService service = new StatsService(conn);
                        GameStats stats = service.getStats(commandUser, targetUser, dayspan);

                        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                                .title("Stats for " + stats.getUsername())
                                .description("Total playtime over the last " + dayspan + " days.")
                                .addField("Total Hours", stats.getTotalHours() + "h", false)
                                .addField("Breakdown", SharedFormatter.formatBreakdown(stats.getGameBreakdown()), false)
                                .build();

                        return InteractionFollowupCreateSpec.builder()
                                .addEmbed(embed)
                                .build();

                    } catch (Exception e) {
                        return InteractionFollowupCreateSpec.builder()
                                .content("⚠️ Error: " + e.getMessage())
                                .build();
                    }
                }).subscribeOn(Schedulers.boundedElastic()))
                .flatMap(event::createFollowup)
                .then();
    }
}
