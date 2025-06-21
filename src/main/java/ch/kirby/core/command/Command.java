package ch.kirby.core.command;
import ch.kirby.core.reaction.Reaction;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
public interface Command extends Reaction<ChatInputInteractionEvent> {
    String getName();
}
