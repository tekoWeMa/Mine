package ch.kirby.commands;

import ch.kirby.SQL.DBConnection;
import ch.kirby.model.GameStats;
import ch.kirby.service.StatsService;
import ch.kirby.core.command.Command;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
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

                        StringBuilder sb = new StringBuilder("\uD83D\uDCCA Stats for **" + stats.getUsername() + "** in last " + dayspan + " days:\n");
                        sb.append("Total hours: ").append(stats.getTotalHours()).append("\n");
                        stats.getGameBreakdown().forEach((game, hrs) -> {
                            sb.append("• ").append(game).append(": ").append(hrs).append("h\n");
                        });

                        return sb.toString();

                    } catch (Exception e) {
                        return "\u26A0\uFE0F Error: " + e.getMessage();
                    }
                }).subscribeOn(Schedulers.boundedElastic()))
                .flatMap(result -> event.createFollowup().withContent(result))
                .then();
    }
}