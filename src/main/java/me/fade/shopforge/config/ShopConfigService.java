package me.fade.shopforge.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.fade.shopforge.config.model.ShopCategory;
import me.fade.shopforge.config.model.ShopConfig;
import me.fade.shopforge.config.model.ShopItemEntry;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

public class ShopConfigService {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final File rootDir;
    private final File configFile;

    private ShopConfig liveConfig;

    public ShopConfigService(File rootDir) {
        this.rootDir = rootDir;
        this.configFile = new File(rootDir, "shop.json");
    }

    public synchronized void load() {
        ensureFolder();
        if (!configFile.exists()) {
            liveConfig = createDefaultConfig();
            writeToDisk(liveConfig);
            return;
        }

        try (Reader reader = Files.newBufferedReader(configFile.toPath(), StandardCharsets.UTF_8)) {
            ShopConfig parsed = GSON.fromJson(reader, ShopConfig.class);
            liveConfig = sanitize(parsed);
            writeToDisk(liveConfig);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load shop config", ex);
        }
    }

    public synchronized ShopConfig getLiveConfigCopy() {
        ensureLoaded();
        return liveConfig.copy();
    }

    public synchronized void saveLiveConfig(ShopConfig config) {
        ensureLoaded();
        liveConfig = sanitize(config);
        writeToDisk(liveConfig);
    }

    private ShopConfig sanitize(ShopConfig config) {
        ShopConfig safe = config == null ? createDefaultConfig() : config;
        if (safe.currencyPrefix == null || safe.currencyPrefix.trim().isEmpty()) {
            safe.currencyPrefix = "$";
        }
        if (safe.categories == null) {
            safe.categories = new ArrayList<>();
        }
        for (ShopCategory category : safe.categories) {
            if (category.id == null || category.id.trim().isEmpty()) {
                category.id = "category_" + safe.categories.indexOf(category);
            }
            if (category.name == null || category.name.trim().isEmpty()) {
                category.name = category.id;
            }
            if (category.iconItemId == null || category.iconItemId.trim().isEmpty()) {
                category.iconItemId = "minecraft:chest";
            }
            if (category.items == null) {
                category.items = new ArrayList<>();
            }
            for (ShopItemEntry item : category.items) {
                if (item.itemId == null || item.itemId.trim().isEmpty()) {
                    item.itemId = "minecraft:stone";
                }
                item.count = Math.max(1, item.count);
                item.meta = Math.max(0, item.meta);
                item.price = Math.max(0.0D, item.price);
            }
        }
        return safe;
    }

    private void ensureFolder() {
        if (!rootDir.exists() && !rootDir.mkdirs()) {
            throw new RuntimeException("Failed to create config directory: " + rootDir.getAbsolutePath());
        }
    }

    private void ensureLoaded() {
        if (liveConfig == null) {
            load();
        }
    }

    private void writeToDisk(ShopConfig config) {
        try (Writer writer = Files.newBufferedWriter(configFile.toPath(), StandardCharsets.UTF_8)) {
            GSON.toJson(config, writer);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write shop config", ex);
        }
    }

    private static ShopConfig createDefaultConfig() {
        ShopConfig config = new ShopConfig();

        ShopCategory blocks = new ShopCategory();
        blocks.id = "blocks";
        blocks.name = "Blocks";
        blocks.iconItemId = "minecraft:stone";
        blocks.items.add(newItem("minecraft:stone", 0, 64, 32.0D));
        blocks.items.add(newItem("minecraft:cobblestone", 0, 64, 16.0D));
        blocks.items.add(newItem("minecraft:glass", 0, 64, 24.0D));

        ShopCategory combat = new ShopCategory();
        combat.id = "combat";
        combat.name = "Combat";
        combat.iconItemId = "minecraft:iron_sword";
        combat.items.add(newItem("minecraft:iron_sword", 0, 1, 150.0D));
        combat.items.add(newItem("minecraft:bow", 0, 1, 140.0D));
        combat.items.add(newItem("minecraft:arrow", 0, 32, 25.0D));

        ShopCategory food = new ShopCategory();
        food.id = "food";
        food.name = "Food";
        food.iconItemId = "minecraft:cooked_beef";
        food.items.add(newItem("minecraft:cooked_beef", 0, 16, 20.0D));
        food.items.add(newItem("minecraft:golden_apple", 0, 1, 120.0D));

        config.categories.add(blocks);
        config.categories.add(combat);
        config.categories.add(food);
        return config;
    }

    private static ShopItemEntry newItem(String id, int meta, int count, double price) {
        ShopItemEntry entry = new ShopItemEntry();
        entry.itemId = id;
        entry.meta = meta;
        entry.count = count;
        entry.price = price;
        return entry;
    }
}