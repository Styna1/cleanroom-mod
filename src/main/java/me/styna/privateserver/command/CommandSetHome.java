package me.styna.privateserver.command;

import me.styna.privateserver.PrivateServer;
import me.styna.privateserver.teleport.TeleportStore;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandSetHome extends BasePrivateCommand {

    public CommandSetHome(PrivateServer mod) {
        super(mod);
    }

    @Override
    public String getName() {
        return "sethome";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/sethome [name]";
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
        String name = args.length >= 1 ? args[0] : "home";
        int count = mod.getTeleportService().getHomeCount(player);
        int limit = mod.getTeleportService().getMaxHomes(player);

        boolean saved = mod.getTeleportService().setHome(player, name);
        if (!saved) {
            send(sender, mod.getConfigService().getTeleportConfig().commandPrefix,
                    "&cHome limit reached (" + limit + "). Delete one first.");
            return;
        }

        String normalized = TeleportStore.normalizeName(name);
        send(sender, mod.getConfigService().getTeleportConfig().commandPrefix,
                "&aHome '&e" + normalized + "&a' saved. (&e" + Math.min(count + 1, limit) + "/" + limit + "&a)");
    }
}
