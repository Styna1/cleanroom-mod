package me.styna.privateserver.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.styna.privateserver.PrivateServer;
import me.styna.privateserver.config.ConfigService;
import me.styna.privateserver.discord.DiscordLinkStore;
import me.styna.privateserver.discord.DiscordProfile;
import net.minecraft.entity.player.EntityPlayerMP;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiscordRelayService {

    private final ConfigService configService;
    private final DiscordLinkStore linkStore;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "privateserver-discord-relay");
        thread.setDaemon(true);
        return thread;
    });
    private final Gson gson = new Gson();

    public DiscordRelayService(ConfigService configService, File databaseFile) {
        this.configService = configService;
        this.linkStore = new DiscordLinkStore(databaseFile);
    }

    public void initialize() throws SQLException {
        linkStore.initialize();
    }

    public void shutdown() {
        linkStore.close();
        executor.shutdownNow();
    }

    public void relay(String eventType, String message) {
        if (!isRelayEnabled()) {
            return;
        }
        JsonObject payload = new JsonObject();
        JsonArray embeds = new JsonArray();
        JsonObject embed = new JsonObject();
        embed.addProperty("title", "PrivateServer: " + eventType);
        embed.addProperty("description", message);
        embed.addProperty("color", colorForEvent(eventType));
        embeds.add(embed);
        payload.add("embeds", embeds);
        sendWebhook(payload);
    }

    public void relayChat(EntityPlayerMP player, String message) {
        if (!isRelayEnabled() || !configService.getChatConfig().relayChatMessages) {
            return;
        }
        JsonObject payload = new JsonObject();
        Optional<DiscordProfile> linked = getLinkedProfile(player.getUniqueID());
        payload.addProperty("username", linked.map(DiscordProfile::getDisplayName).orElse(player.getName()));
        linked.map(DiscordProfile::getAvatarUrl).ifPresent(avatar -> payload.addProperty("avatar_url", avatar));

        JsonArray embeds = new JsonArray();
        JsonObject embed = new JsonObject();
        embed.addProperty("description", message);
        embed.addProperty("color", 5793266);
        embeds.add(embed);
        payload.add("embeds", embeds);

        sendWebhook(payload);
    }

    public Optional<DiscordProfile> getLinkedProfile(UUID playerId) {
        try {
            return linkStore.get(playerId);
        } catch (SQLException e) {
            PrivateServer.LOGGER.error("Could not get discord link", e);
            return Optional.empty();
        }
    }

    public boolean unlink(UUID playerId) {
        try {
            return linkStore.remove(playerId);
        } catch (SQLException e) {
            throw new RuntimeException("Could not unlink discord account", e);
        }
    }

    public LinkResult link(UUID playerId, String discordUserId) {
        if (!configService.getChatConfig().discordLinkingEnabled) {
            return LinkResult.error("Discord linking is disabled in modules/chat.json.");
        }
        if (configService.getChatConfig().discordBotToken == null || configService.getChatConfig().discordBotToken.trim().isEmpty()) {
            return LinkResult.error("discordBotToken is missing in modules/chat.json.");
        }
        try {
            DiscordProfile profile = fetchProfile(discordUserId);
            linkStore.upsert(playerId, profile);
            return LinkResult.success(profile, "Linked to " + profile.getDisplayName() + ".");
        } catch (Exception e) {
            return LinkResult.error("Could not link Discord account: " + e.getMessage());
        }
    }

    private DiscordProfile fetchProfile(String discordUserId) throws IOException {
        String botToken = configService.getChatConfig().discordBotToken.trim();
        String guildId = configService.getChatConfig().discordGuildId == null ? "" : configService.getChatConfig().discordGuildId.trim();

        String displayName = null;
        String avatarUrl = null;

        if (!guildId.isEmpty()) {
            JsonObject member = discordGetJson("https://discord.com/api/v10/guilds/" + guildId + "/members/" + discordUserId, botToken);
            JsonObject user = member.has("user") && member.get("user").isJsonObject() ? member.getAsJsonObject("user") : new JsonObject();
            if (member.has("nick") && !member.get("nick").isJsonNull()) {
                displayName = member.get("nick").getAsString();
            }
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = pickDisplayName(user);
            }
            avatarUrl = pickAvatarUrl(discordUserId, user);
        }

        if (displayName == null || displayName.trim().isEmpty()) {
            JsonObject user = discordGetJson("https://discord.com/api/v10/users/" + discordUserId, botToken);
            displayName = pickDisplayName(user);
            avatarUrl = pickAvatarUrl(discordUserId, user);
        }

        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = "DiscordUser-" + discordUserId;
        }
        return new DiscordProfile(discordUserId, displayName, avatarUrl == null ? "" : avatarUrl);
    }

    private JsonObject discordGetJson(String url, String botToken) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Bot " + botToken);
        connection.setRequestProperty("User-Agent", "PrivateServer-Mod");

        int code = connection.getResponseCode();
        String body = readBody(code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream());
        if (code < 200 || code >= 300) {
            throw new IOException("Discord API status " + code + " for " + url + ": " + body);
        }
        JsonElement parsed = new JsonParser().parse(body);
        if (!parsed.isJsonObject()) {
            throw new IOException("Discord API returned invalid JSON object");
        }
        return parsed.getAsJsonObject();
    }

    private static String pickDisplayName(JsonObject userJson) {
        if (userJson.has("global_name") && !userJson.get("global_name").isJsonNull()) {
            String globalName = userJson.get("global_name").getAsString();
            if (!globalName.trim().isEmpty()) {
                return globalName;
            }
        }
        if (userJson.has("username") && !userJson.get("username").isJsonNull()) {
            return userJson.get("username").getAsString();
        }
        return null;
    }

    private static String pickAvatarUrl(String discordUserId, JsonObject userJson) {
        if (!userJson.has("avatar") || userJson.get("avatar").isJsonNull()) {
            return "";
        }
        String avatarHash = userJson.get("avatar").getAsString();
        if (avatarHash.trim().isEmpty()) {
            return "";
        }
        String ext = avatarHash.startsWith("a_") ? "gif" : "png";
        return "https://cdn.discordapp.com/avatars/" + discordUserId + "/" + avatarHash + "." + ext + "?size=128";
    }

    private void sendWebhook(JsonObject payload) {
        String webhook = configService.getChatConfig().discordWebhookUrl;
        if (webhook == null || webhook.trim().isEmpty()) {
            return;
        }
        final String body = gson.toJson(payload);
        executor.submit(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(webhook).openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "PrivateServer-Mod");
                try (OutputStream out = connection.getOutputStream()) {
                    out.write(body.getBytes(StandardCharsets.UTF_8));
                }
                int code = connection.getResponseCode();
                if (code < 200 || code >= 300) {
                    String response = readBody(connection.getErrorStream());
                    PrivateServer.LOGGER.warn("Discord webhook failed with status {} body {}", code, response);
                }
            } catch (Exception e) {
                PrivateServer.LOGGER.warn("Discord webhook send failed", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private boolean isRelayEnabled() {
        return configService.getMainConfig().modules.chat
                && configService.getChatConfig().enabled
                && configService.getChatConfig().discordRelayEnabled;
    }

    private static String readBody(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        }
    }

    private static int colorForEvent(String eventType) {
        if ("death".equalsIgnoreCase(eventType)) {
            return 15548997;
        }
        if ("join".equalsIgnoreCase(eventType)) {
            return 5763719;
        }
        if ("leave".equalsIgnoreCase(eventType)) {
            return 15158332;
        }
        if ("advancement".equalsIgnoreCase(eventType)) {
            return 3447003;
        }
        return 9807270;
    }

    public static final class LinkResult {
        private final boolean success;
        private final DiscordProfile profile;
        private final String message;

        private LinkResult(boolean success, DiscordProfile profile, String message) {
            this.success = success;
            this.profile = profile;
            this.message = message;
        }

        public static LinkResult success(DiscordProfile profile, String message) {
            return new LinkResult(true, profile, message);
        }

        public static LinkResult error(String message) {
            return new LinkResult(false, null, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public DiscordProfile getProfile() {
            return profile;
        }

        public String getMessage() {
            return message;
        }
    }
}
