package me.fade.shopforge.gui;

import me.fade.shopforge.ShopForgeMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class ShopGuiHandler implements IGuiHandler {

    private final ShopForgeMod mod;

    public ShopGuiHandler(ShopForgeMod mod) {
        this.mod = mod;
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return new ShopContainer(player.inventory, mod.getShopService(), id, x, false);
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        ShopContainer container = new ShopContainer(player.inventory, null, id, x, true);
        return ShopForgeMod.PROXY.createShopGui(container);
    }
}