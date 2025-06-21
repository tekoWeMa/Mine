package ch.kirby.commands;

import ch.kirby.core.command.Command;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public class StatsCommand implements Command {
    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        return event.reply()
                .withEphemeral(false) // Empheral is secret or not secret
                .withContent("Will be implemented!");
    }
}
