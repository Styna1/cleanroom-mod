package me.styna.privateserver.command;

import me.styna.privateserver.PrivateServer;
import me.styna.privateserver.discord.DiscordProfile;
import me.styna.privateserver.service.DiscordRelayService;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CommandDiscord extends BasePrivateCommand {

    public CommandDiscord(PrivateServer mod) {
        super(mod);
    }

    @Override
    public String getName() {
        return "discord";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/discord <link|unlink|status> [discordUserId]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("dc");
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

        String sub = args[0].toLowerCase();
        if ("link".equals(sub)) {
            if (args.length != 2) {
                throw new CommandException("/discord link <discordUserId>");
            }
            DiscordRelayService.LinkResult result = mod.getDiscordRelayService().link(player.getUniqueID(), args[1]);
            if (!result.isSuccess()) {
                send(sender, mod.getConfigService().getChatConfig().commandPrefix, "&c" + result.getMessage());
                return;
            }
            send(sender, mod.getConfigService().getChatConfig().commandPrefix, "&a" + result.getMessage());
            return;
        }

        if ("unlink".equals(sub)) {
            boolean removed = mod.getDiscordRelayService().unlink(player.getUniqueID());
            if (!removed) {
                send(sender, mod.getConfigService().getChatConfig().commandPrefix, "&cNo linked account found.");
                return;
            }
            send(sender, mod.getConfigService().getChatConfig().commandPrefix, "&aDiscord account unlinked.");
            return;
        }

        if ("status".equals(sub)) {
            Optional<DiscordProfile> linked = mod.getDiscordRelayService().getLinkedProfile(player.getUniqueID());
            if (!linked.isPresent()) {
                send(sender, mod.getConfigService().getChatConfig().commandPrefix, "&7No discord account linked.");
                return;
            }
            DiscordProfile profile = linked.get();
            send(sender, mod.getConfigService().getChatConfig().commandPrefix,
                    "&aLinked as &e" + profile.getDisplayName() + " &7(" + profile.getDiscordUserId() + ")");
            return;
        }

        throw new CommandException(getUsage(sender));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, net.minecraft.util.math.BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, Arrays.asList("link", "unlink", "status"));
        }
        return Collections.emptyList();
    }
}
