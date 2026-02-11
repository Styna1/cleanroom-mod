package me.styna.privateserver.command;

import me.styna.privateserver.PrivateServer;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandEco extends BasePrivateCommand {

    public CommandEco(PrivateServer mod) {
        super(mod);
    }

    @Override
    public String getName() {
        return "eco";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/eco <take|give|set> <player> <amount>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!mod.getEconomyService().isEnabled()) {
            send(sender, mod.getConfigService().getEconomyConfig().commandPrefix, "&cEconomy module is disabled.");
            return;
        }
        if (args.length != 3) {
            throw new CommandException(getUsage(sender));
        }

        String action = args[0].toLowerCase();
        EntityPlayerMP target = getPlayer(server, sender, args[1]);
        double amount = parsePositiveAmount(args[2]);

        double balance;
        switch (action) {
            case "give":
                balance = mod.getEconomyService().deposit(target.getUniqueID(), target.getName(), amount);
                break;
            case "take":
                balance = mod.getEconomyService().withdraw(target.getUniqueID(), target.getName(), amount);
                break;
            case "set":
                balance = mod.getEconomyService().setBalance(target.getUniqueID(), target.getName(), amount);
                break;
            default:
                throw new CommandException("Unknown action: " + action);
        }

        send(sender, mod.getConfigService().getEconomyConfig().commandPrefix,
                "&aSet " + target.getName() + "'s balance to &e$" + String.format("%.2f", balance));
        send(target, mod.getConfigService().getEconomyConfig().commandPrefix,
                "&7Your balance is now &e$" + String.format("%.2f", balance));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, Arrays.asList("take", "give", "set"));
        }
        if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }
        return Collections.emptyList();
    }
}
