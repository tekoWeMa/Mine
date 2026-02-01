package ch.kirby.service;

import ch.kirby.model.GameStats;
import ch.kirby.repository.StatsRepository;
import discord4j.core.object.entity.User;

import java.sql.Connection;
import java.util.List;
import java.util.Set;

import ch.kirby.model.SpotifyStats;


public class StatsService {

    private final StatsRepository repo;

    public StatsService(Connection connection) {
        this.repo = new StatsRepository(connection);
    }

    public GameStats getStats(User commandUser, User target, int dayspan) throws Exception {
        User user = (target != null) ? target : commandUser;
        long userId = user.getId().asLong();
        String displayName = user.getUsername();
        return repo.getStatsForUser(userId, displayName, dayspan);
    }

    public GameStats getStats(long userId, String displayName, int dayspan) throws Exception {
        return repo.getStatsForUser(userId, displayName, dayspan);
    }

    public List<GameStats> getGameLeaderboard(String game, int dayspan) {
        return repo.fetchGameLeaderboard(game, dayspan);
    }

    public List<GameStats> getSpotifyLeaderboard(int dayspan) {
        return repo.fetchSpotifyLeaderboard(dayspan);
    }

    public List<SpotifyStats> getTopSongsForUser(long userId, String displayName, int dayspan) {
        return repo.getTopSongsForUser(userId, displayName, dayspan);
    }

    public List<SpotifyStats> getTopArtistsForUser(long userId, String displayName, int dayspan) {
        return repo.getTopArtistsForUser(userId, displayName, dayspan);
    }

    public List<GameStats> getServerGameLeaderboard(Set<Long> memberIds, String game, int dayspan) {
        return repo.fetchServerGameLeaderboard(memberIds, game, dayspan);
    }

    public List<GameStats> getServerSpotifyLeaderboard(Set<Long> memberIds, int dayspan) {
        return repo.fetchServerSpotifyLeaderboard(memberIds, dayspan);
    }
}
