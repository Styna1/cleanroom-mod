package me.fade.shopforge.config.model;

import java.util.ArrayList;
import java.util.List;

public class ShopConfig {

    public int schemaVersion = 1;
    public String currencyPrefix = "$";
    public List<ShopCategory> categories = new ArrayList<>();

    public ShopConfig copy() {
        ShopConfig copy = new ShopConfig();
        copy.schemaVersion = this.schemaVersion;
        copy.currencyPrefix = this.currencyPrefix;
        copy.categories = new ArrayList<>(this.categories.size());
        for (ShopCategory category : this.categories) {
            copy.categories.add(category.copy());
        }
        return copy;
    }

    public ShopCategory findCategoryById(String id) {
        for (ShopCategory category : categories) {
            if (category.id.equalsIgnoreCase(id)) {
                return category;
            }
        }
        return null;
    }
}