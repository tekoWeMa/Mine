package ch.kirby.presence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class CommandUsageTracker {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandUsageTracker.class);
    private static final CommandUsageTracker INSTANCE = new CommandUsageTracker();

    private final AtomicLong statsCount = new AtomicLong(0);
    private final AtomicReference<LocalDate> lastResetDate = new AtomicReference<>(LocalDate.now(ZoneId.systemDefault()));

    private CommandUsageTracker() {}

    public static CommandUsageTracker getInstance() {
        return INSTANCE;
    }

    public void incrementStats() {
        checkAndResetIfNewDay();
        statsCount.incrementAndGet();
    }

    public long getStatsCount() {
        checkAndResetIfNewDay();
        return statsCount.get();
    }

    private void checkAndResetIfNewDay() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate lastReset = lastResetDate.get();

        if (!today.equals(lastReset)) {
            if (lastResetDate.compareAndSet(lastReset, today)) {
                long oldCount = statsCount.getAndSet(0);
                LOGGER.info("Daily reset: cleared {} stats from {}", oldCount, lastReset);
            }
        }
    }
}
