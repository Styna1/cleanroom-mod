package me.fade.shopforge.command;

import me.fade.shopforge.ShopForgeMod;
import me.fade.shopforge.ShopGuiIds;
import me.fade.shopforge.config.model.ShopCategory;
import me.fade.shopforge.config.model.ShopConfig;
import me.fade.shopforge.config.model.ShopItemEntry;
import me.fade.shopforge.service.AdminSession;
import me.fade.shopforge.service.ShopService;
import me.fade.shopforge.util.ItemResolver;
import me.fade.shopforge.util.MessageUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandAdminShop extends CommandBase {

    private final ShopForgeMod mod;

    public CommandAdminShop(ShopForgeMod mod) {
        this.mod = mod;
    }

    @Override
    public String getName() {
        return "adminshop";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/adminshop [open|done|cancel|addcategory|removecategory|additem|setprice|move]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        ShopService service = mod.getShopService();

        if (args.length == 0 || "open".equalsIgnoreCase(args[0])) {
            openEditor(player, service, args);
            return;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "done":
                if (service.commitSession(player)) {
                    service.refreshOpenContainers(server);
                    MessageUtil.info(player, "Admin session saved and synced.");
                } else {
                    MessageUtil.error(player, "No active admin session.");
                }
                return;
            case "cancel":
                service.cancelSession(player);
                MessageUtil.info(player, "Admin session canceled.");
                return;
            case "addcategory":
                addCategory(player, service, args);
                return;
            case "removecategory":
                removeCategory(player, service, args);
                return;
            case "additem":
                addItem(player, service, args);
                return;
            case "setprice":
                setPrice(player, service, args);
                return;
            case "move":
                moveCategory(player, service, args);
                return;
            default:
                throw new WrongUsageException(getUsage(sender));
        }
    }

    private void openEditor(EntityPlayerMP player, ShopService service, String[] args) {
        AdminSession session = service.startSession(player);
        if (args.length >= 2) {
            ShopCategory category = session.getWorkingConfig().findCategoryById(args[1]);
            if (category != null) {
                session.setSelectedCategoryId(category.id);
                int index = session.getWorkingConfig().categories.indexOf(category);
                player.openGui(mod, ShopGuiIds.ADMIN_ITEMS, player.world, index, 0, 0);
                return;
            }
            MessageUtil.error(player, "Unknown category: " + args[1]);
        }
        player.openGui(mod, ShopGuiIds.ADMIN_CATEGORIES, player.world, 0, 0, 0);
    }

    private void addCategory(EntityPlayerMP player, ShopService service, String[] args) throws CommandException {
        if (args.length < 3) {
            throw new WrongUsageException("/adminshop addcategory <id> <display name>");
        }

        AdminSession session = service.startSession(player);
        ShopConfig config = session.getWorkingConfig();

        String id = args[1].toLowerCase();
        if (config.findCategoryById(id) != null) {
            throw new CommandException("Category already exists: " + id);
        }

        String name = buildString(args, 2);
        ShopCategory category = new ShopCategory();
        category.id = id;
        category.name = name;
        category.iconItemId = "minecraft:chest";
        category.iconMeta = 0;
        config.categories.add(category);

        MessageUtil.info(player, "Added category " + name + " (" + id + ").");
        if (player.openContainer instanceof me.fade.shopforge.gui.ShopContainer) {
            ((me.fade.shopforge.gui.ShopContainer) player.openContainer).refreshFromServer();
        }
    }

    private void removeCategory(EntityPlayerMP player, ShopService service, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new WrongUsageException("/adminshop removecategory <id>");
        }

        AdminSession session = service.startSession(player);
        ShopConfig config = session.getWorkingConfig();
        ShopCategory category = config.findCategoryById(args[1]);
        if (category == null) {
            throw new CommandException("Unknown category: " + args[1]);
        }

        config.categories.remove(category);
        if (category.id.equalsIgnoreCase(session.getSelectedCategoryId())) {
            session.setSelectedCategoryId(null);
        }
        MessageUtil.info(player, "Removed category " + category.name + ".");
        if (player.openContainer instanceof me.fade.shopforge.gui.ShopContainer) {
            ((me.fade.shopforge.gui.ShopContainer) player.openContainer).refreshFromServer();
        }
    }

    private void addItem(EntityPlayerMP player, ShopService service, String[] args) throws CommandException {
        if (args.length < 3) {
            throw new WrongUsageException("/adminshop additem <category> <itemId> [meta] [count] [price]");
        }

        AdminSession session = service.startSession(player);
        ShopConfig config = session.getWorkingConfig();
        ShopCategory category = config.findCategoryById(args[1]);
        if (category == null) {
            throw new CommandException("Unknown category: " + args[1]);
        }

        String itemId = args[2];
        int meta = args.length >= 4 ? parseInt(args[3], 0) : 0;
        int count = args.length >= 5 ? parseInt(args[4], 1) : 1;
        double price = args.length >= 6 ? parseDouble(args[5], 0.0D) : 10.0D;

        ItemStack stack = ItemResolver.resolve(itemId, meta, count);
        if (stack.isEmpty()) {
            throw new CommandException("Invalid item ID: " + itemId);
        }

        ShopItemEntry entry = new ShopItemEntry();
        entry.itemId = itemId;
        entry.meta = meta;
        entry.count = Math.max(1, count);
        entry.price = Math.max(0.0D, price);
        category.items.add(entry);

        session.setSelectedCategoryId(category.id);
        MessageUtil.info(player, "Added item " + itemId + " to " + category.name + " for " + entry.price + ".");

        if (player.openContainer instanceof me.fade.shopforge.gui.ShopContainer) {
            ((me.fade.shopforge.gui.ShopContainer) player.openContainer).refreshFromServer();
        }
    }

    private void setPrice(EntityPlayerMP player, ShopService service, String[] args) throws CommandException {
        if (args.length < 4) {
            throw new WrongUsageException("/adminshop setprice <category> <index> <price>");
        }

        AdminSession session = service.startSession(player);
        ShopConfig config = session.getWorkingConfig();
        ShopCategory category = config.findCategoryById(args[1]);
        if (category == null) {
            throw new CommandException("Unknown category: " + args[1]);
        }

        int itemIndex = parseInt(args[2]);
        if (itemIndex < 0 || itemIndex >= category.items.size()) {
            throw new CommandException("Invalid item index: " + itemIndex);
        }

        double price = Math.max(0.0D, parseDouble(args[3], 0.0D));
        category.items.get(itemIndex).price = price;
        session.setSelectedCategoryId(category.id);

        MessageUtil.info(player, "Set price for " + category.id + " item #" + itemIndex + " to " + price + ".");
        if (player.openContainer instanceof me.fade.shopforge.gui.ShopContainer) {
            ((me.fade.shopforge.gui.ShopContainer) player.openContainer).refreshFromServer();
        }
    }

    private void moveCategory(EntityPlayerMP player, ShopService service, String[] args) throws CommandException {
        if (args.length < 3) {
            throw new WrongUsageException("/adminshop move <category> <slot|-1>");
        }

        AdminSession session = service.startSession(player);
        ShopConfig config = session.getWorkingConfig();
        ShopCategory category = config.findCategoryById(args[1]);
        if (category == null) {
            throw new CommandException("Unknown category: " + args[1]);
        }

        int slot = parseInt(args[2], -1);
        if (slot < -1 || slot > 44) {
            throw new CommandException("Slot must be -1 to 44.");
        }

        category.preferredSlot = slot;
        MessageUtil.info(player, slot == -1
                ? "Category " + category.id + " set back to centered layout."
                : "Category " + category.id + " moved to slot " + slot + ".");

        if (player.openContainer instanceof me.fade.shopforge.gui.ShopContainer) {
            ((me.fade.shopforge.gui.ShopContainer) player.openContainer).refreshFromServer();
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args,
                    Arrays.asList("open", "done", "cancel", "addcategory", "removecategory", "additem", "setprice", "move"));
        }

        if (args.length == 2 && isCategoryCompletionSubcommand(args[0])) {
            try {
                EntityPlayerMP player = getCommandSenderAsPlayer(sender);
                AdminSession session = mod.getShopService().startSession(player);
                List<String> ids = new java.util.ArrayList<>();
                for (ShopCategory category : session.getWorkingConfig().categories) {
                    ids.add(category.id);
                }
                return getListOfStringsMatchingLastWord(args, ids);
            } catch (CommandException ignored) {
                return Collections.emptyList();
            }
        }

        return Collections.emptyList();
    }

    private boolean isCategoryCompletionSubcommand(String sub) {
        String normalized = sub.toLowerCase();
        return "open".equals(normalized)
                || "removecategory".equals(normalized)
                || "additem".equals(normalized)
                || "setprice".equals(normalized)
                || "move".equals(normalized);
    }
}