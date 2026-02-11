package me.styna.privateserver.command;

import me.styna.privateserver.PrivateServer;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandTpDeny extends BasePrivateCommand {

    public CommandTpDeny(PrivateServer mod) {
        super(mod);
    }

    @Override
    public String getName() {
        return "tpdeny";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/tpdeny";
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
        java.util.UUID requesterId = mod.getTeleportService().denyTpaAndGetRequester(player);
        if (requesterId == null) {
            send(sender, mod.getConfigService().getTeleportConfig().commandPrefix, "&cNo pending request.");
            return;
        }
        send(sender, mod.getConfigService().getTeleportConfig().commandPrefix, "&aTeleport request denied.");
        EntityPlayerMP requester = server.getPlayerList().getPlayerByUUID(requesterId);
        if (requester != null) {
            send(requester, mod.getConfigService().getTeleportConfig().commandPrefix, "&cYour teleport request was denied.");
        }
    }
}
