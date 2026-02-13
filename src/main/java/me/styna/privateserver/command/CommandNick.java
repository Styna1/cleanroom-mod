package me.styna.privateserver.command;

import me.styna.privateserver.PrivateServer;
import me.styna.privateserver.util.TextUtil;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.util.Arrays;

public class CommandNick extends BasePrivateCommand {

    public CommandNick(PrivateServer mod) {
        super(mod);
    }

    @Override
    public String getName() {
        return "nick";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/nick <name|reset>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender.getCommandSenderEntity() instanceof EntityPlayerMP)) {
            throw new CommandException("Only players can use this command.");
        }

        EntityPlayerMP player = (EntityPlayerMP) sender.getCommandSenderEntity();
        if (args.length == 0) {
            throw new CommandException(getUsage(sender));
        }

        if ("reset".equalsIgnoreCase(args[0])) {
            player.setCustomNameTag(player.getName());
            send(sender, "", "&aNickname reset.");
            return;
        }

        String nick = String.join(" ", Arrays.asList(args));
        player.setCustomNameTag(TextUtil.colorize(nick));
        player.setAlwaysRenderNameTag(true);
        send(sender, "", "&aNickname set to " + nick);
    }
}
