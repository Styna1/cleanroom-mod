package me.styna.privateserver.integration;

import me.styna.privateserver.PrivateServer;
import me.styna.privateserver.item.ModItems;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.oredict.OreDictionary;

public final class ChiselIntegration {

    private static final String CHISEL_MOD_ID = "chisel";
    private static final String GROUP_ORE_KEY = "group:ore";
    private static final String COCAINE_GROUP = "privateserver:cocaine";
    private static final String COCAINE_ORE = "privateserverCocaineMaterial";

    private ChiselIntegration() {
    }

    public static void registerSugarToCocaineVariations() {
        if (!Loader.isModLoaded(CHISEL_MOD_ID)) {
            return;
        }

        Item cocaineItem = ModItems.COCAINE;
        if (Item.REGISTRY.getNameForObject(cocaineItem) == null) {
            PrivateServer.LOGGER.warn("Skipping Chisel integration: cocaine item is not registered yet.");
            return;
        }

        OreDictionary.registerOre(COCAINE_ORE, Items.SUGAR);
        OreDictionary.registerOre(COCAINE_ORE, cocaineItem);
        FMLInterModComms.sendMessage(CHISEL_MOD_ID, GROUP_ORE_KEY, COCAINE_GROUP + "|" + COCAINE_ORE);
        PrivateServer.LOGGER.info("Registered Chisel sugar<->cocaine variations.");
    }
}
