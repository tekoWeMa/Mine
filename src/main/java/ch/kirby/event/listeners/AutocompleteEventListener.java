package ch.kirby.event.listeners;

import ch.kirby.cache.GameNameCache;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AutocompleteEventListener {

    private static final Set<String> SUPPORTED_COMMANDS = Set.of("leaderboard", "serverleaderboard");

    public static Mono<Void> handle(ChatInputAutoCompleteEvent event) {
        if (!SUPPORTED_COMMANDS.contains(event.getCommandName())) {
            return Mono.empty();
        }

        String focusedOption = event.getFocusedOption().getName();
        if (!"game".equals(focusedOption)) {
            return Mono.empty();
        }

        String typed = event.getFocusedOption().getValue()
                .map(v -> v.asString())
                .orElse("");

        List<String> games = GameNameCache.getInstance().search(typed, 24);
        List<ApplicationCommandOptionChoiceData> suggestions = new ArrayList<>();

        if ("spotify".startsWith(typed.toLowerCase()) || typed.isEmpty()) {
            suggestions.add(ApplicationCommandOptionChoiceData.builder()
                    .name("spotify").value("spotify").build());
        }

        for (String game : games) {
            suggestions.add(ApplicationCommandOptionChoiceData.builder()
                    .name(game).value(game).build());
        }

        return event.respondWithSuggestions(suggestions)
                .onErrorResume(e -> e.getMessage() != null && e.getMessage().contains("Unknown interaction"),
                        e -> Mono.empty());
    }
}
