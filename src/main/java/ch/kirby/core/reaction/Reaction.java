package ch.kirby.core.reaction;

import reactor.core.publisher.Mono;

public interface Reaction<TEvent> {
    Mono<Void> handle(TEvent event);
}
