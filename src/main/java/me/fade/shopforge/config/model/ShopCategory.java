package me.fade.shopforge.config.model;

import java.util.ArrayList;
import java.util.List;

public class ShopCategory {

    public String id = "blocks";
    public String name = "Blocks";
    public String iconItemId = "minecraft:stone";
    public int iconMeta = 0;
    public int preferredSlot = -1;
    public List<ShopItemEntry> items = new ArrayList<>();

    public ShopCategory copy() {
        ShopCategory category = new ShopCategory();
        category.id = this.id;
        category.name = this.name;
        category.iconItemId = this.iconItemId;
        category.iconMeta = this.iconMeta;
        category.preferredSlot = this.preferredSlot;
        category.items = new ArrayList<>(this.items.size());
        for (ShopItemEntry item : this.items) {
            category.items.add(item.copy());
        }
        return category;
    }
}