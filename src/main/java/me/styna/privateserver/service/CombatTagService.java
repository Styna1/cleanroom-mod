package me.styna.privateserver.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatTagService {

    private final Map<UUID, Long> taggedUntilTick = new ConcurrentHashMap<>();

    public void tag(UUID playerId, int seconds, long currentTick) {
        if (seconds <= 0) {
            return;
        }
        long until = currentTick + (seconds * 20L);
        taggedUntilTick.put(playerId, until);
    }

    public boolean isTagged(UUID playerId, long currentTick) {
        long remaining = remainingTicks(playerId, currentTick);
        return remaining > 0L;
    }

    public long remainingSeconds(UUID playerId, long currentTick) {
        long ticks = remainingTicks(playerId, currentTick);
        if (ticks <= 0) {
            return 0L;
        }
        return (long) Math.ceil(ticks / 20.0D);
    }

    private long remainingTicks(UUID playerId, long currentTick) {
        Long until = taggedUntilTick.get(playerId);
        if (until == null) {
            return 0L;
        }
        long remaining = until - currentTick;
        if (remaining <= 0L) {
            taggedUntilTick.remove(playerId);
            return 0L;
        }
        return remaining;
    }
}
