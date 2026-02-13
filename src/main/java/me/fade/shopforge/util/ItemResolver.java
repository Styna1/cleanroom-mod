package me.fade.shopforge.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public final class ItemResolver {

    private ItemResolver() {
    }

    public static ItemStack resolve(String itemId, int meta, int count) {
        Item item = Item.getByNameOrId(itemId);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        int safeMeta = Math.max(0, meta);
        int safeCount = Math.max(1, Math.min(Math.max(1, count), item.getItemStackLimit()));
        return new ItemStack(item, safeCount, safeMeta);
    }

    public static String registryId(ItemStack stack) {
        if (stack.isEmpty()) {
            return "";
        }
        ResourceLocation key = stack.getItem().getRegistryName();
        return key == null ? "" : key.toString();
    }
}