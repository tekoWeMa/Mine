package ch.kirby.service;

import ch.kirby.model.GameStats;
import ch.kirby.repository.StatsRepository;
import discord4j.core.object.entity.User;

import java.sql.Connection;
import java.util.List;

public class StatsService {

    private final StatsRepository repo;

    public StatsService(Connection connection) {
        this.repo = new StatsRepository(connection);
    }

    public GameStats getStats(User commandUser, User target, int dayspan) throws Exception {
        String username = (target != null) ? target.getUsername() : commandUser.getUsername();
        return repo.getStatsForUser(username, dayspan);
    }

    public List<GameStats> getGameLeaderboard(String game, int dayspan) {
        return repo.fetchGameLeaderboard(game, dayspan);
    }

    public List<GameStats> getSpotifyLeaderboard(int dayspan) {
        return repo.fetchSpotifyLeaderboard(dayspan);
    }

}
