package me.styna.privateserver.command;

import me.styna.privateserver.PrivateServer;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;

public class CommandPay extends BasePrivateCommand {

    public CommandPay(PrivateServer mod) {
        super(mod);
    }

    @Override
    public String getName() {
        return "pay";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/pay <player> <amount>";
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
        if (!mod.getEconomyService().isEnabled()) {
            send(sender, mod.getConfigService().getEconomyConfig().commandPrefix, "&cEconomy module is disabled.");
            return;
        }
        if (args.length != 2) {
            throw new CommandException(getUsage(sender));
        }

        EntityPlayerMP payer = (EntityPlayerMP) sender.getCommandSenderEntity();
        EntityPlayerMP target = getPlayer(server, sender, args[0]);
        if (payer.getUniqueID().equals(target.getUniqueID())) {
            throw new CommandException("You cannot pay yourself.");
        }

        double amount = parsePositiveAmount(args[1]);
        if (amount <= 0.0D) {
            throw new CommandException("Amount must be greater than zero.");
        }

        double payerBalance = mod.getEconomyService().getBalance(payer);
        if (payerBalance < amount) {
            send(sender, mod.getConfigService().getEconomyConfig().commandPrefix, "&cYou do not have enough money.");
            return;
        }

        double updatedPayerBalance = mod.getEconomyService().withdraw(payer.getUniqueID(), payer.getName(), amount);
        double updatedTargetBalance = mod.getEconomyService().deposit(target.getUniqueID(), target.getName(), amount);

        send(payer, mod.getConfigService().getEconomyConfig().commandPrefix,
                "&aYou paid &e" + target.getName() + " &a$" + String.format("%.2f", amount) + "&a. New balance: &e$" + String.format("%.2f", updatedPayerBalance));
        send(target, mod.getConfigService().getEconomyConfig().commandPrefix,
                "&aYou received &e$" + String.format("%.2f", amount) + " &afrom &e" + payer.getName() + "&a. New balance: &e$" + String.format("%.2f", updatedTargetBalance));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }
        return Collections.emptyList();
    }
}
