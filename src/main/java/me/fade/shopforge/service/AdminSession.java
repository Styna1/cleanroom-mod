package me.fade.shopforge.service;

import me.fade.shopforge.config.model.ShopCategory;
import me.fade.shopforge.config.model.ShopConfig;

import java.util.UUID;

public class AdminSession {

    private final UUID ownerId;
    private final ShopConfig workingConfig;
    private String movingCategoryId;
    private String selectedCategoryId;

    public AdminSession(UUID ownerId, ShopConfig workingConfig) {
        this.ownerId = ownerId;
        this.workingConfig = workingConfig;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public ShopConfig getWorkingConfig() {
        return workingConfig;
    }

    public String getMovingCategoryId() {
        return movingCategoryId;
    }

    public void setMovingCategoryId(String movingCategoryId) {
        this.movingCategoryId = movingCategoryId;
    }

    public String getSelectedCategoryId() {
        return selectedCategoryId;
    }

    public void setSelectedCategoryId(String selectedCategoryId) {
        this.selectedCategoryId = selectedCategoryId;
    }

    public ShopCategory selectedCategory() {
        if (selectedCategoryId == null) {
            return null;
        }
        return workingConfig.findCategoryById(selectedCategoryId);
    }
}