package ch.kirby;

import ch.kirby.cache.GameNameCache;
import ch.kirby.event.listeners.AutocompleteEventListener;
import ch.kirby.event.listeners.ButtonInteractionEventListener;
import ch.kirby.event.listeners.ChatInputInteractionEventListener;
import ch.kirby.event.listeners.MessageCreateEventListener;
import ch.kirby.presence.BotPresenceManager;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.gateway.intent.IntentSet;
import discord4j.gateway.intent.Intent;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;


public class Mine {
    private static final Logger LOGGER = LoggerFactory.getLogger(Mine.class);
    public static void main(final String[] args) {
        final var token = System.getenv("DISCORD_CLIENT_TOKEN_MINE");
        final GatewayDiscordClient client = DiscordClientBuilder.create(token).build()
                .gateway()
                .setEnabledIntents(IntentSet.nonPrivileged().or(IntentSet.of(Intent.GUILD_MEMBERS)))
                .login()
                .block();

        BotPresenceManager presenceManager = new BotPresenceManager(client);
        presenceManager.start();
        ChatInputInteractionEventListener.initialize(presenceManager);

        GameNameCache.getInstance().start();

        List<String> commands = List.of(
                "ping.json",
                "stats.json",
                "leaderboard.json",
                "serverleaderboard.json",
                "spotify.json",
                "status.json");
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
        client.on(ChatInputAutoCompleteEvent.class, AutocompleteEventListener::handle)
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
