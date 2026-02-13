package me.fade.shopforge.proxy;

import me.fade.shopforge.gui.GuiShop;
import me.fade.shopforge.gui.ShopContainer;

public class ClientProxy extends CommonProxy {

    @Override
    public Object createShopGui(ShopContainer container) {
        return new GuiShop(container);
    }
}