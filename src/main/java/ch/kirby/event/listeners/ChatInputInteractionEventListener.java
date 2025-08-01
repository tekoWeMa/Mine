package ch.kirby.event.listeners;

import ch.kirby.commands.LeaderboardCommand;
import ch.kirby.commands.PingCommand;
import ch.kirby.commands.SpotifyCommand;
import ch.kirby.commands.StatsCommand;
import ch.kirby.core.command.Command;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class ChatInputInteractionEventListener {

    private final static List<Command> commands = new ArrayList<>();

    static {
        commands.add(new PingCommand());
        commands.add(new StatsCommand());
        commands.add(new LeaderboardCommand());
        commands.add(new SpotifyCommand());
    }

    public static Mono<Void> handle(ChatInputInteractionEvent event) {
        return Flux.fromIterable(commands)
                .filter(command -> command.getName().equals(event.getCommandName()))
                .next()
                .flatMap(command -> command.handle(event));
    }
}
