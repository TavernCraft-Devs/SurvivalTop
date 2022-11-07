package tk.taverncraft.survivaltop;

import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import tk.taverncraft.survivaltop.events.leaderboard.SignBreakEvent;
import tk.taverncraft.survivaltop.events.leaderboard.SignPlaceEvent;
import tk.taverncraft.survivaltop.events.stats.ViewPageEvent;
import tk.taverncraft.survivaltop.group.GroupManager;
import tk.taverncraft.survivaltop.land.LandManager;
import tk.taverncraft.survivaltop.commands.CommandParser;
import tk.taverncraft.survivaltop.commands.CommandTabCompleter;
import tk.taverncraft.survivaltop.events.DependencyLoadEvent;
import tk.taverncraft.survivaltop.leaderboard.LeaderboardManager;
import tk.taverncraft.survivaltop.stats.EntityStatsManager;
import tk.taverncraft.survivaltop.stats.ServerStatsManager;
import tk.taverncraft.survivaltop.storage.StorageManager;
import tk.taverncraft.survivaltop.utils.*;

/**
 * The plugin class.
 */
public class Main extends JavaPlugin {

    private static final Logger log = Logger.getLogger("Minecraft");

    // vault
    private static Economy econ = null;
    private static Permission perms = null;

    // config
    private FileConfiguration config;
    private FileConfiguration blocksConfig;
    private FileConfiguration spawnersConfig;
    private FileConfiguration containersConfig;
    private FileConfiguration signsConfig;

    // managers
    private ConfigManager configManager;
    private DependencyManager dependencyManager;
    private LandManager landManager;
    private GroupManager groupManager;
    private EntityStatsManager entityStatsManager;
    private ServerStatsManager serverStatsManager;
    private LeaderboardManager leaderboardManager;
    private StorageManager storageManager;

    // console uuid
    private UUID consoleUuid;

    @Override
    public void onDisable() {
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
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

        this.configManager = new ConfigManager(this);

        // config setup
        configManager.createConfig();
        configManager.createMessageFile();
        configManager.createBlocksConfig();
        configManager.createSpawnersConfig();
        configManager.createContainersConfig();
        configManager.createSignsConfig();

        //this.createScheduleConfig();
        this.getCommand("survivaltop").setTabCompleter(new CommandTabCompleter());
        this.getCommand("survivaltop").setExecutor(new CommandParser(this));

        try {
            this.dependencyManager = new DependencyManager(this);
            this.storageManager = new StorageManager(this);
            this.entityStatsManager = new EntityStatsManager(this);
            this.serverStatsManager = new ServerStatsManager(this);
            this.leaderboardManager = new LeaderboardManager(this);
            this.landManager = new LandManager(this);
            this.groupManager = new GroupManager(this);
        } catch (NullPointerException e) {
            Bukkit.getConsoleSender().sendMessage("[SurvivalTop] Is your config.yml updated/set up correctly?");
            getServer().getPluginManager().disablePlugin(this);
        }

        this.consoleUuid = UUID.randomUUID();

        int pluginId = 12982;
        Metrics metrics = new Metrics(this, pluginId);

        // Optional: Add custom charts
        metrics.addCustomChart(new Metrics.SimplePie("chart_id", () -> "My value"));

        if (Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.12")) {
            Bukkit.getScheduler().runTaskLater(this, this::loadDependencies, 1);
        } else {
            this.getServer().getPluginManager().registerEvents(new DependencyLoadEvent(this), this);
        }

        if (getConfig().getBoolean("update-on-start")) {
            leaderboardManager.scheduleLeaderboardUpdate(getConfig().getInt("update-interval"), 3);
        } else {
            leaderboardManager.scheduleLeaderboardUpdate(getConfig().getInt("update-interval"), getConfig().getInt("update-interval"));
        }

        this.getServer().getPluginManager().registerEvents(new SignPlaceEvent(this), this);
        this.getServer().getPluginManager().registerEvents(new SignBreakEvent(this), this);
        this.getServer().getPluginManager().registerEvents(new ViewPageEvent(this), this);
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

        if (!this.getDependencyManager().hasDependenciesLoaded()) {
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    /**
     * Checks if PAPI is present.
     */
    private void checkPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info(String.format("[%s] - PlaceholderAPI found, integrated with plugin!", getDescription().getName()));
            new PapiManager(this).register();
        }
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        econ = rsp.getProvider();
    }

    private void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
    }

    public double getMaxHeight() {
        if (Bukkit.getVersion().contains("1.18") || Bukkit.getVersion().contains("1.19")) {
            return 320;
        } else {
            return 256;
        }
    }

    public double getMinHeight() {
        if (Bukkit.getVersion().contains("1.18") || Bukkit.getVersion().contains("1.19")) {
            return -64;
        } else {
            return 0;
        }
    }

    public boolean groupIsEnabled() {
        return this.getConfig().getBoolean("enable-group", false);
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

    public FileConfiguration getSignsConfig() {
        return this.signsConfig;
    }

    public void setSignsConfig(FileConfiguration signsConfig) {
        this.signsConfig = signsConfig;
    }

    public void setConfig(FileConfiguration config) {
        this.config = config;
    }

    public ConfigManager getConfigManager() { return configManager; }

    public DependencyManager getDependencyManager() {
        return this.dependencyManager;
    }

    public LandManager getLandManager() {
        return landManager;
    }

    public GroupManager getGroupManager() { return this.groupManager; }

    public EntityStatsManager getEntityStatsManager() { return this.entityStatsManager; }

    public ServerStatsManager getServerStatsManager() {
        return this.serverStatsManager;
    }

    public LeaderboardManager getLeaderboardManager() { return this.leaderboardManager; }

    public StorageManager getStorageManager() {
        return this.storageManager;
    }

    public UUID getConsoleUuid() {
        return consoleUuid;
    }
}