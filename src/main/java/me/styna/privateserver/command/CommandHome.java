package me.styna.privateserver.command;

import me.styna.privateserver.PrivateServer;
import me.styna.privateserver.teleport.HomeEntry;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandHome extends BasePrivateCommand {

    public CommandHome(PrivateServer mod) {
        super(mod);
    }

    @Override
    public String getName() {
        return "home";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/home [name|list]";
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
        EntityPlayerMP player = (EntityPlayerMP) sender.getCommandSenderEntity();
        if (args.length > 0 && "list".equalsIgnoreCase(args[0])) {
            List<HomeEntry> homes = mod.getTeleportService().listHomes(player);
            if (homes.isEmpty()) {
                send(sender, mod.getConfigService().getTeleportConfig().commandPrefix, "&7No homes set.");
                return;
            }
            String joined = homes.stream().map(HomeEntry::getName).collect(Collectors.joining("&7, &e"));
            send(sender, mod.getConfigService().getTeleportConfig().commandPrefix, "&aHomes: &e" + joined);
            return;
        }

        String name = args.length >= 1 ? args[0] : "home";
        long tick = mod.getCurrentServerTick();
        boolean queued = mod.getTeleportService().requestHomeTeleport(player, name, tick);
        if (!queued) {
            if (mod.getTeleportService().hasPendingTeleport(player.getUniqueID())) {
                send(sender, mod.getConfigService().getTeleportConfig().commandPrefix, "&cYou already have a pending teleport.");
            } else {
                send(sender, mod.getConfigService().getTeleportConfig().commandPrefix, "&cHome not found.");
            }
            return;
        }
        send(sender, mod.getConfigService().getTeleportConfig().commandPrefix, "&eTeleport to home started.");
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        if (!(sender.getCommandSenderEntity() instanceof EntityPlayerMP)) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            EntityPlayerMP player = (EntityPlayerMP) sender.getCommandSenderEntity();
            List<String> names = mod.getTeleportService().listHomes(player).stream().map(HomeEntry::getName).collect(Collectors.toList());
            names.add("list");
            return getListOfStringsMatchingLastWord(args, names);
        }
        return Collections.emptyList();
    }
}
