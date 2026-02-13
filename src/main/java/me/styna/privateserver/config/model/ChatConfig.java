package me.styna.privateserver.config.model;

public class ChatConfig {
    public String commandPrefix = "";
    public boolean enabled = true;
    public boolean discordRelayEnabled = false;
    public String discordWebhookUrl = "";
    public String discordBotToken = "";
    public String discordGuildId = "";
    public boolean discordLinkingEnabled = false;
    public boolean relayChatMessages = true;
    public boolean relayDeaths = true;
    public boolean relayAdvancements = true;
    public boolean relayJoinLeave = true;
    public boolean relayServerLifecycle = true;
}
