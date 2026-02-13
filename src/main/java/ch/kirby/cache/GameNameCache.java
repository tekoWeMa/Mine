package ch.kirby.cache;

import ch.kirby.SQL.DBConnection;
import ch.kirby.service.StatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameNameCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameNameCache.class);
    private static final GameNameCache INSTANCE = new GameNameCache();

    private volatile List<String> gameNames = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private GameNameCache() {}

    public static GameNameCache getInstance() {
        return INSTANCE;
    }

    public void start() {
        refresh();
        scheduler.scheduleAtFixedRate(this::refresh, 10, 10, TimeUnit.MINUTES);
        LOGGER.info("GameNameCache started with {} games", gameNames.size());
    }

    private void refresh() {
        try (Connection conn = new DBConnection().SQLDBConnection()) {
            StatsService service = new StatsService(conn);
            List<String> names = service.searchGameNames("", 10000);
            gameNames = names;
            LOGGER.debug("GameNameCache refreshed: {} games", names.size());
        } catch (Exception e) {
            LOGGER.error("Failed to refresh game name cache", e);
        }
    }

    public List<String> search(String prefix, int limit) {
        String lowerPrefix = prefix.toLowerCase(Locale.ROOT);
        List<String> results = new ArrayList<>();

        for (String name : gameNames) {
            if (name.toLowerCase(Locale.ROOT).startsWith(lowerPrefix)) {
                results.add(name);
                if (results.size() >= limit) {
                    break;
                }
            }
        }
        return results;
    }
}
