package me.styna.privateserver.command;

import me.styna.privateserver.PrivateServer;
import me.styna.privateserver.teleport.TeleportService;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandRenameHomei extends BasePrivateCommand {

    public CommandRenameHomei(PrivateServer mod) {
        super(mod);
    }

    @Override
    public String getName() {
        return "renamehomei";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("renamehome");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/renamehomei <old> <new>";
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
        if (args.length != 2) {
            throw new CommandException(getUsage(sender));
        }

        EntityPlayerMP player = (EntityPlayerMP) sender.getCommandSenderEntity();
        TeleportService.RenameHomeResult result = mod.getTeleportService().renameHome(player, args[0], args[1]);
        switch (result) {
            case RENAMED:
                send(sender, mod.getConfigService().getTeleportConfig().commandPrefix, "&aHome renamed.");
                return;
            case SAME_NAME:
                send(sender, mod.getConfigService().getTeleportConfig().commandPrefix, "&cOld and new names are the same.");
                return;
            case TARGET_EXISTS:
                send(sender, mod.getConfigService().getTeleportConfig().commandPrefix, "&cA home with that name already exists.");
                return;
            case NOT_FOUND:
            default:
                send(sender, mod.getConfigService().getTeleportConfig().commandPrefix, "&cSource home not found.");
        }
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
