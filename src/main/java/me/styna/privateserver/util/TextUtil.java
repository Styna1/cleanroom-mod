package me.styna.privateserver.util;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;

public final class TextUtil {

    private TextUtil() {
    }

    public static String colorize(String text) {
        if (text == null) {
            return "";
        }
        return text.replace('&', '\u00A7');
    }

    public static TextComponentString component(String text) {
        return new TextComponentString(colorize(text));
    }

    public static void send(ICommandSender sender, String text) {
        sender.sendMessage(component(text));
    }

    public static void actionBar(EntityPlayerMP player, String text) {
        player.sendStatusMessage(component(text), true);
    }
}
