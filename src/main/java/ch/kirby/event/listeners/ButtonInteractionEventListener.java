package ch.kirby.event.listeners;

import ch.kirby.commands.LeaderboardCommand;
import ch.kirby.commands.ServerLeaderboardCommand;
import ch.kirby.commands.SpotifyCommand;
import ch.kirby.commands.StatsCommand;
import ch.kirby.core.command.ButtonHandler;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ButtonInteractionEventListener {

    private static final Map<String, ButtonHandler> handlers = List.of(
            new StatsCommand(),
            new LeaderboardCommand(),
            new ServerLeaderboardCommand(),
            new SpotifyCommand()
    ).stream().collect(Collectors.toMap(ButtonHandler::getCommandPrefix, Function.identity()));

    public static Mono<Void> handle(ButtonInteractionEvent event) {
        String[] parts = event.getCustomId().split("_");
        if (parts.length < 3) {
            return event.reply("Invalid button ID").withEphemeral(true);
        }

        String commandPrefix = parts[0];
        String key = parts[1];

        if (!"dayspan".equals(key)) {
            return event.reply("Unsupported button key: " + key).withEphemeral(true);
        }

        ButtonHandler handler = handlers.get(commandPrefix);
        if (handler == null) {
            return event.reply("Unsupported command: " + commandPrefix).withEphemeral(true);
        }

        return event.deferEdit()
                .timeout(Duration.ofMinutes(5))
                .then(handler.handleButton(event))
                .then();
    }
}
