package me.styna.privateserver.service;

import me.styna.privateserver.util.TextUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;

public class BossbarService {

    private BossInfoServer bossbar;

    public void show(MinecraftServer server, String message) {
        if (bossbar == null) {
            bossbar = new BossInfoServer(new TextComponentString(TextUtil.colorize(message)), BossInfo.Color.BLUE, BossInfo.Overlay.PROGRESS);
            bossbar.setPercent(1.0F);
        } else {
            bossbar.setName(new TextComponentString(TextUtil.colorize(message)));
        }

        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            bossbar.addPlayer(player);
        }
    }

    public void hide() {
        if (bossbar == null) {
            return;
        }
        bossbar.setVisible(false);
        bossbar = null;
    }

    public void onPlayerJoin(EntityPlayerMP player) {
        if (bossbar != null) {
            bossbar.addPlayer(player);
        }
    }
}
