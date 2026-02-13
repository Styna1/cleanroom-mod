package me.fade.shopforge.gui;

import me.fade.shopforge.ShopForgeMod;
import me.fade.shopforge.ShopGuiIds;
import me.fade.shopforge.config.model.ShopCategory;
import me.fade.shopforge.config.model.ShopConfig;
import me.fade.shopforge.config.model.ShopItemEntry;
import me.fade.shopforge.service.AdminSession;
import me.fade.shopforge.service.EconomyBridge;
import me.fade.shopforge.service.ShopService;
import me.fade.shopforge.util.ItemResolver;
import me.fade.shopforge.util.MessageUtil;
import me.fade.shopforge.util.StackDisplay;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShopContainer extends Container {

    private static final int MENU_SIZE = 54;
    private static final int INTERACTIVE_SIZE = 45;

    private final InventoryBasic menuInventory;
    private final EntityPlayer owner;
    private final UUID ownerId;
    private final ShopService shopService;
    private final boolean adminView;
    private final boolean categoryView;
    private final boolean clientOnly;

    private int categoryIndex;
    private String title;

    private final Map<Integer, Integer> slotToCategoryIndex = new HashMap<>();
    private final Map<Integer, Integer> slotToItemIndex = new HashMap<>();

    public ShopContainer(InventoryPlayer playerInventory, ShopService shopService, int guiId, int categoryIndex, boolean clientOnly) {
        this.menuInventory = new InventoryBasic("shop_menu", false, MENU_SIZE);
        this.owner = playerInventory.player;
        this.ownerId = playerInventory.player.getUniqueID();
        this.shopService = shopService;
        this.adminView = guiId == ShopGuiIds.ADMIN_CATEGORIES || guiId == ShopGuiIds.ADMIN_ITEMS;
        this.categoryView = guiId == ShopGuiIds.SHOP_CATEGORIES || guiId == ShopGuiIds.ADMIN_CATEGORIES;
        this.categoryIndex = categoryIndex;
        this.clientOnly = clientOnly;
        this.title = adminView ? "Admin Shop" : "Shop";

        addMenuSlots();
        addPlayerInventory(playerInventory);

        if (!clientOnly) {
            refreshFromServer();
        }
    }

    public void refreshFromServer() {
        if (clientOnly || shopService == null || owner.world.isRemote) {
            return;
        }

        clearMenu();
        if (!(owner instanceof EntityPlayerMP)) {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) owner;
        ShopConfig config = shopService.viewConfig(player, adminView);
        if (categoryView) {
            buildCategoryMenu(config);
        } else {
            buildItemMenu(config);
        }
        detectAndSendChanges();
    }

    private void addMenuSlots() {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                int slot = col + row * 9;
                addSlotToContainer(new ReadonlySlot(menuInventory, slot, 8 + col * 18, 18 + row * 18));
            }
        }
    }

    private void addPlayerInventory(InventoryPlayer playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInventory, col, 8 + col * 18, 198));
        }
    }

    private void clearMenu() {
        slotToCategoryIndex.clear();
        slotToItemIndex.clear();
        for (int i = 0; i < MENU_SIZE; i++) {
            menuInventory.setInventorySlotContents(i, ItemStack.EMPTY);
        }
    }

    private void buildCategoryMenu(ShopConfig config) {
        title = adminView ? "Admin Shop Categories" : "Shop Categories";
        if (config.categories.isEmpty()) {
            menuInventory.setInventorySlotContents(22, StackDisplay.withDisplay(new ItemStack(Items.BARRIER), "No categories", Arrays.asList("Use /adminshop addcategory ...")));
            return;
        }

        Map<Integer, Integer> layout = computeCategorySlotLayout(config.categories);
        for (Map.Entry<Integer, Integer> entry : layout.entrySet()) {
            int slot = entry.getKey();
            int index = entry.getValue();
            ShopCategory category = config.categories.get(index);

            ItemStack icon = ItemResolver.resolve(category.iconItemId, category.iconMeta, 1);
            if (icon.isEmpty()) {
                icon = new ItemStack(Items.CHEST);
            }

            List<String> lore = new ArrayList<>();
            if (adminView) {
                lore.add("ID: " + category.id);
                lore.add("Items: " + category.items.size());
                lore.add("Left-click: open items");
                lore.add("Sneak + click: move mode");
            } else {
                lore.add("Click to open");
            }

            ItemStack display = StackDisplay.withDisplay(icon, "\u00A7e" + category.name, lore);
            menuInventory.setInventorySlotContents(slot, display);
            slotToCategoryIndex.put(slot, index);
        }

        if (adminView) {
            menuInventory.setInventorySlotContents(45, controlItem(Items.DYE, 10, "\u00A7aSave Session", "Applies edits and syncs all open shops"));
            menuInventory.setInventorySlotContents(46, controlItem(Items.DYE, 1, "\u00A7cCancel Session", "Discard your current edits"));
            menuInventory.setInventorySlotContents(47, controlItem(Items.ANVIL, 0, "\u00A7fAdd Category", "/adminshop addcategory <id> <name>"));
            menuInventory.setInventorySlotContents(48, controlItem(Items.BOOK, 0, "\u00A7fMove Category", "Sneak-click category, then click a slot"));
            menuInventory.setInventorySlotContents(53, controlItem(Items.COMPASS, 0, "\u00A7bOpen Player View", "Preview normal /shop GUI"));
        }
    }

    private void buildItemMenu(ShopConfig config) {
        ShopCategory category = resolveActiveCategory(config);
        if (category == null) {
            title = adminView ? "Admin Shop Items" : "Shop Items";
            menuInventory.setInventorySlotContents(22, StackDisplay.withDisplay(new ItemStack(Items.BARRIER), "No category", Arrays.asList("Use /shop or /adminshop")));
            return;
        }

        title = (adminView ? "Admin: " : "Shop: ") + category.name;

        List<Integer> slots = centeredSlots(Math.min(INTERACTIVE_SIZE, category.items.size()));
        for (int i = 0; i < slots.size(); i++) {
            int slot = slots.get(i);
            ShopItemEntry entry = category.items.get(i);
            ItemStack stack = ItemResolver.resolve(entry.itemId, entry.meta, entry.count);
            if (stack.isEmpty()) {
                stack = new ItemStack(Items.BARRIER);
            }

            List<String> lore = new ArrayList<>();
            lore.add("Price: " + MessageUtil.formatPrice(config.currencyPrefix, entry.price));
            if (adminView) {
                lore.add("Left-click: +1 price");
                lore.add("Right-click: -1 price");
                lore.add("Sneak click: +/-10");
                lore.add("Shift-click: remove entry");
                lore.add("Index: " + i);
                lore.add("Item ID: " + entry.itemId + " @" + entry.meta);
            } else {
                lore.add("Click to buy " + entry.count);
            }

            ItemStack display = StackDisplay.withDisplay(stack, "\u00A7e" + stack.getDisplayName(), lore);
            menuInventory.setInventorySlotContents(slot, display);
            slotToItemIndex.put(slot, i);
        }

        menuInventory.setInventorySlotContents(45, controlItem(Items.ARROW, 0, "\u00A7fBack", "Return to categories"));
        if (adminView) {
            menuInventory.setInventorySlotContents(46, controlItem(Items.DYE, 10, "\u00A7aSave Session", "Applies edits and syncs all open shops"));
            menuInventory.setInventorySlotContents(47, controlItem(Items.DYE, 1, "\u00A7cCancel Session", "Discard your current edits"));
            menuInventory.setInventorySlotContents(48, controlItem(Items.CHEST, 0, "\u00A7fAdd Item From Inventory", "Pick an item below, click empty slot above"));
            menuInventory.setInventorySlotContents(49, controlItem(Items.BOOK, 0, "\u00A7fAdd Item By ID", "/adminshop additem <category> <id> [meta] [count] [price]"));
        } else {
            if (shopService.getEconomyBridge().isAvailable()) {
                menuInventory.setInventorySlotContents(53, controlItem(Items.GOLD_NUGGET, 0, "\u00A7aEconomy Online", "Purchases are enabled"));
            } else {
                menuInventory.setInventorySlotContents(53, controlItem(Items.BARRIER, 0, "\u00A7cEconomy Missing", "Install privateserver economy mod"));
            }
        }
    }

    private ShopCategory resolveActiveCategory(ShopConfig config) {
        if (config.categories.isEmpty()) {
            return null;
        }

        if (adminView && owner instanceof EntityPlayerMP && shopService != null) {
            AdminSession session = shopService.getSession((EntityPlayerMP) owner);
            if (session != null) {
                ShopCategory selected = session.selectedCategory();
                if (selected != null) {
                    int foundIndex = config.categories.indexOf(selected);
                    if (foundIndex >= 0) {
                        categoryIndex = foundIndex;
                    }
                    return selected;
                }
            }
        }

        if (categoryIndex < 0 || categoryIndex >= config.categories.size()) {
            categoryIndex = 0;
        }

        ShopCategory fallback = config.categories.get(categoryIndex);
        if (adminView && owner instanceof EntityPlayerMP && shopService != null) {
            AdminSession session = shopService.getSession((EntityPlayerMP) owner);
            if (session != null) {
                session.setSelectedCategoryId(fallback.id);
            }
        }
        return fallback;
    }

    private Map<Integer, Integer> computeCategorySlotLayout(List<ShopCategory> categories) {
        Map<Integer, Integer> slotToCategory = new HashMap<>();
        List<Integer> autoCategoryIndexes = new ArrayList<>();

        for (int i = 0; i < categories.size(); i++) {
            int preferredSlot = categories.get(i).preferredSlot;
            if (preferredSlot >= 0 && preferredSlot < INTERACTIVE_SIZE && !slotToCategory.containsKey(preferredSlot)) {
                slotToCategory.put(preferredSlot, i);
            } else {
                autoCategoryIndexes.add(i);
            }
        }

        List<Integer> centeredSlots = centeredSlots(autoCategoryIndexes.size());
        int autoIndex = 0;
        for (int slot : centeredSlots) {
            if (autoIndex >= autoCategoryIndexes.size()) {
                break;
            }
            if (slotToCategory.containsKey(slot)) {
                continue;
            }
            slotToCategory.put(slot, autoCategoryIndexes.get(autoIndex));
            autoIndex++;
        }

        for (int slot = 0; slot < INTERACTIVE_SIZE && autoIndex < autoCategoryIndexes.size(); slot++) {
            if (!slotToCategory.containsKey(slot)) {
                slotToCategory.put(slot, autoCategoryIndexes.get(autoIndex));
                autoIndex++;
            }
        }

        return slotToCategory;
    }

    private List<Integer> centeredSlots(int count) {
        List<Integer> slots = new ArrayList<>();
        if (count <= 0) {
            return slots;
        }

        int rowsNeeded = Math.min(5, Math.max(1, (count + 8) / 9));
        int startRow = (5 - rowsNeeded) / 2;

        int remaining = count;
        for (int row = 0; row < rowsNeeded; row++) {
            int inRow = Math.min(9, remaining);
            int startCol = (9 - inRow) / 2;
            for (int col = 0; col < inRow; col++) {
                slots.add((startRow + row) * 9 + startCol + col);
            }
            remaining -= inRow;
            if (remaining <= 0) {
                break;
            }
        }

        return slots;
    }

    private ItemStack controlItem(net.minecraft.item.Item item, int meta, String name, String lore) {
        return StackDisplay.withDisplay(new ItemStack(item, 1, meta), name, Arrays.asList(lore));
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        if (slotId >= 0 && slotId < MENU_SIZE) {
            handleMenuClick(slotId, dragType, clickTypeIn, player);
            return ItemStack.EMPTY;
        }
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    private void handleMenuClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        if (player.world.isRemote || !(player instanceof EntityPlayerMP) || shopService == null) {
            return;
        }

        EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
        if (adminView) {
            if (categoryView) {
                handleAdminCategoryClick(slotId, serverPlayer);
            } else {
                handleAdminItemClick(slotId, dragType, clickTypeIn, serverPlayer);
            }
            return;
        }

        if (categoryView) {
            Integer clickedCategoryIndex = slotToCategoryIndex.get(slotId);
            if (clickedCategoryIndex != null) {
                serverPlayer.openGui(ShopForgeMod.INSTANCE, ShopGuiIds.SHOP_ITEMS, serverPlayer.world, clickedCategoryIndex, 0, 0);
            }
            return;
        }

        if (slotId == 45) {
            serverPlayer.openGui(ShopForgeMod.INSTANCE, ShopGuiIds.SHOP_CATEGORIES, serverPlayer.world, 0, 0, 0);
            return;
        }

        Integer itemIndex = slotToItemIndex.get(slotId);
        if (itemIndex != null) {
            handlePurchase(serverPlayer, itemIndex);
        }
    }

    private void handlePurchase(EntityPlayerMP player, int itemIndex) {
        ShopConfig config = shopService.liveConfigCopy();
        if (config.categories.isEmpty()) {
            MessageUtil.error(player, "No categories configured.");
            return;
        }

        int safeCategoryIndex = Math.max(0, Math.min(categoryIndex, config.categories.size() - 1));
        ShopCategory category = config.categories.get(safeCategoryIndex);
        if (itemIndex < 0 || itemIndex >= category.items.size()) {
            MessageUtil.error(player, "That item no longer exists.");
            refreshFromServer();
            return;
        }

        ShopItemEntry entry = category.items.get(itemIndex);
        ItemStack payout = ItemResolver.resolve(entry.itemId, entry.meta, entry.count);
        if (payout.isEmpty()) {
            MessageUtil.error(player, "This shop item is invalid: " + entry.itemId);
            return;
        }

        EconomyBridge.WithdrawResult result = shopService.getEconomyBridge().withdraw(player, entry.price);
        if (!result.isSuccess()) {
            if ("Insufficient balance.".equals(result.getFailureReason())) {
                MessageUtil.error(player, "You cannot afford this item. Cost: " + MessageUtil.formatPrice(config.currencyPrefix, entry.price));
            } else {
                MessageUtil.error(player, "Purchase failed: " + result.getFailureReason());
            }
            return;
        }

        ItemStack reward = payout.copy();
        boolean fullyAdded = player.inventory.addItemStackToInventory(reward);
        if (!fullyAdded) {
            player.dropItem(reward, false);
        }
        player.inventoryContainer.detectAndSendChanges();

        MessageUtil.info(player, "Purchased " + payout.getCount() + "x " + payout.getDisplayName()
                + " for " + MessageUtil.formatPrice(config.currencyPrefix, entry.price)
                + ". Balance: " + MessageUtil.formatPrice(config.currencyPrefix, result.getResultingBalance()));
    }

    private void handleAdminCategoryClick(int slotId, EntityPlayerMP player) {
        if (slotId == 45) {
            if (shopService.commitSession(player)) {
                shopService.refreshOpenContainers(player.getServer());
                MessageUtil.info(player, "Admin session saved and synced.");
                player.closeScreen();
            } else {
                MessageUtil.error(player, "No active admin session.");
            }
            return;
        }

        if (slotId == 46) {
            shopService.cancelSession(player);
            MessageUtil.info(player, "Admin session canceled.");
            player.closeScreen();
            return;
        }

        if (slotId == 47) {
            MessageUtil.info(player, "Use /adminshop addcategory <id> <name>");
            return;
        }

        if (slotId == 48) {
            MessageUtil.info(player, "Sneak-click a category, then click target slot.");
            return;
        }

        if (slotId == 53) {
            player.openGui(ShopForgeMod.INSTANCE, ShopGuiIds.SHOP_CATEGORIES, player.world, 0, 0, 0);
            return;
        }

        AdminSession session = shopService.getSession(player);
        if (session == null) {
            MessageUtil.error(player, "No active admin session.");
            return;
        }

        ShopConfig config = session.getWorkingConfig();
        if (session.getMovingCategoryId() != null) {
            ShopCategory movingCategory = config.findCategoryById(session.getMovingCategoryId());
            if (movingCategory != null) {
                Integer targetIndex = slotToCategoryIndex.get(slotId);
                if (targetIndex != null) {
                    ShopCategory targetCategory = config.categories.get(targetIndex);
                    if (!targetCategory.id.equalsIgnoreCase(movingCategory.id)) {
                        targetCategory.preferredSlot = -1;
                    }
                }
                movingCategory.preferredSlot = slotId;
                MessageUtil.info(player, "Moved category " + movingCategory.name + " to slot " + slotId + ".");
            }
            session.setMovingCategoryId(null);
            refreshFromServer();
            return;
        }

        Integer categorySlotIndex = slotToCategoryIndex.get(slotId);
        if (categorySlotIndex == null || categorySlotIndex < 0 || categorySlotIndex >= config.categories.size()) {
            return;
        }

        ShopCategory selectedCategory = config.categories.get(categorySlotIndex);
        if (player.isSneaking()) {
            session.setMovingCategoryId(selectedCategory.id);
            MessageUtil.info(player, "Move mode for " + selectedCategory.name + ". Click destination slot.");
            return;
        }

        session.setSelectedCategoryId(selectedCategory.id);
        categoryIndex = categorySlotIndex;
        player.openGui(ShopForgeMod.INSTANCE, ShopGuiIds.ADMIN_ITEMS, player.world, categoryIndex, 0, 0);
    }

    private void handleAdminItemClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayerMP player) {
        if (slotId == 45) {
            player.openGui(ShopForgeMod.INSTANCE, ShopGuiIds.ADMIN_CATEGORIES, player.world, 0, 0, 0);
            return;
        }

        if (slotId == 46) {
            if (shopService.commitSession(player)) {
                shopService.refreshOpenContainers(player.getServer());
                MessageUtil.info(player, "Admin session saved and synced.");
                player.closeScreen();
            } else {
                MessageUtil.error(player, "No active admin session.");
            }
            return;
        }

        if (slotId == 47) {
            shopService.cancelSession(player);
            MessageUtil.info(player, "Admin session canceled.");
            player.closeScreen();
            return;
        }

        if (slotId == 48) {
            MessageUtil.info(player, "Pick an item from your inventory, then click an empty slot in this menu.");
            return;
        }

        if (slotId == 49) {
            MessageUtil.info(player, "Use /adminshop additem <category> <id> [meta] [count] [price]");
            return;
        }

        AdminSession session = shopService.getSession(player);
        if (session == null) {
            MessageUtil.error(player, "No active admin session.");
            return;
        }

        ShopCategory category = resolveAdminSessionCategory(session);
        if (category == null) {
            MessageUtil.error(player, "No category selected.");
            return;
        }

        if (slotId < 0 || slotId >= INTERACTIVE_SIZE) {
            return;
        }

        Integer itemIndex = slotToItemIndex.get(slotId);
        if (itemIndex != null && itemIndex >= 0 && itemIndex < category.items.size()) {
            ShopItemEntry itemEntry = category.items.get(itemIndex);

            if (clickTypeIn == ClickType.QUICK_MOVE) {
                category.items.remove((int) itemIndex);
                MessageUtil.info(player, "Removed item from category.");
                refreshFromServer();
                return;
            }

            if (clickTypeIn == ClickType.PICKUP) {
                double delta = player.isSneaking() ? 10.0D : 1.0D;
                if (dragType == 0) {
                    itemEntry.price += delta;
                    MessageUtil.info(player, "Price increased to " + itemEntry.price + ".");
                } else if (dragType == 1) {
                    itemEntry.price = Math.max(0.0D, itemEntry.price - delta);
                    MessageUtil.info(player, "Price decreased to " + itemEntry.price + ".");
                }
                refreshFromServer();
                return;
            }

            if (clickTypeIn == ClickType.CLONE) {
                category.items.remove((int) itemIndex);
                MessageUtil.info(player, "Removed item from category.");
                refreshFromServer();
            }
            return;
        }

        ItemStack cursor = player.inventory.getItemStack();
        if (!cursor.isEmpty() && clickTypeIn == ClickType.PICKUP) {
            String itemId = ItemResolver.registryId(cursor);
            if (itemId.isEmpty()) {
                MessageUtil.error(player, "Could not resolve item ID.");
                return;
            }

            ShopItemEntry newEntry = new ShopItemEntry();
            newEntry.itemId = itemId;
            newEntry.meta = cursor.getMetadata();
            newEntry.count = cursor.getCount();
            newEntry.price = 10.0D;
            category.items.add(newEntry);

            MessageUtil.info(player, "Added " + cursor.getDisplayName() + " to " + category.name + " with default price 10.");
            refreshFromServer();
        }
    }

    private ShopCategory resolveAdminSessionCategory(AdminSession session) {
        ShopCategory selected = session.selectedCategory();
        if (selected != null) {
            return selected;
        }

        ShopConfig config = session.getWorkingConfig();
        if (config.categories.isEmpty()) {
            return null;
        }

        int safeIndex = Math.max(0, Math.min(categoryIndex, config.categories.size() - 1));
        ShopCategory fallback = config.categories.get(safeIndex);
        session.setSelectedCategoryId(fallback.id);
        return fallback;
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
        if (slotIn.inventory == menuInventory) {
            return false;
        }
        return super.canMergeSlot(stack, slotIn);
    }

    @Override
    public boolean canDragIntoSlot(Slot slotIn) {
        return slotIn.inventory != menuInventory;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        IInventory top = this.menuInventory;
        top.closeInventory(playerIn);
    }

    public boolean isAdminView() {
        return adminView;
    }

    public boolean isCategoryView() {
        return categoryView;
    }

    public String getTitle() {
        return title;
    }

    public UUID getOwnerId() {
        return ownerId;
    }
}