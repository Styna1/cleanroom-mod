package me.fade.shopforge.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.List;

public final class StackDisplay {

    private StackDisplay() {
    }

    public static ItemStack withDisplay(ItemStack source, String displayName, List<String> lore) {
        ItemStack stack = source.copy();
        NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        NBTTagCompound displayTag = tag.hasKey("display", 10) ? tag.getCompoundTag("display") : new NBTTagCompound();

        displayTag.setString("Name", displayName);
        NBTTagList loreList = new NBTTagList();
        for (String line : lore) {
            loreList.appendTag(new NBTTagString(line));
        }
        displayTag.setTag("Lore", loreList);

        tag.setTag("display", displayTag);
        stack.setTagCompound(tag);
        return stack;
    }
}