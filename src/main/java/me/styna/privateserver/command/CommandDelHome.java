package me.styna.privateserver.command;

import me.styna.privateserver.PrivateServer;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandDelHome extends BasePrivateCommand {

    public CommandDelHome(PrivateServer mod) {
        super(mod);
    }

    @Override
    public String getName() {
        return "delhome";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/delhome <name>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
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
        if (args.length < 1) {
            throw new CommandException(getUsage(sender));
        }

        EntityPlayerMP player = (EntityPlayerMP) sender.getCommandSenderEntity();
        boolean deleted = mod.getTeleportService().deleteHome(player, args[0]);
        if (!deleted) {
            send(sender, mod.getConfigService().getTeleportConfig().commandPrefix, "&cHome not found.");
            return;
        }
        send(sender, mod.getConfigService().getTeleportConfig().commandPrefix, "&aHome deleted.");
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        if (!(sender.getCommandSenderEntity() instanceof EntityPlayerMP)) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            EntityPlayerMP player = (EntityPlayerMP) sender.getCommandSenderEntity();
            return getListOfStringsMatchingLastWord(
                    args,
                    mod.getTeleportService().listHomes(player).stream().map(home -> home.getName()).collect(Collectors.toList())
            );
        }
        return Collections.emptyList();
    }
}
