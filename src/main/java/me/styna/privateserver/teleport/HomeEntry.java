package me.styna.privateserver.teleport;

public class HomeEntry {
    private final String name;
    private final TeleportLocation location;

    public HomeEntry(String name, TeleportLocation location) {
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public TeleportLocation getLocation() {
        return location;
    }
}
