package me.styna.privateserver.item;

import me.styna.privateserver.PrivateServer;
import me.styna.privateserver.config.model.MainConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CocaineItem extends ItemFood {

    public CocaineItem() {
        super(1, 0.1F, false);
        setAlwaysEdible();
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        ItemStack result = super.onItemUseFinish(stack, worldIn, entityLiving);
        if (worldIn.isRemote || !(entityLiving instanceof EntityPlayerMP)) {
            return result;
        }

        EntityPlayerMP player = (EntityPlayerMP) entityLiving;
        MinecraftServer server = player.getServer();
        PrivateServer mod = PrivateServer.get();
        if (server == null || mod == null || mod.getConfigService() == null) {
            return result;
        }

        MainConfig config = mod.getConfigService().getMainConfig();
        List<String> effectCommands = config.cocaineConsumeConsoleCommands == null
                ? Collections.emptyList()
                : config.cocaineConsumeConsoleCommands;
        List<String> weightedOptions = effectCommands.stream()
                .filter(entry -> entry != null && !entry.trim().isEmpty())
                .limit(4)
                .collect(Collectors.toList());
        if (weightedOptions.isEmpty()) {
            return result;
        }

        double allEffectsChance = Math.max(0.0D, Math.min(100.0D, config.cocaineAllEffectsChancePercent));
        if (worldIn.rand.nextDouble() < (allEffectsChance / 100.0D)) {
            for (String command : weightedOptions) {
                runAsConsole(server, player, command);
            }
            return result;
        }

        int index = worldIn.rand.nextInt(weightedOptions.size());
        runAsConsole(server, player, weightedOptions.get(index));
        return result;
    }

    private static void runAsConsole(MinecraftServer server, EntityPlayerMP player, String rawCommand) {
        if (rawCommand == null) {
            return;
        }
        String command = rawCommand.trim();
        if (command.isEmpty()) {
            return;
        }
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        String resolved = command
                .replace("%player%", player.getName())
                .replace("%uuid%", player.getUniqueID().toString());
        try {
            server.getCommandManager().executeCommand(server, resolved);
        } catch (Throwable throwable) {
            PrivateServer.LOGGER.error("Failed to run cocaine consume command: {}", resolved, throwable);
        }
    }
}
