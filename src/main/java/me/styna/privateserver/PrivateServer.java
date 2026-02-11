package me.styna.privateserver;

import me.styna.privateserver.command.CommandBal;
import me.styna.privateserver.command.CommandBalTop;
import me.styna.privateserver.command.CommandBack;
import me.styna.privateserver.command.CommandBossbar;
import me.styna.privateserver.command.CommandDelHome;
import me.styna.privateserver.command.CommandDiscord;
import me.styna.privateserver.command.CommandEco;
import me.styna.privateserver.command.CommandFly;
import me.styna.privateserver.command.CommandHome;
import me.styna.privateserver.command.CommandNick;
import me.styna.privateserver.command.CommandRenameHomei;
import me.styna.privateserver.command.CommandSetHome;
import me.styna.privateserver.command.CommandSpeed;
import me.styna.privateserver.command.CommandTpAccept;
import me.styna.privateserver.command.CommandTpDeny;
import me.styna.privateserver.command.CommandTpa;
import me.styna.privateserver.command.CommandTpaHere;
import me.styna.privateserver.config.ConfigService;
import me.styna.privateserver.economy.EconomyService;
import me.styna.privateserver.integration.ChiselIntegration;
import me.styna.privateserver.service.BossbarService;
import me.styna.privateserver.service.CombatTagService;
import me.styna.privateserver.service.DiscordRelayService;
import me.styna.privateserver.service.GameplayEventHandler;
import me.styna.privateserver.teleport.TeleportService;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class PrivateServer {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    private static PrivateServer instance;

    private ConfigService configService;
    private EconomyService economyService;
    private TeleportService teleportService;
    private CombatTagService combatTagService;
    private BossbarService bossbarService;
    private DiscordRelayService discordRelayService;
    private long currentServerTick;

    public PrivateServer() {
        instance = this;
    }

    public static PrivateServer get() {
        return instance;
    }

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        try {
            File baseConfigDir = new File(event.getModConfigurationDirectory(), Tags.MOD_ID);
            this.configService = new ConfigService(baseConfigDir);
            this.configService.loadAll();

            File databaseFile = new File(configService.getDatabasesDir(), "data.sqlite");
            this.economyService = new EconomyService(configService, databaseFile);
            this.economyService.initialize();
            this.teleportService = new TeleportService(configService, economyService, databaseFile);
            this.teleportService.initialize();

            this.combatTagService = new CombatTagService();
            this.bossbarService = new BossbarService();
            this.discordRelayService = new DiscordRelayService(configService, databaseFile);
            this.discordRelayService.initialize();

            MinecraftForge.EVENT_BUS.register(new GameplayEventHandler(this));
            LOGGER.info("Initialized {} with config path {}", Tags.MOD_ID, baseConfigDir.getAbsolutePath());
        } catch (IOException | SQLException exception) {
            throw new RuntimeException("Failed to initialize mod services", exception);
        }
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        ChiselIntegration.registerSugarToCocaineVariations();
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandBal(this));
        event.registerServerCommand(new CommandBalTop(this));
        event.registerServerCommand(new CommandEco(this));
        event.registerServerCommand(new CommandNick(this));
        event.registerServerCommand(new CommandFly(this));
        event.registerServerCommand(new CommandSpeed(this));
        event.registerServerCommand(new CommandBossbar(this));
        event.registerServerCommand(new CommandBack(this));
        event.registerServerCommand(new CommandHome(this));
        event.registerServerCommand(new CommandSetHome(this));
        event.registerServerCommand(new CommandDelHome(this));
        event.registerServerCommand(new CommandRenameHomei(this));
        event.registerServerCommand(new CommandTpa(this));
        event.registerServerCommand(new CommandTpaHere(this));
        event.registerServerCommand(new CommandTpAccept(this));
        event.registerServerCommand(new CommandTpDeny(this));
        event.registerServerCommand(new CommandDiscord(this));

        if (configService.getChatConfig().relayServerLifecycle) {
            discordRelayService.relay("server", "Server starting");
        }
    }

    @Mod.EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        if (configService.getChatConfig().relayServerLifecycle) {
            discordRelayService.relay("server", "Server started");
        }
    }

    @Mod.EventHandler
    public void onServerStopped(FMLServerStoppedEvent event) {
        if (configService.getChatConfig().relayServerLifecycle) {
            discordRelayService.relay("server", "Server stopped");
        }
        if (economyService != null) {
            economyService.shutdown();
        }
        if (teleportService != null) {
            teleportService.shutdown();
        }
        if (discordRelayService != null) {
            discordRelayService.shutdown();
        }
    }

    public ConfigService getConfigService() {
        return configService;
    }

    public EconomyService getEconomyService() {
        return economyService;
    }

    public CombatTagService getCombatTagService() {
        return combatTagService;
    }

    public TeleportService getTeleportService() {
        return teleportService;
    }

    public BossbarService getBossbarService() {
        return bossbarService;
    }

    public DiscordRelayService getDiscordRelayService() {
        return discordRelayService;
    }

    public long getCurrentServerTick() {
        return currentServerTick;
    }

    public void setCurrentServerTick(long currentServerTick) {
        this.currentServerTick = currentServerTick;
    }
}
