package me.styna.privateserver.command;

import me.styna.privateserver.PrivateServer;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.util.Collections;
import java.util.List;

public class CommandBal extends BasePrivateCommand {

    public CommandBal(PrivateServer mod) {
        super(mod);
    }

    @Override
    public String getName() {
        return "bal";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/bal [player]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!mod.getEconomyService().isEnabled()) {
            send(sender, mod.getConfigService().getEconomyConfig().commandPrefix, "&cEconomy module is disabled.");
            return;
        }

        EntityPlayerMP target;
        if (args.length == 0) {
            if (!(sender.getCommandSenderEntity() instanceof EntityPlayerMP)) {
                throw new CommandException("Console must provide a player name.");
            }
            target = (EntityPlayerMP) sender.getCommandSenderEntity();
        } else {
            target = getPlayer(server, sender, args[0]);
        }

        double balance = mod.getEconomyService().getBalance(target);
        send(sender, mod.getConfigService().getEconomyConfig().commandPrefix, "&a" + target.getName() + "'s balance: &e$" + String.format("%.2f", balance));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, net.minecraft.util.math.BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }
        return Collections.emptyList();
    }
}
