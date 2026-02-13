package me.fade.shopforge.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.text.DecimalFormat;

public final class MessageUtil {

    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("0.00");

    private MessageUtil() {
    }

    public static void info(EntityPlayer player, String message) {
        player.sendMessage(new TextComponentString(TextFormatting.GREEN + "[Shop] " + TextFormatting.RESET + message));
    }

    public static void error(EntityPlayer player, String message) {
        player.sendMessage(new TextComponentString(TextFormatting.RED + "[Shop] " + TextFormatting.RESET + message));
    }

    public static String formatPrice(String prefix, double value) {
        return prefix + PRICE_FORMAT.format(value);
    }
}