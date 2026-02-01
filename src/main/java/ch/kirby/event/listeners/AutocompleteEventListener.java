package ch.kirby.event.listeners;

import ch.kirby.SQL.DBConnection;
import ch.kirby.service.StatsService;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.sql.Connection;
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

        return Mono.fromCallable(() -> {
            try (Connection conn = new DBConnection().SQLDBConnection()) {
                StatsService service = new StatsService(conn);
                List<String> games = service.searchGameNames(typed, 24);

                List<ApplicationCommandOptionChoiceData> suggestions = new ArrayList<>();

                if ("spotify".startsWith(typed.toLowerCase()) || typed.isEmpty()) {
                    suggestions.add(ApplicationCommandOptionChoiceData.builder()
                            .name("spotify").value("spotify").build());
                }

                for (String game : games) {
                    suggestions.add(ApplicationCommandOptionChoiceData.builder()
                            .name(game).value(game).build());
                }

                return suggestions;
            }
        }).subscribeOn(Schedulers.boundedElastic())
          .flatMap(event::respondWithSuggestions);
    }
}
