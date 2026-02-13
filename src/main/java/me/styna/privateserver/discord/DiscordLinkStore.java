package me.styna.privateserver.discord;

import me.styna.privateserver.db.SqliteSupport;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.UUID;

public class DiscordLinkStore {

    private final File databaseFile;
    private Connection connection;

    public DiscordLinkStore(File databaseFile) {
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
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS discord_links (" +
                    "player_uuid TEXT PRIMARY KEY," +
                    "discord_user_id TEXT NOT NULL," +
                    "display_name TEXT NOT NULL," +
                    "avatar_url TEXT," +
                    "linked_at INTEGER NOT NULL" +
                    ")");
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

    public synchronized void upsert(UUID playerId, DiscordProfile profile) throws SQLException {
        String sql = "INSERT INTO discord_links(player_uuid, discord_user_id, display_name, avatar_url, linked_at) VALUES(?, ?, ?, ?, ?) " +
                "ON CONFLICT(player_uuid) DO UPDATE SET " +
                "discord_user_id = excluded.discord_user_id, " +
                "display_name = excluded.display_name, " +
                "avatar_url = excluded.avatar_url, " +
                "linked_at = excluded.linked_at";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            statement.setString(2, profile.getDiscordUserId());
            statement.setString(3, profile.getDisplayName());
            statement.setString(4, profile.getAvatarUrl());
            statement.setLong(5, System.currentTimeMillis());
            statement.executeUpdate();
        }
    }

    public synchronized Optional<DiscordProfile> get(UUID playerId) throws SQLException {
        String sql = "SELECT discord_user_id, display_name, avatar_url FROM discord_links WHERE player_uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new DiscordProfile(
                            rs.getString("discord_user_id"),
                            rs.getString("display_name"),
                            rs.getString("avatar_url")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    public synchronized boolean remove(UUID playerId) throws SQLException {
        String sql = "DELETE FROM discord_links WHERE player_uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            return statement.executeUpdate() > 0;
        }
    }
}
