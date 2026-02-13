package me.styna.privateserver.economy;

import me.styna.privateserver.api.economy.EconomyApi;
import me.styna.privateserver.config.ConfigService;
import net.minecraft.entity.player.EntityPlayerMP;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EconomyService implements EconomyApi {

    private final ConfigService configService;
    private final EconomyStore store;

    public EconomyService(ConfigService configService, File databaseFile) {
        this.configService = configService;
        this.store = new EconomyStore(databaseFile);
    }

    public void initialize() throws SQLException {
        store.initialize();
    }

    public void shutdown() {
        store.close();
    }

    public boolean isEnabled() {
        return configService.getMainConfig().modules.economy && configService.getEconomyConfig().enabled;
    }

    public double getBalance(EntityPlayerMP player) {
        return getBalance(player.getUniqueID(), player.getName());
    }

    @Override
    public double getBalance(UUID playerId, String playerName) {
        try {
            return store.getOrCreateBalance(playerId, playerName, configService.getEconomyConfig().startingBalance);
        } catch (SQLException e) {
            throw new RuntimeException("Could not read balance", e);
        }
    }

    @Override
    public double setBalance(UUID playerId, String playerName, double newBalance) {
        try {
            return store.setBalance(playerId, playerName, Math.max(0.0D, newBalance), configService.getEconomyConfig().startingBalance);
        } catch (SQLException e) {
            throw new RuntimeException("Could not set balance", e);
        }
    }

    @Override
    public double deposit(UUID playerId, String playerName, double amount) {
        double current = getBalance(playerId, playerName);
        return setBalance(playerId, playerName, current + Math.max(0.0D, amount));
    }

    @Override
    public double withdraw(UUID playerId, String playerName, double amount) {
        double current = getBalance(playerId, playerName);
        return setBalance(playerId, playerName, Math.max(0.0D, current - Math.max(0.0D, amount)));
    }

    @Override
    public List<LeaderEntry> getTopBalances(int limit) {
        int actualLimit = Math.max(1, Math.min(limit, 50));
        try {
            List<EconomyStore.EconomyRow> rows = store.topBalances(actualLimit);
            List<LeaderEntry> entries = new ArrayList<>(rows.size());
            for (EconomyStore.EconomyRow row : rows) {
                entries.add(new LeaderEntry(row.getPlayerName(), row.getBalance()));
            }
            return entries;
        } catch (SQLException e) {
            throw new RuntimeException("Could not read baltop", e);
        }
    }
}
