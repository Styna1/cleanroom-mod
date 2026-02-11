package me.styna.privateserver.teleport;

import me.styna.privateserver.config.ConfigService;
import me.styna.privateserver.economy.EconomyService;
import me.styna.privateserver.util.TextUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TeleportService {

    private final ConfigService configService;
    private final EconomyService economyService;
    private final TeleportStore store;
    private final Map<UUID, PendingTeleport> pendingTeleports = new HashMap<>();
    private final Map<UUID, TpaRequest> tpaRequestsByTarget = new HashMap<>();
    private final Map<UUID, TeleportLocation> backLocations = new HashMap<>();

    public TeleportService(ConfigService configService, EconomyService economyService, File databaseFile) {
        this.configService = configService;
        this.economyService = economyService;
        this.store = new TeleportStore(databaseFile);
    }

    public void initialize() throws SQLException {
        store.initialize();
    }

    public void shutdown() {
        store.close();
    }

    public boolean isEnabled() {
        return configService.getMainConfig().modules.teleport && configService.getTeleportConfig().enabled;
    }

    public int getMaxHomes(EntityPlayerMP player) {
        return player.canUseCommand(2, "home")
                ? Math.max(1, configService.getTeleportConfig().homesForOps)
                : Math.max(1, configService.getTeleportConfig().homesForPlayers);
    }

    public int getHomeCount(EntityPlayerMP player) {
        try {
            return store.countHomes(player.getUniqueID());
        } catch (SQLException e) {
            throw new RuntimeException("Could not count homes", e);
        }
    }

    public List<HomeEntry> listHomes(EntityPlayerMP player) {
        try {
            return store.listHomes(player.getUniqueID());
        } catch (SQLException e) {
            throw new RuntimeException("Could not list homes", e);
        }
    }

    public boolean setHome(EntityPlayerMP player, String homeName) {
        String normalized = TeleportStore.normalizeName(homeName);
        try {
            Optional<HomeEntry> existing = store.getHome(player.getUniqueID(), normalized);
            if (!existing.isPresent() && store.countHomes(player.getUniqueID()) >= getMaxHomes(player)) {
                return false;
            }
            store.upsertHome(player.getUniqueID(), player.getName(), normalized, TeleportLocation.fromPlayer(player));
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Could not save home", e);
        }
    }

    public boolean deleteHome(EntityPlayerMP player, String homeName) {
        try {
            return store.deleteHome(player.getUniqueID(), homeName);
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete home", e);
        }
    }

    public RenameHomeResult renameHome(EntityPlayerMP player, String oldName, String newName) {
        String normalizedOld = TeleportStore.normalizeName(oldName);
        String normalizedNew = TeleportStore.normalizeName(newName);
        if (normalizedOld.equals(normalizedNew)) {
            return RenameHomeResult.SAME_NAME;
        }
        try {
            Optional<HomeEntry> targetHome = store.getHome(player.getUniqueID(), normalizedNew);
            if (targetHome.isPresent()) {
                return RenameHomeResult.TARGET_EXISTS;
            }
            boolean renamed = store.renameHome(player.getUniqueID(), normalizedOld, normalizedNew);
            return renamed ? RenameHomeResult.RENAMED : RenameHomeResult.NOT_FOUND;
        } catch (SQLException e) {
            throw new RuntimeException("Could not rename home", e);
        }
    }

    public boolean requestHomeTeleport(EntityPlayerMP player, String homeName, long currentTick) {
        Optional<HomeEntry> target;
        try {
            target = store.getHome(player.getUniqueID(), homeName);
        } catch (SQLException e) {
            throw new RuntimeException("Could not read home", e);
        }
        if (!target.isPresent()) {
            return false;
        }
        double cost = Math.max(0.0D, configService.getTeleportConfig().homeCost);
        return scheduleTeleport(player, target.get().getLocation(), cost, "home", currentTick);
    }

    public boolean requestBackTeleport(EntityPlayerMP player, long currentTick) {
        TeleportLocation destination = backLocations.get(player.getUniqueID());
        if (destination == null) {
            return false;
        }
        double cost = Math.max(0.0D, configService.getTeleportConfig().backCost);
        return scheduleTeleport(player, destination, cost, "back", currentTick);
    }

    public RequestResult requestTpa(EntityPlayerMP requester, EntityPlayerMP target, boolean here, long currentTick) {
        if (requester.getUniqueID().equals(target.getUniqueID())) {
            return RequestResult.SELF;
        }
        if (tpaRequestsByTarget.containsKey(target.getUniqueID())) {
            return RequestResult.TARGET_BUSY;
        }
        TpaRequest request = new TpaRequest(requester.getUniqueID(), target.getUniqueID(), here, currentTick);
        tpaRequestsByTarget.put(target.getUniqueID(), request);
        return RequestResult.CREATED;
    }

    public AcceptResult acceptTpa(EntityPlayerMP target, MinecraftServer server, long currentTick) {
        TpaRequest request = tpaRequestsByTarget.remove(target.getUniqueID());
        if (request == null) {
            return AcceptResult.none("No pending TPA request.");
        }

        EntityPlayerMP requester = server.getPlayerList().getPlayerByUUID(request.requesterId);
        if (requester == null) {
            return AcceptResult.none("Requester is no longer online.");
        }

        TeleportLocation destination = request.here
                ? TeleportLocation.fromPlayer(target)
                : TeleportLocation.fromPlayer(requester);
        EntityPlayerMP teleported = request.here ? target : requester;
        double cost = Math.max(0.0D, configService.getTeleportConfig().tpaCost);

        boolean scheduled = scheduleTeleport(
                teleported,
                destination,
                cost,
                request.here ? "tpahere" : "tpa",
                currentTick,
                requester.getUniqueID(),
                requester.getName()
        );
        if (!scheduled) {
            return AcceptResult.none("Could not start teleport (already pending).");
        }
        return new AcceptResult(teleported, requester, target, request.here, true, "Teleport request accepted.");
    }

    public boolean denyTpa(EntityPlayerMP target) {
        return denyTpaAndGetRequester(target) != null;
    }

    public UUID denyTpaAndGetRequester(EntityPlayerMP target) {
        TpaRequest removed = tpaRequestsByTarget.remove(target.getUniqueID());
        return removed == null ? null : removed.requesterId;
    }

    public boolean scheduleTeleport(EntityPlayerMP player, TeleportLocation destination, double cost, String reason, long currentTick) {
        return scheduleTeleport(player, destination, cost, reason, currentTick, player.getUniqueID(), player.getName());
    }

    public boolean scheduleTeleport(EntityPlayerMP player, TeleportLocation destination, double cost, String reason, long currentTick, UUID costPayerId, String costPayerName) {
        if (pendingTeleports.containsKey(player.getUniqueID())) {
            return false;
        }
        int delaySeconds = Math.max(0, configService.getTeleportConfig().delaySeconds);
        PendingTeleport pending = new PendingTeleport(
                player.getUniqueID(),
                TeleportLocation.fromPlayer(player),
                destination,
                cost,
                reason,
                costPayerId,
                costPayerName,
                currentTick,
                currentTick + (delaySeconds * 20L)
        );
        pendingTeleports.put(player.getUniqueID(), pending);
        return true;
    }

    public boolean hasPendingTeleport(UUID playerId) {
        return pendingTeleports.containsKey(playerId);
    }

    public void recordBackLocation(EntityPlayerMP player) {
        backLocations.put(player.getUniqueID(), TeleportLocation.fromPlayer(player));
    }

    public void onTick(MinecraftServer server, long currentTick) {
        expireRequests(server, currentTick);
        processPendingTeleports(server, currentTick);
    }

    private void expireRequests(MinecraftServer server, long currentTick) {
        int timeoutSeconds = Math.max(5, configService.getTeleportConfig().tpaRequestTimeoutSeconds);
        long timeoutTicks = timeoutSeconds * 20L;
        Iterator<Map.Entry<UUID, TpaRequest>> iterator = tpaRequestsByTarget.entrySet().iterator();
        while (iterator.hasNext()) {
            TpaRequest request = iterator.next().getValue();
            if (currentTick - request.createdAtTick > timeoutTicks) {
                EntityPlayerMP requester = server.getPlayerList().getPlayerByUUID(request.requesterId);
                EntityPlayerMP target = server.getPlayerList().getPlayerByUUID(request.targetId);
                if (requester != null) {
                    TextUtil.send(requester, "&cYour teleport request expired.");
                }
                if (target != null) {
                    TextUtil.send(target, "&7A teleport request expired.");
                }
                iterator.remove();
            }
        }
    }

    private void processPendingTeleports(MinecraftServer server, long currentTick) {
        Iterator<Map.Entry<UUID, PendingTeleport>> iterator = pendingTeleports.entrySet().iterator();
        while (iterator.hasNext()) {
            PendingTeleport pending = iterator.next().getValue();
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(pending.playerId);
            if (player == null) {
                iterator.remove();
                continue;
            }

            if (hasMoved(player, pending.origin)) {
                TextUtil.send(player, "&cTeleport cancelled because you moved.");
                iterator.remove();
                continue;
            }

            long remainingTicks = pending.executeAtTick - currentTick;
            if (remainingTicks > 0L) {
                if (currentTick % 20L == 0L) {
                    long remainingSeconds = (long) Math.ceil(remainingTicks / 20.0D);
                    TextUtil.actionBar(player, "&eTeleporting in " + remainingSeconds + "s...");
                }
                continue;
            }

            if (pending.cost > 0.0D) {
                if (!economyService.isEnabled()) {
                    TextUtil.send(player, "&cEconomy module is disabled. Teleport cancelled.");
                    iterator.remove();
                    continue;
                }
                EntityPlayerMP payer = server.getPlayerList().getPlayerByUUID(pending.costPayerId);
                if (payer == null) {
                    TextUtil.send(player, "&cTeleport payer is offline. Teleport cancelled.");
                    iterator.remove();
                    continue;
                }
                double balance = economyService.getBalance(payer);
                if (balance < pending.cost) {
                    TextUtil.send(payer, "&cYou need $" + String.format("%.2f", pending.cost) + " to pay for this teleport.");
                    if (!payer.getUniqueID().equals(player.getUniqueID())) {
                        TextUtil.send(player, "&cTeleport payer does not have enough balance.");
                    }
                    iterator.remove();
                    continue;
                }
                economyService.withdraw(payer.getUniqueID(), pending.costPayerName, pending.cost);
            }

            recordBackLocation(player);
            teleport(server, player, pending.destination);
            TextUtil.send(player, "&aTeleported.");
            iterator.remove();
        }
    }

    private static boolean hasMoved(EntityPlayerMP player, TeleportLocation origin) {
        if (player.dimension != origin.getDimension()) {
            return true;
        }
        double dx = player.posX - origin.getX();
        double dy = player.posY - origin.getY();
        double dz = player.posZ - origin.getZ();
        return (dx * dx) + (dy * dy) + (dz * dz) > 0.0625D;
    }

    private static void teleport(MinecraftServer server, EntityPlayerMP player, TeleportLocation destination) {
        if (player.dimension != destination.getDimension()) {
            player.changeDimension(destination.getDimension());
            player = server.getPlayerList().getPlayerByUUID(player.getUniqueID());
            if (player == null) {
                return;
            }
        }
        player.connection.setPlayerLocation(
                destination.getX(),
                destination.getY(),
                destination.getZ(),
                destination.getYaw(),
                destination.getPitch()
        );
    }

    public enum RenameHomeResult {
        RENAMED,
        SAME_NAME,
        TARGET_EXISTS,
        NOT_FOUND
    }

    public enum RequestResult {
        CREATED,
        SELF,
        TARGET_BUSY
    }

    public static final class AcceptResult {
        private final EntityPlayerMP teleportedPlayer;
        private final EntityPlayerMP requester;
        private final EntityPlayerMP target;
        private final boolean here;
        private final boolean accepted;
        private final String message;

        private AcceptResult(EntityPlayerMP teleportedPlayer, EntityPlayerMP requester, EntityPlayerMP target, boolean here, boolean accepted, String message) {
            this.teleportedPlayer = teleportedPlayer;
            this.requester = requester;
            this.target = target;
            this.here = here;
            this.accepted = accepted;
            this.message = message;
        }

        public static AcceptResult none(String message) {
            return new AcceptResult(null, null, null, false, false, message);
        }

        public EntityPlayerMP getTeleportedPlayer() {
            return teleportedPlayer;
        }

        public EntityPlayerMP getRequester() {
            return requester;
        }

        public EntityPlayerMP getTarget() {
            return target;
        }

        public boolean isHere() {
            return here;
        }

        public boolean isAccepted() {
            return accepted;
        }

        public String getMessage() {
            return message;
        }
    }

    private static final class PendingTeleport {
        private final UUID playerId;
        private final TeleportLocation origin;
        private final TeleportLocation destination;
        private final double cost;
        private final String reason;
        private final UUID costPayerId;
        private final String costPayerName;
        private final long createdAtTick;
        private final long executeAtTick;

        private PendingTeleport(UUID playerId, TeleportLocation origin, TeleportLocation destination, double cost, String reason, UUID costPayerId, String costPayerName, long createdAtTick, long executeAtTick) {
            this.playerId = playerId;
            this.origin = origin;
            this.destination = destination;
            this.cost = cost;
            this.reason = reason;
            this.costPayerId = costPayerId;
            this.costPayerName = costPayerName;
            this.createdAtTick = createdAtTick;
            this.executeAtTick = executeAtTick;
        }
    }

    private static final class TpaRequest {
        private final UUID requesterId;
        private final UUID targetId;
        private final boolean here;
        private final long createdAtTick;

        private TpaRequest(UUID requesterId, UUID targetId, boolean here, long createdAtTick) {
            this.requesterId = requesterId;
            this.targetId = targetId;
            this.here = here;
            this.createdAtTick = createdAtTick;
        }
    }
}
