package me.fade.shopforge;

import me.fade.shopforge.command.CommandAdminShop;
import me.fade.shopforge.command.CommandShop;
import me.fade.shopforge.config.ShopConfigService;
import me.fade.shopforge.gui.ShopGuiHandler;
import me.fade.shopforge.proxy.CommonProxy;
import me.fade.shopforge.service.EconomyBridge;
import me.fade.shopforge.service.ShopService;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, acceptableRemoteVersions = "*")
public class ShopForgeMod {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Mod.Instance(Tags.MOD_ID)
    public static ShopForgeMod INSTANCE;

    @SidedProxy(clientSide = "me.fade.shopforge.proxy.ClientProxy", serverSide = "me.fade.shopforge.proxy.CommonProxy")
    public static CommonProxy PROXY;

    private ShopService shopService;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        File modConfigDir = new File(event.getModConfigurationDirectory(), Tags.MOD_ID);
        ShopConfigService configService = new ShopConfigService(modConfigDir);
        configService.load();

        this.shopService = new ShopService(configService, new EconomyBridge(LOGGER));
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new ShopGuiHandler(this));

        LOGGER.info("Initialized {} with config path {}", Tags.MOD_ID, modConfigDir.getAbsolutePath());
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandShop(this));
        event.registerServerCommand(new CommandAdminShop(this));
    }

    public ShopService getShopService() {
        return shopService;
    }
}