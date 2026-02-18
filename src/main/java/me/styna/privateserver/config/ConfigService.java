package me.styna.privateserver.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.styna.privateserver.config.model.ChatConfig;
import me.styna.privateserver.config.model.CombatConfig;
import me.styna.privateserver.config.model.EconomyConfig;
import me.styna.privateserver.config.model.MainConfig;
import me.styna.privateserver.config.model.TabConfig;
import me.styna.privateserver.config.model.TeleportConfig;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Supplier;

public class ConfigService {

    private final File rootDir;
    private final File modulesDir;
    private final File databasesDir;
    private final Gson gson;

    private MainConfig mainConfig;
    private EconomyConfig economyConfig;
    private CombatConfig combatConfig;
    private ChatConfig chatConfig;
    private TabConfig tabConfig;
    private TeleportConfig teleportConfig;

    public ConfigService(File rootDir) {
        this.rootDir = rootDir;
        this.modulesDir = new File(rootDir, "modules");
        this.databasesDir = new File(rootDir, "databases");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void loadAll() throws IOException {
        ensureDirectories();
        this.mainConfig = loadFile(new File(rootDir, "config.json"), MainConfig.class, MainConfig::new);
        this.economyConfig = loadFile(new File(modulesDir, "economy.json"), EconomyConfig.class, EconomyConfig::new);
        this.combatConfig = loadFile(new File(modulesDir, "combat.json"), CombatConfig.class, CombatConfig::new);
        this.chatConfig = loadFile(new File(modulesDir, "chat.json"), ChatConfig.class, ChatConfig::new);
        this.tabConfig = loadFile(new File(modulesDir, "tab.json"), TabConfig.class, TabConfig::new);
        this.teleportConfig = loadFile(new File(modulesDir, "teleport.json"), TeleportConfig.class, TeleportConfig::new);
<<<<<<< HEAD

        // Re-write economy defaults so newly added settings are visible and editable in economy.json.
        writeFile(new File(modulesDir, "economy.json"), this.economyConfig);
=======
>>>>>>> 293884e14fd113d3dc79e066dba0a1ad26810e84
    }

    private void ensureDirectories() throws IOException {
        if (!rootDir.exists() && !rootDir.mkdirs()) {
            throw new IOException("Could not create config directory: " + rootDir.getAbsolutePath());
        }
        if (!modulesDir.exists() && !modulesDir.mkdirs()) {
            throw new IOException("Could not create modules directory: " + modulesDir.getAbsolutePath());
        }
        if (!databasesDir.exists() && !databasesDir.mkdirs()) {
            throw new IOException("Could not create databases directory: " + databasesDir.getAbsolutePath());
        }
    }

    private <T> T loadFile(File file, Class<T> type, Supplier<T> defaults) throws IOException {
        if (!file.exists()) {
            T value = defaults.get();
            writeFile(file, value);
            return value;
        }

        try (FileReader reader = new FileReader(file)) {
            T value = gson.fromJson(reader, type);
            if (value == null) {
                value = defaults.get();
                writeFile(file, value);
            }
            return value;
        }
    }

    public void saveAll() throws IOException {
        writeFile(new File(rootDir, "config.json"), mainConfig);
        writeFile(new File(modulesDir, "economy.json"), economyConfig);
        writeFile(new File(modulesDir, "combat.json"), combatConfig);
        writeFile(new File(modulesDir, "chat.json"), chatConfig);
        writeFile(new File(modulesDir, "tab.json"), tabConfig);
        writeFile(new File(modulesDir, "teleport.json"), teleportConfig);
    }

    private void writeFile(File file, Object value) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(value, writer);
        }
    }

    public File getRootDir() {
        return rootDir;
    }

    public File getModulesDir() {
        return modulesDir;
    }

    public File getDatabasesDir() {
        return databasesDir;
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public EconomyConfig getEconomyConfig() {
        return economyConfig;
    }

    public CombatConfig getCombatConfig() {
        return combatConfig;
    }

    public ChatConfig getChatConfig() {
        return chatConfig;
    }

    public TabConfig getTabConfig() {
        return tabConfig;
    }

    public TeleportConfig getTeleportConfig() {
        return teleportConfig;
    }
}
