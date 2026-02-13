package me.styna.privateserver.command;

import me.styna.privateserver.PrivateServer;
import me.styna.privateserver.teleport.TeleportService;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;

public class CommandTpaHere extends BasePrivateCommand {

    public CommandTpaHere(PrivateServer mod) {
        super(mod);
    }

    @Override
    public String getName() {
        return "tpahere";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/tpahere <player>";
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
        if (args.length != 1) {
            throw new CommandException(getUsage(sender));
        }
        EntityPlayerMP requester = (EntityPlayerMP) sender.getCommandSenderEntity();
        EntityPlayerMP target = getPlayer(server, sender, args[0]);

        long tick = mod.getCurrentServerTick();
        TeleportService.RequestResult result = mod.getTeleportService().requestTpa(requester, target, true, tick);
        switch (result) {
            case SELF:
                send(sender, mod.getConfigService().getTeleportConfig().commandPrefix, "&cYou cannot request yourself.");
                return;
            case TARGET_BUSY:
                send(sender, mod.getConfigService().getTeleportConfig().commandPrefix, "&cThat player already has a pending request.");
                return;
            case CREATED:
            default:
                send(requester, mod.getConfigService().getTeleportConfig().commandPrefix,
                        "&aRequest sent to &e" + target.getName() + "&a.");
                send(target, mod.getConfigService().getTeleportConfig().commandPrefix,
                        "&e" + requester.getName() + " &awants you to teleport to them. Use &e/tpaccept &aor &e/tpdeny&a.");
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }
        return Collections.emptyList();
    }
}
