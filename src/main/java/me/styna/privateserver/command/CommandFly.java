package me.styna.privateserver.command;

import me.styna.privateserver.PrivateServer;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandFly extends BasePrivateCommand {

    public CommandFly(PrivateServer mod) {
        super(mod);
    }

    @Override
    public String getName() {
        return "fly";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/fly";
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

        EntityPlayerMP player = (EntityPlayerMP) sender.getCommandSenderEntity();
        boolean enabling = !player.capabilities.allowFlying;
        if (enabling && !player.canUseCommand(2, "fly")) {
            int cost = Math.max(0, mod.getConfigService().getMainConfig().flyEnableCostLevels);
            if (player.experienceLevel < cost) {
                send(sender, "", "&cYou need " + cost + " levels to enable fly.");
                return;
            }
            player.addExperienceLevel(-cost);
        }

        player.capabilities.allowFlying = enabling;
        if (!enabling) {
            player.capabilities.isFlying = false;
        }
        player.sendPlayerAbilities();

        send(sender, "", enabling ? "&aFly enabled." : "&cFly disabled.");
    }
}
