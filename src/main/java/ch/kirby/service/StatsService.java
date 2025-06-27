package ch.kirby.service;

import ch.kirby.model.GameStats;
import ch.kirby.repository.StatsRepository;
import discord4j.core.object.entity.User;

import java.sql.Connection;

public class StatsService {

    private final StatsRepository repo;

    public StatsService(Connection connection) {
        this.repo = new StatsRepository(connection);
    }

    public GameStats getStats(User commandUser, User target, int dayspan) throws Exception {
        String username = (target != null) ? target.getUsername() : commandUser.getUsername();
        return repo.getStatsForUser(username, dayspan);
    }
}
