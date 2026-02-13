package me.styna.privateserver.economy;

import me.styna.privateserver.db.SqliteSupport;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EconomyStore {

    private final File databaseFile;
    private Connection connection;

    public EconomyStore(File databaseFile) {
        this.databaseFile = databaseFile;
    }

    public synchronized void initialize() throws SQLException {
        if (databaseFile.getParentFile() != null && !databaseFile.getParentFile().exists()) {
            //noinspection ResultOfMethodCallIgnored
            databaseFile.getParentFile().mkdirs();
        }

        SqliteSupport.ensureDriverLoaded();
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS economy_accounts (" +
                    "player_uuid TEXT PRIMARY KEY," +
                    "player_name TEXT NOT NULL," +
                    "balance REAL NOT NULL DEFAULT 0" +
                    ")");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_economy_balance ON economy_accounts(balance DESC)");
        }
    }

    public synchronized void close() {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ignored) {
            // No-op
        }
        connection = null;
    }

    public synchronized double getOrCreateBalance(UUID playerId, String playerName, double defaultBalance) throws SQLException {
        ensureAccount(playerId, playerName, defaultBalance);
        String sql = "SELECT balance FROM economy_accounts WHERE player_uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        }
        return defaultBalance;
    }

    public synchronized double setBalance(UUID playerId, String playerName, double newBalance, double defaultBalance) throws SQLException {
        ensureAccount(playerId, playerName, defaultBalance);
        String sql = "UPDATE economy_accounts SET balance = ?, player_name = ? WHERE player_uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, Math.max(0.0D, newBalance));
            statement.setString(2, playerName);
            statement.setString(3, playerId.toString());
            statement.executeUpdate();
        }
        return getOrCreateBalance(playerId, playerName, defaultBalance);
    }

    public synchronized List<EconomyRow> topBalances(int limit) throws SQLException {
        String sql = "SELECT player_name, balance FROM economy_accounts ORDER BY balance DESC LIMIT ?";
        List<EconomyRow> rows = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Math.max(1, limit));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(new EconomyRow(rs.getString("player_name"), rs.getDouble("balance")));
                }
            }
        }
        return rows;
    }

    private void ensureAccount(UUID playerId, String playerName, double defaultBalance) throws SQLException {
        String sql = "INSERT OR IGNORE INTO economy_accounts(player_uuid, player_name, balance) VALUES(?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            statement.setString(2, playerName);
            statement.setDouble(3, Math.max(0.0D, defaultBalance));
            statement.executeUpdate();
        }

        String updateNameSql = "UPDATE economy_accounts SET player_name = ? WHERE player_uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(updateNameSql)) {
            statement.setString(1, playerName);
            statement.setString(2, playerId.toString());
            statement.executeUpdate();
        }
    }

    public static final class EconomyRow {
        private final String playerName;
        private final double balance;

        public EconomyRow(String playerName, double balance) {
            this.playerName = playerName;
            this.balance = balance;
        }

        public String getPlayerName() {
            return playerName;
        }

        public double getBalance() {
            return balance;
        }
    }
}
