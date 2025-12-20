package ch.kirby;

import ch.kirby.event.listeners.ButtonInteractionEventListener;
import ch.kirby.event.listeners.ChatInputInteractionEventListener;
import ch.kirby.event.listeners.MessageCreateEventListener;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;


public class Mine {
    private static final Logger LOGGER = LoggerFactory.getLogger(Mine.class);
    public static void main(final String[] args) {
        final var token = System.getenv("DISCORD_CLIENT_TOKEN_MINE");
        final GatewayDiscordClient client = DiscordClientBuilder.create(token).build().login().block();

        List<String> commands = List.of(
                "ping.json",
                "stats.json",
                "leaderboard.json",
                "spotify.json");
        try {
            new GlobalCommandRegistrar(client.getRestClient()).registerCommands(commands);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        catch (Exception e) {
            LOGGER.error("Error trying to register global slash commands", e);
        }

        client.on(ChatInputInteractionEvent.class, ChatInputInteractionEventListener::handle)
                .then(client.onDisconnect())
                .subscribe();
        client.on(MessageCreateEvent.class, MessageCreateEventListener::handle)
                .then(client.onDisconnect())
                .subscribe();
        client.on(ButtonInteractionEvent.class, ButtonInteractionEventListener::handle)
                .then(client.onDisconnect())
                .subscribe();



        client.onDisconnect().block();
    }
}
