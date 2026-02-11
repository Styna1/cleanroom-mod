package me.styna.privateserver.item;

import me.styna.privateserver.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ModItems {

    public static final Item COCAINE = new CocaineItem()
            .setRegistryName(Tags.MOD_ID, "cocaine")
            .setTranslationKey(Tags.MOD_ID + ".cocaine")
            .setCreativeTab(CreativeTabs.MISC);

    private ModItems() {
    }

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(COCAINE);
    }
}
