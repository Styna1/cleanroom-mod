package me.fade.shopforge.service;

import me.fade.shopforge.config.ShopConfigService;
import me.fade.shopforge.config.model.ShopConfig;
import me.fade.shopforge.gui.ShopContainer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopService {

    private final ShopConfigService configService;
    private final EconomyBridge economyBridge;
    private final Map<UUID, AdminSession> sessionsByPlayer = new HashMap<>();

    public ShopService(ShopConfigService configService, EconomyBridge economyBridge) {
        this.configService = configService;
        this.economyBridge = economyBridge;
    }

    public synchronized ShopConfig liveConfigCopy() {
        return configService.getLiveConfigCopy();
    }

    public synchronized AdminSession startSession(EntityPlayerMP player) {
        return sessionsByPlayer.computeIfAbsent(player.getUniqueID(), key -> new AdminSession(player.getUniqueID(), configService.getLiveConfigCopy()));
    }

    public synchronized AdminSession getSession(EntityPlayerMP player) {
        return sessionsByPlayer.get(player.getUniqueID());
    }

    public synchronized AdminSession getSession(UUID playerId) {
        return sessionsByPlayer.get(playerId);
    }

    public synchronized boolean hasSession(EntityPlayerMP player) {
        return sessionsByPlayer.containsKey(player.getUniqueID());
    }

    public synchronized void cancelSession(EntityPlayerMP player) {
        sessionsByPlayer.remove(player.getUniqueID());
    }

    public synchronized boolean commitSession(EntityPlayerMP player) {
        AdminSession session = sessionsByPlayer.remove(player.getUniqueID());
        if (session == null) {
            return false;
        }

        configService.saveLiveConfig(session.getWorkingConfig());
        return true;
    }

    public synchronized ShopConfig viewConfig(EntityPlayerMP player, boolean adminView) {
        if (adminView) {
            AdminSession session = sessionsByPlayer.get(player.getUniqueID());
            if (session != null) {
                return session.getWorkingConfig();
            }
        }
        return configService.getLiveConfigCopy();
    }

    public EconomyBridge getEconomyBridge() {
        return economyBridge;
    }

    public void refreshOpenContainers(MinecraftServer server) {
        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            if (player.openContainer instanceof ShopContainer) {
                ShopContainer container = (ShopContainer) player.openContainer;
                container.refreshFromServer();
            }
        }
    }
}