package me.fade.shopforge.config.model;

public class ShopItemEntry {

    public String itemId = "minecraft:stone";
    public int meta = 0;
    public int count = 1;
    public double price = 10.0D;

    public ShopItemEntry copy() {
        ShopItemEntry entry = new ShopItemEntry();
        entry.itemId = this.itemId;
        entry.meta = this.meta;
        entry.count = this.count;
        entry.price = this.price;
        return entry;
    }
}