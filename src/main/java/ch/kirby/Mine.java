package ch.kirby;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class Mine {
    public static void main(final String[] args) {
        final String token = System.getenv("DISCORD_CLIENT_TOKEN_MINE");

        if (token == null || token.isBlank()) {
            System.out.println("DISCORD_CLIENT_TOKEN_MINE is not set or is blank.");
            return;
        }

        final GatewayDiscordClient client = DiscordClientBuilder.create(token)
                .build()
                .login()
                .block();

        if (client == null) {
            System.out.println("Login failed. Check your token.");
            return;
        }

        // Register event handler for message create
        client.on(MessageCreateEvent.class, event -> {
            final var message = event.getMessage();

            if ("!ping".equalsIgnoreCase(message.getContent())) {
                return message.getChannel()
                        .flatMap(channel -> channel.createMessage("Pong!"))
                        .then();
            }

            return Mono.empty();
        }).subscribe();

        // Keep bot alive
        client.onDisconnect().block();
    }
}
