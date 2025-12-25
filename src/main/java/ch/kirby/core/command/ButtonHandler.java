package ch.kirby.core.command;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import reactor.core.publisher.Mono;

public interface ButtonHandler {
    String getCommandPrefix();
    Mono<Void> handleButton(ButtonInteractionEvent event);
}
