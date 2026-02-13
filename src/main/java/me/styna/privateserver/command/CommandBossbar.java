package me.styna.privateserver.command;

import me.styna.privateserver.PrivateServer;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.Arrays;

public class CommandBossbar extends BasePrivateCommand {

    public CommandBossbar(PrivateServer mod) {
        super(mod);
    }

    @Override
    public String getName() {
        return "bossbar";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/bossbar <off|message...>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            throw new CommandException(getUsage(sender));
        }

        if ("off".equalsIgnoreCase(args[0])) {
            mod.getBossbarService().hide();
            send(sender, "", "&aBossbar cleared.");
            return;
        }

        String message = String.join(" ", Arrays.asList(args));
        mod.getBossbarService().show(server, message);
        send(sender, "", "&aBossbar updated.");
    }
}
