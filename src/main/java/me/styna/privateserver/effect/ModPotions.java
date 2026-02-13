package me.styna.privateserver.effect;

import me.styna.privateserver.Tags;
import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ModPotions {

    public static final Potion DIZZY = new DizzyPotion()
            .setRegistryName(Tags.MOD_ID, "dizzy")
            .setPotionName("effect." + Tags.MOD_ID + ".dizzy");

    private ModPotions() {
    }

    @SubscribeEvent
    public static void onRegisterPotions(RegistryEvent.Register<Potion> event) {
        event.getRegistry().register(DIZZY);
    }
}
