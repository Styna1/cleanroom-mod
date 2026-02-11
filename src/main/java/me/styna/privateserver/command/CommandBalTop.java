package me.styna.privateserver.command;

import me.styna.privateserver.PrivateServer;
import me.styna.privateserver.api.economy.EconomyApi;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.List;

public class CommandBalTop extends BasePrivateCommand {

    public CommandBalTop(PrivateServer mod) {
        super(mod);
    }

    @Override
    public String getName() {
        return "baltop";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/baltop";
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

        int limit = mod.getConfigService().getEconomyConfig().baltopLimit;
        List<EconomyApi.LeaderEntry> leaders = mod.getEconomyService().getTopBalances(limit);
        send(sender, mod.getConfigService().getEconomyConfig().commandPrefix, "&6Top balances:");
        if (leaders.isEmpty()) {
            send(sender, mod.getConfigService().getEconomyConfig().commandPrefix, "&7No balances yet.");
            return;
        }

        int index = 1;
        for (EconomyApi.LeaderEntry entry : leaders) {
            send(sender, mod.getConfigService().getEconomyConfig().commandPrefix,
                    "&e#" + index + " &f" + entry.getPlayerName() + " &7- &a$" + String.format("%.2f", entry.getBalance()));
            index++;
        }
    }
}
