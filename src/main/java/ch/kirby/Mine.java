package ch.kirby;

import ch.kirby.event.listeners.ChatInputInteractionEventListener;
import ch.kirby.event.listeners.MessageCreateEventListener;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

import java.io.IOException;
import java.util.List;


public class Mine {
    public static void main(final String[] args) {
        final var token = System.getenv("DISCORD_CLIENT_TOKEN_MINE");
        final GatewayDiscordClient client = DiscordClientBuilder.create(token).build().login().block();

        List<String> commands = List.of("ping.json", "stats.json");
        try {
            new GlobalCommandRegistrar(client.getRestClient()).registerCommands(commands);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        catch (Exception e) {
//            LOGGER.error("Error trying to register global slash commands", e);
//        }

        client.on(ChatInputInteractionEvent.class, ChatInputInteractionEventListener::handle)
                .then(client.onDisconnect())
                .subscribe();
        client.on(MessageCreateEvent.class, MessageCreateEventListener::handle)
                .then(client.onDisconnect())
                .subscribe();

        client.onDisconnect().block();
    }
}
