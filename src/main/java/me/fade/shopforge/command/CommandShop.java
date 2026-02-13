package me.fade.shopforge.command;

import me.fade.shopforge.ShopForgeMod;
import me.fade.shopforge.ShopGuiIds;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandShop extends CommandBase {

    private final ShopForgeMod mod;

    public CommandShop(ShopForgeMod mod) {
        this.mod = mod;
    }

    @Override
    public String getName() {
        return "shop";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/shop";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        player.openGui(mod, ShopGuiIds.SHOP_CATEGORIES, player.world, 0, 0, 0);
    }
}