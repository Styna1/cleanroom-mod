package me.styna.privateserver.effect;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class DizzyPotion extends Potion {

    protected DizzyPotion() {
        super(true, 0x8F55D6);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier) {
        int nauseaDuration = 80 + (amplifier * 20);
        entityLivingBaseIn.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, nauseaDuration, 0, true, true));
    }
}
