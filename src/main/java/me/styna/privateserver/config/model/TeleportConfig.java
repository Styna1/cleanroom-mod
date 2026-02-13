package me.styna.privateserver.config.model;

public class TeleportConfig {
    public String commandPrefix = "";
    public boolean enabled = true;
    public int delaySeconds = 5;
    public double homeCost = 20.0D;
    public double backCost = 0.0D;
    public double tpaCost = 150.0D;
    public int tpaRequestTimeoutSeconds = 60;
    public int homesForPlayers = 2;
    public int homesForOps = 10;
}
