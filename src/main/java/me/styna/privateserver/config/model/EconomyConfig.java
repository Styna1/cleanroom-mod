package me.styna.privateserver.config.model;

public class EconomyConfig {
    public String commandPrefix = "";
    public boolean enabled = true;
    public double startingBalance = 0.0D;
    public int baltopLimit = 10;
    public boolean playtimeRewardEnabled = true;
    public double playtimeRewardAmount = 150.0D;
    public int playtimeRewardIntervalMinutes = 30;
    public int playtimeAfkTimeoutMinutes = 5;
    public String playtimeRewardMessage = "&aYou've received &e%amount% &afor playing on the server!";
}
