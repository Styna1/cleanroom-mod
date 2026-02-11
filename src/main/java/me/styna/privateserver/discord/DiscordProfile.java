package me.styna.privateserver.discord;

public class DiscordProfile {
    private final String discordUserId;
    private final String displayName;
    private final String avatarUrl;

    public DiscordProfile(String discordUserId, String displayName, String avatarUrl) {
        this.discordUserId = discordUserId;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
    }

    public String getDiscordUserId() {
        return discordUserId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
