package me.styna.privateserver.config.model;

import java.util.Arrays;
import java.util.List;

public class MainConfig {
    public String commandPrefix = "&7[&bPrivateServer&7] ";
    public ModuleFlags modules = new ModuleFlags();
    public int flyEnableCostLevels = 5;
    public List<String> cocaineConsumeConsoleCommands = Arrays.asList(
            "effect %player% minecraft:speed 20 1 true",
            "effect %player% minecraft:haste 20 1 true",
            "effect %player% minecraft:strength 20 0 true",
            "effect %player% privateserver:dizzy 20 0 true"
    );
    public double cocaineAllEffectsChancePercent = 1.0D;
    public double dizzyTripChancePercent = 15.0D;
}
