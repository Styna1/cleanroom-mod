package me.styna.privateserver.service;

import me.styna.privateserver.PrivateServer;
<<<<<<< HEAD
import me.styna.privateserver.config.model.EconomyConfig;
=======
>>>>>>> 293884e14fd113d3dc79e066dba0a1ad26810e84
import me.styna.privateserver.effect.ModPotions;
import me.styna.privateserver.util.TextUtil;
import net.minecraft.command.ICommand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GameplayEventHandler {

    private static final long TICKS_PER_SECOND = 20L;
<<<<<<< HEAD
    private static final String DEFAULT_PLAYTIME_REWARD_MESSAGE = "&aYou've received &e%amount% &afor playing on the server!";
=======
    private static final long PLAYTIME_REWARD_INTERVAL_TICKS = 30L * 60L * TICKS_PER_SECOND;
    private static final long AFK_TIMEOUT_TICKS = 5L * 60L * TICKS_PER_SECOND;
    private static final double PLAYTIME_REWARD_AMOUNT = 150.0D;
    private static final String PLAYTIME_REWARD_MESSAGE = "(default) you've received %s for playing on the server!";
>>>>>>> 293884e14fd113d3dc79e066dba0a1ad26810e84
    private static final double MEMORY_WARN_THRESHOLD_RATIO = 0.95D;
    private static final long MEMORY_WARN_COOLDOWN_TICKS = 5L * 60L * TICKS_PER_SECOND;
    private static final double ACTIVITY_POSITION_DELTA_SQ = 0.0001D;
    private static final float ACTIVITY_ROTATION_DELTA = 2.0F;

    private static final Set<String> BLOCKED_WHILE_COMBAT_TAGGED = new HashSet<>(Arrays.asList(
            "home",
            "sethome",
            "delhome",
            "renamehome",
            "renamehomei",
            "back",
            "tpa",
            "tpahere",
            "tpaccept",
            "tpdeny",
            "spawn",
            "rtp"
    ));

    private final PrivateServer mod;
    private final Map<UUID, PlayerActivityState> activityByPlayer = new HashMap<>();
    private long serverTick = 0L;
    private long lastMemoryWarningTick = Long.MIN_VALUE;

    public GameplayEventHandler(PrivateServer mod) {
        this.mod = mod;
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!mod.getConfigService().getMainConfig().modules.combat || !mod.getConfigService().getCombatConfig().enabled) {
            return;
        }

        if (!(event.getEntityLiving() instanceof EntityPlayerMP)) {
            return;
        }

        Entity source = event.getSource().getTrueSource();
        if (!(source instanceof EntityPlayerMP)) {
            return;
        }

        EntityPlayerMP victim = (EntityPlayerMP) event.getEntityLiving();
        EntityPlayerMP attacker = (EntityPlayerMP) source;
        int seconds = mod.getConfigService().getCombatConfig().combatTagSeconds;

        mod.getCombatTagService().tag(victim.getUniqueID(), seconds, serverTick);
        mod.getCombatTagService().tag(attacker.getUniqueID(), seconds, serverTick);
        markActivity(victim);
        markActivity(attacker);
    }

    @SubscribeEvent
    public void onCommand(CommandEvent event) {
<<<<<<< HEAD
=======
        if (!mod.getConfigService().getMainConfig().modules.combat || !mod.getConfigService().getCombatConfig().enabled) {
            return;
        }
>>>>>>> 293884e14fd113d3dc79e066dba0a1ad26810e84
        if (!(event.getSender() instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) event.getSender();
        markActivity(player);
<<<<<<< HEAD
        if (!mod.getConfigService().getMainConfig().modules.combat || !mod.getConfigService().getCombatConfig().enabled) {
            return;
        }
=======
>>>>>>> 293884e14fd113d3dc79e066dba0a1ad26810e84
        if (!mod.getCombatTagService().isTagged(player.getUniqueID(), serverTick)) {
            return;
        }

        ICommand command = event.getCommand();
        String commandName = command.getName().toLowerCase(Locale.ROOT);
        if (!BLOCKED_WHILE_COMBAT_TAGGED.contains(commandName)) {
            return;
        }

        long seconds = mod.getCombatTagService().remainingSeconds(player.getUniqueID(), serverTick);
        TextUtil.send(player, "&cYou cannot use teleport commands while in combat. Remaining: " + seconds + "s");
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        mod.getBossbarService().onPlayerJoin(player);
        PlayerActivityState state = getActivityState(player);
        state.lastActivityTick = serverTick;
        state.capture(player);

        if (mod.getConfigService().getChatConfig().relayJoinLeave) {
            mod.getDiscordRelayService().relay("join", player.getName() + " joined the server");
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
<<<<<<< HEAD
        activityByPlayer.remove(event.player.getUniqueID());
=======
>>>>>>> 293884e14fd113d3dc79e066dba0a1ad26810e84
        if (mod.getConfigService().getChatConfig().relayJoinLeave) {
            mod.getDiscordRelayService().relay("leave", event.player.getName() + " left the server");
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();
        mod.getTeleportService().recordBackLocation(player);
        markActivity(player);

        if (mod.getConfigService().getChatConfig().relayDeaths) {
            mod.getDiscordRelayService().relay("death", player.getName() + " died");
        }
    }

    @SubscribeEvent
    public void onAdvancement(AdvancementEvent event) {
        if (!(event.getEntityPlayer() instanceof EntityPlayerMP)) {
            return;
        }
        if (!mod.getConfigService().getChatConfig().relayAdvancements) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) event.getEntityPlayer();
        markActivity(player);
        mod.getDiscordRelayService().relay(
                "advancement",
                player.getName() + " made advancement " + event.getAdvancement().getId()
        );
    }

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        markActivity(event.getPlayer());
        mod.getDiscordRelayService().relayChat(event.getPlayer(), event.getMessage());
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        serverTick++;
        if (serverTick % 20L != 0L) {
            return;
        }

        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) {
            return;
        }

        mod.setCurrentServerTick(serverTick);
        mod.getTeleportService().onTick(server, serverTick);
        warnIfMemoryHigh(server);

        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            tickPlaytimeReward(player);
            handleDizzyTrip(player);

            long remaining = mod.getCombatTagService().remainingSeconds(player.getUniqueID(), serverTick);
            if (remaining > 0L) {
                TextUtil.actionBar(player, "&cIn combat for " + remaining + "s");
            }
        }
    }

    private void handleDizzyTrip(EntityPlayerMP player) {
        if (!player.isPotionActive(ModPotions.DIZZY)) {
            return;
        }

        double tripChance = Math.max(0.0D, Math.min(100.0D, mod.getConfigService().getMainConfig().dizzyTripChancePercent));
        if (player.getRNG().nextDouble() >= (tripChance / 100.0D)) {
            return;
        }

        ItemStack heldItem = player.getHeldItemMainhand();
        if (heldItem.isEmpty()) {
            return;
        }

        ItemStack dropped = heldItem.copy();
        player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
        player.dropItem(dropped, false, true);
        TextUtil.send(player, "&cYou tripped and dropped your held item.");
    }

    private void tickPlaytimeReward(EntityPlayerMP player) {
        PlayerActivityState state = getActivityState(player);
        updateMovementActivity(player, state);
        if (!mod.getEconomyService().isEnabled()) {
            return;
        }
<<<<<<< HEAD
        EconomyConfig economyConfig = mod.getConfigService().getEconomyConfig();
        if (!economyConfig.playtimeRewardEnabled) {
            return;
        }

        long afkTimeoutTicks = Math.max(1L, economyConfig.playtimeAfkTimeoutMinutes) * 60L * TICKS_PER_SECOND;
        if ((serverTick - state.lastActivityTick) > afkTimeoutTicks) {
            return;
        }

        long rewardIntervalTicks = Math.max(1L, economyConfig.playtimeRewardIntervalMinutes) * 60L * TICKS_PER_SECOND;
        double rewardAmount = Math.max(0.0D, economyConfig.playtimeRewardAmount);
        if (rewardAmount <= 0.0D) {
=======
        if ((serverTick - state.lastActivityTick) > AFK_TIMEOUT_TICKS) {
>>>>>>> 293884e14fd113d3dc79e066dba0a1ad26810e84
            return;
        }

        state.activeTicks += TICKS_PER_SECOND;
<<<<<<< HEAD
        while (state.activeTicks >= rewardIntervalTicks) {
            state.activeTicks -= rewardIntervalTicks;
            mod.getEconomyService().deposit(player.getUniqueID(), player.getName(), rewardAmount);
            sendPlaytimeRewardMessage(player, rewardAmount, economyConfig.playtimeRewardMessage);
        }
    }

    private void sendPlaytimeRewardMessage(EntityPlayerMP player, double rewardAmount, String template) {
        String messageTemplate = template;
        if (messageTemplate == null || messageTemplate.trim().isEmpty()) {
            messageTemplate = DEFAULT_PLAYTIME_REWARD_MESSAGE;
        }

        String amountText = "$" + String.format(Locale.US, "%.2f", rewardAmount);
        String resolved = messageTemplate.replace("%amount%", amountText).replace("%s", amountText);
        String mainPrefix = mod.getConfigService().getMainConfig().commandPrefix;
        String withDefaultReplaced = replaceDefaultToken(resolved, mainPrefix);
        if (withDefaultReplaced.equals(resolved) && !resolved.startsWith(mainPrefix)) {
            withDefaultReplaced = mainPrefix + resolved;
        }
        TextUtil.send(player, withDefaultReplaced);
    }

    private static String replaceDefaultToken(String text, String replacement) {
        String token = "(default)";
        String result = text;
        String lower = result.toLowerCase(Locale.ROOT);
        int index = lower.indexOf(token);
        while (index >= 0) {
            result = result.substring(0, index) + replacement + result.substring(index + token.length());
            lower = result.toLowerCase(Locale.ROOT);
            index = lower.indexOf(token);
        }
        return result;
    }

=======
        while (state.activeTicks >= PLAYTIME_REWARD_INTERVAL_TICKS) {
            state.activeTicks -= PLAYTIME_REWARD_INTERVAL_TICKS;
            mod.getEconomyService().deposit(player.getUniqueID(), player.getName(), PLAYTIME_REWARD_AMOUNT);
            String amountText = "$" + String.format(Locale.US, "%.2f", PLAYTIME_REWARD_AMOUNT);
            TextUtil.send(player, "&a" + PLAYTIME_REWARD_MESSAGE.replace("%s", "&e" + amountText + "&a"));
        }
    }

>>>>>>> 293884e14fd113d3dc79e066dba0a1ad26810e84
    private void updateMovementActivity(EntityPlayerMP player, PlayerActivityState state) {
        if (!state.hasSnapshot) {
            state.lastActivityTick = serverTick;
            state.capture(player);
            return;
        }

        double dx = player.posX - state.lastX;
        double dy = player.posY - state.lastY;
        double dz = player.posZ - state.lastZ;
        double deltaSq = (dx * dx) + (dy * dy) + (dz * dz);
        float yawDiff = wrappedAngleDiff(player.rotationYaw, state.lastYaw);
        float pitchDiff = Math.abs(player.rotationPitch - state.lastPitch);
        boolean changedDimension = player.dimension != state.lastDimension;
        boolean moved = changedDimension || deltaSq > ACTIVITY_POSITION_DELTA_SQ
                || yawDiff > ACTIVITY_ROTATION_DELTA || pitchDiff > ACTIVITY_ROTATION_DELTA;

        state.capture(player);
        if (moved) {
            state.lastActivityTick = serverTick;
        }
    }

    private static float wrappedAngleDiff(float current, float previous) {
        float diff = Math.abs(current - previous) % 360.0F;
        return diff > 180.0F ? 360.0F - diff : diff;
    }

    private void markActivity(EntityPlayerMP player) {
        PlayerActivityState state = getActivityState(player);
        state.lastActivityTick = serverTick;
        state.capture(player);
    }

    private PlayerActivityState getActivityState(EntityPlayerMP player) {
        return activityByPlayer.computeIfAbsent(player.getUniqueID(), id -> new PlayerActivityState());
    }

    private void warnIfMemoryHigh(MinecraftServer server) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        if (maxMemory <= 0L) {
            return;
        }

        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double usageRatio = (double) usedMemory / (double) maxMemory;
        if (usageRatio < MEMORY_WARN_THRESHOLD_RATIO) {
            return;
        }
        if ((serverTick - lastMemoryWarningTick) < MEMORY_WARN_COOLDOWN_TICKS) {
            return;
        }

        lastMemoryWarningTick = serverTick;
        server.getPlayerList().sendMessage(TextUtil.component("&cServer might crash, memory maxed."));
    }

    private static final class PlayerActivityState {
        private boolean hasSnapshot;
        private int lastDimension;
        private double lastX;
        private double lastY;
        private double lastZ;
        private float lastYaw;
        private float lastPitch;
        private long lastActivityTick;
        private long activeTicks;

        private void capture(EntityPlayerMP player) {
            hasSnapshot = true;
            lastDimension = player.dimension;
            lastX = player.posX;
            lastY = player.posY;
            lastZ = player.posZ;
            lastYaw = player.rotationYaw;
            lastPitch = player.rotationPitch;
        }
    }
}
