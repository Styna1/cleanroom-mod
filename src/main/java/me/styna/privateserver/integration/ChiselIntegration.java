package me.styna.privateserver.integration;

import me.styna.privateserver.PrivateServer;
import me.styna.privateserver.item.ModItems;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public final class ChiselIntegration {

    private static final String CHISEL_MOD_ID = "chisel";
    private static final String ADD_VARIATION_KEY = "add_variation";
    private static final String COCAINE_GROUP = "privateserver:cocaine";

    private ChiselIntegration() {
    }

    public static void registerSugarToCocaineVariations() {
        if (!Loader.isModLoaded(CHISEL_MOD_ID)) {
            return;
        }

        sendVariation(new ItemStack(Items.SUGAR));
        sendVariation(new ItemStack(ModItems.COCAINE));
        PrivateServer.LOGGER.info("Registered Chisel sugar<->cocaine variations.");
    }

    private static void sendVariation(ItemStack stack) {
        NBTTagCompound message = new NBTTagCompound();
        message.setString("group", COCAINE_GROUP);
        message.setTag("stack", stack.writeToNBT(new NBTTagCompound()));
        FMLInterModComms.sendMessage(CHISEL_MOD_ID, ADD_VARIATION_KEY, message);
    }
}
