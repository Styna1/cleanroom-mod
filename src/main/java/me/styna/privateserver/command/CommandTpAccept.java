package me.styna.privateserver.command;

import me.styna.privateserver.PrivateServer;
import me.styna.privateserver.teleport.TeleportService;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandTpAccept extends BasePrivateCommand {

    public CommandTpAccept(PrivateServer mod) {
        super(mod);
    }

    @Override
    public String getName() {
        return "tpaccept";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/tpaccept";
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

        EntityPlayerMP target = (EntityPlayerMP) sender.getCommandSenderEntity();
        long tick = mod.getCurrentServerTick();
        TeleportService.AcceptResult result = mod.getTeleportService().acceptTpa(target, server, tick);
        if (!result.isAccepted()) {
            send(sender, mod.getConfigService().getTeleportConfig().commandPrefix, "&c" + result.getMessage());
            return;
        }

        send(target, mod.getConfigService().getTeleportConfig().commandPrefix, "&aTeleport request accepted.");
        if (result.getRequester() != null) {
            send(result.getRequester(), mod.getConfigService().getTeleportConfig().commandPrefix, "&aTeleport request accepted.");
        }
        if (result.getTeleportedPlayer() != null) {
            send(result.getTeleportedPlayer(), mod.getConfigService().getTeleportConfig().commandPrefix,
                    "&eTeleport will run after delay.");
        }
    }
}
