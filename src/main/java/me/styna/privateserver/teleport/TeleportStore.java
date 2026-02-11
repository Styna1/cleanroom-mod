package me.styna.privateserver.teleport;

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
import java.util.Optional;
import java.util.UUID;

public class TeleportStore {

    private final File databaseFile;
    private Connection connection;

    public TeleportStore(File databaseFile) {
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
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS homes (" +
                    "player_uuid TEXT NOT NULL," +
                    "player_name TEXT NOT NULL," +
                    "name TEXT NOT NULL," +
                    "dimension INTEGER NOT NULL," +
                    "x REAL NOT NULL," +
                    "y REAL NOT NULL," +
                    "z REAL NOT NULL," +
                    "yaw REAL NOT NULL," +
                    "pitch REAL NOT NULL," +
                    "created_at INTEGER NOT NULL," +
                    "PRIMARY KEY(player_uuid, name)" +
                    ")");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_homes_owner ON homes(player_uuid)");
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

    public synchronized int countHomes(UUID playerId) throws SQLException {
        String sql = "SELECT COUNT(1) AS cnt FROM homes WHERE player_uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt");
                }
            }
        }
        return 0;
    }

    public synchronized Optional<HomeEntry> getHome(UUID playerId, String name) throws SQLException {
        String normalizedName = normalizeName(name);
        String sql = "SELECT name, dimension, x, y, z, yaw, pitch FROM homes WHERE player_uuid = ? AND name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            statement.setString(2, normalizedName);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    TeleportLocation location = new TeleportLocation(
                            rs.getInt("dimension"),
                            rs.getDouble("x"),
                            rs.getDouble("y"),
                            rs.getDouble("z"),
                            rs.getFloat("yaw"),
                            rs.getFloat("pitch")
                    );
                    return Optional.of(new HomeEntry(rs.getString("name"), location));
                }
            }
        }
        return Optional.empty();
    }

    public synchronized List<HomeEntry> listHomes(UUID playerId) throws SQLException {
        String sql = "SELECT name, dimension, x, y, z, yaw, pitch FROM homes WHERE player_uuid = ? ORDER BY name ASC";
        List<HomeEntry> homes = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    TeleportLocation location = new TeleportLocation(
                            rs.getInt("dimension"),
                            rs.getDouble("x"),
                            rs.getDouble("y"),
                            rs.getDouble("z"),
                            rs.getFloat("yaw"),
                            rs.getFloat("pitch")
                    );
                    homes.add(new HomeEntry(rs.getString("name"), location));
                }
            }
        }
        return homes;
    }

    public synchronized void upsertHome(UUID playerId, String playerName, String name, TeleportLocation location) throws SQLException {
        String normalizedName = normalizeName(name);
        String sql = "INSERT INTO homes(player_uuid, player_name, name, dimension, x, y, z, yaw, pitch, created_at) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT(player_uuid, name) DO UPDATE SET " +
                "player_name = excluded.player_name, " +
                "dimension = excluded.dimension, " +
                "x = excluded.x, y = excluded.y, z = excluded.z, yaw = excluded.yaw, pitch = excluded.pitch";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            statement.setString(2, playerName);
            statement.setString(3, normalizedName);
            statement.setInt(4, location.getDimension());
            statement.setDouble(5, location.getX());
            statement.setDouble(6, location.getY());
            statement.setDouble(7, location.getZ());
            statement.setFloat(8, location.getYaw());
            statement.setFloat(9, location.getPitch());
            statement.setLong(10, System.currentTimeMillis());
            statement.executeUpdate();
        }
    }

    public synchronized boolean deleteHome(UUID playerId, String name) throws SQLException {
        String normalizedName = normalizeName(name);
        String sql = "DELETE FROM homes WHERE player_uuid = ? AND name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            statement.setString(2, normalizedName);
            return statement.executeUpdate() > 0;
        }
    }

    public synchronized boolean renameHome(UUID playerId, String oldName, String newName) throws SQLException {
        String oldNormalized = normalizeName(oldName);
        String newNormalized = normalizeName(newName);
        String sql = "UPDATE homes SET name = ? WHERE player_uuid = ? AND name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newNormalized);
            statement.setString(2, playerId.toString());
            statement.setString(3, oldNormalized);
            return statement.executeUpdate() > 0;
        }
    }

    public static String normalizeName(String name) {
        return name == null || name.trim().isEmpty() ? "home" : name.trim().toLowerCase();
    }
}
