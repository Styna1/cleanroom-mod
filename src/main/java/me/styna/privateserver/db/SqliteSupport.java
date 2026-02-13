package me.styna.privateserver.db;

import java.sql.SQLException;

public final class SqliteSupport {

    private SqliteSupport() {
    }

    public static void ensureDriverLoaded() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver is missing from the runtime classpath", e);
        }
    }
}
