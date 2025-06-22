package ch.kirby.commands;

import ch.kirby.SQL.DBConnection;
import ch.kirby.SQL.ReadFromSQL;
import ch.kirby.core.command.Command;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
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
        // 1) Tell Discord we need more time
        return event.deferReply()
                // 2) Offload blocking DB call onto boundedElastic
                .then(Mono.fromCallable(() -> {
                                    try (
                                            // open your JDBC connection
                                            Connection conn = new DBConnection().SQLDBConnection();
                                            // wrap it in helper
                                            ReadFromSQL reader = new ReadFromSQL(conn)
                                    ) {
                                        // run the query
                                        return reader.readStats();
                                    } catch (Exception e) {
                                        // return an error message instead of throwing
                                        return "⚠️ Error reading stats: " + e.getMessage();
                                    }
                                })
                                .subscribeOn(Schedulers.boundedElastic())
                                // 3) once complete, edit the deferred reply with the result
                                .flatMap(event::editReply)
                ).then();
    }
}