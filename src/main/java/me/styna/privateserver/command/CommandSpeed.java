package me.styna.privateserver.command;

import me.styna.privateserver.PrivateServer;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;

public class CommandSpeed extends BasePrivateCommand {

    public CommandSpeed(PrivateServer mod) {
        super(mod);
    }

    @Override
    public String getName() {
        return "speed";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/speed <1-10> [player]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1 || args.length > 2) {
            throw new CommandException(getUsage(sender));
        }

        int value;
        try {
            value = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            throw new CommandException("Speed must be a number from 1 to 10.");
        }
        if (value < 1 || value > 10) {
            throw new CommandException("Speed must be a number from 1 to 10.");
        }

        EntityPlayerMP target;
        if (args.length == 2) {
            target = getPlayer(server, sender, args[1]);
        } else if (sender.getCommandSenderEntity() instanceof EntityPlayerMP) {
            target = (EntityPlayerMP) sender.getCommandSenderEntity();
        } else {
            throw new CommandException("Console must provide a player name.");
        }

        float speed = value / 10.0F;
        if (target.capabilities.isFlying) {
            target.capabilities.setFlySpeed(speed);
        } else {
            target.capabilities.setPlayerWalkSpeed(speed);
        }
        target.sendPlayerAbilities();
        send(sender, "", "&aSet speed to " + value + " for " + target.getName());
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }
        return Collections.emptyList();
    }
}
