package tk.taverncraft.survivaltop;

import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import tk.taverncraft.survivaltop.balance.BalanceManager;
import tk.taverncraft.survivaltop.events.leaderboard.SignBreakEvent;
import tk.taverncraft.survivaltop.events.leaderboard.SignPlaceEvent;
import tk.taverncraft.survivaltop.events.stats.ViewPageEvent;
import tk.taverncraft.survivaltop.group.GroupManager;
import tk.taverncraft.survivaltop.inventory.InventoryManager;
import tk.taverncraft.survivaltop.land.LandManager;
import tk.taverncraft.survivaltop.commands.CommandParser;
import tk.taverncraft.survivaltop.commands.CommandTabCompleter;
import tk.taverncraft.survivaltop.events.DependencyLoadEvent;
import tk.taverncraft.survivaltop.leaderboard.LeaderboardManager;
import tk.taverncraft.survivaltop.logs.LogManager;
import tk.taverncraft.survivaltop.config.Options;
import tk.taverncraft.survivaltop.stats.EntityStatsManager;
import tk.taverncraft.survivaltop.stats.ServerStatsManager;
import tk.taverncraft.survivaltop.storage.StorageManager;
import tk.taverncraft.survivaltop.ui.InfoGui;
import tk.taverncraft.survivaltop.config.ConfigManager;
import tk.taverncraft.survivaltop.utils.services.DependencyManager;
import tk.taverncraft.survivaltop.utils.services.PapiManager;
import tk.taverncraft.survivaltop.utils.services.PluginUpdateManager;
import tk.taverncraft.survivaltop.utils.services.Metrics;

/**
 * The plugin class.
 */
public class Main extends JavaPlugin {
    // vault
    private static Economy econ = null;
    private static Permission perms = null;

    // config
    private FileConfiguration config;
    private FileConfiguration blocksConfig;
    private FileConfiguration spawnersConfig;
    private FileConfiguration containersConfig;
    private FileConfiguration inventoriesConfig;
    private FileConfiguration signsConfig;

    // managers
    private ConfigManager configManager;
    private DependencyManager dependencyManager;
    private BalanceManager balanceManager;
    private LandManager landManager;
    private InventoryManager inventoryManager;
    private GroupManager groupManager;
    private EntityStatsManager entityStatsManager;
    private ServerStatsManager serverStatsManager;
    private LeaderboardManager leaderboardManager;
    private StorageManager storageManager;
    private LogManager logManager;

    // options
    // todo: move this into an options manager
    private Options options;

    // console uuid
    private final UUID consoleUuid = UUID.randomUUID();

    @Override
    public void onDisable() {
        entityStatsManager.setStopCalculations(true);
        serverStatsManager.setStopCalculations(true);
        landManager.setStopOperations(true);
        inventoryManager.setStopOperations(true);
        LogManager.info(String.format("Disabled Version %s", getDescription().getVersion()));
    }

    @Override
    public void onEnable() {
        new PluginUpdateManager(this, 96737).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                getLogger().info("You are using the latest version of SurvivalTop!");
            } else {
                getLogger().info("A new version of SurvivalTop is now available on spigot!");
            }
        });

        // config setup
        this.configManager = new ConfigManager(this);
        configManager.createConfigs();

        // set options
        this.options = new Options(this);
        options.initializeOptions();

        //this.createScheduleConfig();
        this.getCommand("survivaltop").setTabCompleter(new CommandTabCompleter());
        this.getCommand("survivaltop").setExecutor(new CommandParser(this));

        try {
            this.dependencyManager = new DependencyManager(this);
            this.storageManager = new StorageManager(this);
            this.entityStatsManager = new EntityStatsManager(this);
            this.serverStatsManager = new ServerStatsManager(this);
            this.leaderboardManager = new LeaderboardManager(this);
            this.balanceManager = new BalanceManager(this);
            this.landManager = new LandManager(this);
            this.inventoryManager = new InventoryManager(this);
            // todo: revisit how gui is done, that whole module is messy and needs to be rewritten
            // load worth values into info gui
            new InfoGui(this);
            this.groupManager = new GroupManager(this);
            this.logManager = new LogManager(this);
        } catch (NullPointerException e) {
            LogManager.error("Is your config.yml updated/set up correctly?");
            getServer().getPluginManager().disablePlugin(this);
        }

        int pluginId = 12982;
        Metrics metrics = new Metrics(this, pluginId);

        // Optional: Add custom charts
        metrics.addCustomChart(new Metrics.SimplePie("chart_id", () -> "My value"));

        if (Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.12")) {
            Bukkit.getScheduler().runTaskLater(this, this::loadDependencies, 1);
        } else {
            this.getServer().getPluginManager().registerEvents(
                    new DependencyLoadEvent(this), this);
        }

        if (getConfig().getBoolean("update-on-start")) {
            leaderboardManager.scheduleLeaderboardUpdate(getConfig().getInt(
                    "update-interval"), 3);
        } else {
            leaderboardManager.scheduleLeaderboardUpdate(getConfig().getInt(
                    "update-interval"), getConfig().getInt("update-interval"));
        }

        this.getServer().getPluginManager().registerEvents(
                new SignPlaceEvent(this), this);
        this.getServer().getPluginManager().registerEvents(
                new SignBreakEvent(this), this);
        this.getServer().getPluginManager().registerEvents(
                new ViewPageEvent(this), this);
    }

    /**
     * Loads dependencies.
     */
    public void loadDependencies() {
        // vault setup
        setupEconomy();
        setupPermissions();

        // placeholderapi setup
        checkPlaceholderAPI();

        if (!this.dependencyManager.checkAllDependencies()) {
            LogManager.error("Some options were disabled on startup " +
                    "to prevent errors, please check your config file!");
        }
    }

    /**
     * Checks if PAPI is present.
     */
    private void checkPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            LogManager.info(String.format("[%s] - PlaceholderAPI found, integrated with plugin!",
                    getDescription().getName()));
            new PapiManager(this).register();
        }
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        econ = rsp.getProvider();
    }

    private void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp =
                getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
    }

    public UUID getSenderUuid(CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getUniqueId();
        } else {
            return this.consoleUuid;
        }
    }

    public static Economy getEconomy() {
        return econ;
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public FileConfiguration getBlocksConfig() {
        return this.blocksConfig;
    }

    public void setBlocksConfig(FileConfiguration blocksConfig) {
        this.blocksConfig = blocksConfig;
    }

    public FileConfiguration getSpawnersConfig() {
        return this.spawnersConfig;
    }

    public void setSpawnersConfig(FileConfiguration spawnersConfig) {
        this.spawnersConfig = spawnersConfig;
    }

    public FileConfiguration getContainersConfig() {
        return this.containersConfig;
    }

    public void setContainersConfig(FileConfiguration containersConfig) {
        this.containersConfig = containersConfig;
    }

    public FileConfiguration getInventoriesConfig() {
        return this.inventoriesConfig;
    }

    public void setInventoriesConfig(FileConfiguration inventoriesConfig) {
        this.inventoriesConfig = inventoriesConfig;
    }

    public FileConfiguration getSignsConfig() {
        return this.signsConfig;
    }

    public void setSignsConfig(FileConfiguration signsConfig) {
        this.signsConfig = signsConfig;
    }

    public void setConfig(FileConfiguration config) {
        this.config = config;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DependencyManager getDependencyManager() {
        return this.dependencyManager;
    }

    public BalanceManager getBalanceManager() {
        return this.balanceManager;
    }

    public LandManager getLandManager() {
        return landManager;
    }

    public InventoryManager getInventoryManager() {
        return this.inventoryManager;
    }

    public GroupManager getGroupManager() {
        return this.groupManager;
    }

    public EntityStatsManager getEntityStatsManager() {
        return this.entityStatsManager;
    }

    public ServerStatsManager getServerStatsManager() {
        return this.serverStatsManager;
    }

    public LeaderboardManager getLeaderboardManager() {
        return this.leaderboardManager;
    }

    public StorageManager getStorageManager() {
        return this.storageManager;
    }

    public LogManager getLogManager() {
        return this.logManager;
    }

    public Options getOptions() {
        return this.options;
    }
}