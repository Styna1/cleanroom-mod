package me.styna.privateserver.command;

import me.styna.privateserver.PrivateServer;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandBack extends BasePrivateCommand {

    public CommandBack(PrivateServer mod) {
        super(mod);
    }

    @Override
    public String getName() {
        return "back";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/back";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender.getCommandSenderEntity() instanceof EntityPlayerMP)) {
            throw new CommandException("Only players can use this command.");
        }
        if (!mod.getTeleportService().isEnabled()) {
            send(sender, mod.getConfigService().getTeleportConfig().commandPrefix, "&cTeleport module is disabled.");
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) sender.getCommandSenderEntity();
        long tick = mod.getCurrentServerTick();
        boolean queued = mod.getTeleportService().requestBackTeleport(player, tick);
        if (!queued) {
            if (mod.getTeleportService().hasPendingTeleport(player.getUniqueID())) {
                send(sender, mod.getConfigService().getTeleportConfig().commandPrefix, "&cYou already have a pending teleport.");
            } else {
                send(sender, mod.getConfigService().getTeleportConfig().commandPrefix, "&cNo back location available.");
            }
            return;
        }
        send(sender, mod.getConfigService().getTeleportConfig().commandPrefix, "&eBack teleport started.");
    }
}
