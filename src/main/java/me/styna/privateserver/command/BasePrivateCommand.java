package me.styna.privateserver.command;

import me.styna.privateserver.PrivateServer;
import me.styna.privateserver.util.TextUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public abstract class BasePrivateCommand extends CommandBase {

    protected final PrivateServer mod;

    protected BasePrivateCommand(PrivateServer mod) {
        this.mod = mod;
    }

    protected void send(ICommandSender sender, String modulePrefix, String message) {
        String mainPrefix = mod.getConfigService().getMainConfig().commandPrefix;
        TextUtil.send(sender, mainPrefix + modulePrefix + message);
    }

    protected double parsePositiveAmount(String input) throws CommandException {
        try {
            double value = Double.parseDouble(input);
            if (value < 0.0D) {
                throw new NumberFormatException("negative");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new CommandException("Amount must be a non-negative number.");
        }
    }
}
