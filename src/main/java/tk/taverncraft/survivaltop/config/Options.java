package tk.taverncraft.survivaltop.config;

import java.time.Instant;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import tk.taverncraft.survivaltop.Main;

/**
 * Handles and stores all options loaded from configuration files. Explanations for each field
 * may be found on the wiki and won't be repeated here.
 */
public class Options {
    private final Main main;
    private boolean useGuiStats;
    private boolean cacheIsEnabled;
    private boolean filterLastJoin;
    private long filterPlayerTime;
    private boolean enableGroup;
    private String groupType;
    private boolean includeBal;
    private boolean includeLand;
    private String landType;
    private boolean includeSpawners;
    private boolean includeContainers;
    private boolean includeInventory;
    private int updateInterval;
    private boolean updateOnStart;
    private double minimumWealth;
    private boolean useGuiLeaderboard;
    private String storageType;
    private int maxLandHeight;
    private int minLandHeight;
    private long lastLoadTime;
    private int cacheDuration;

    /**
     * Constructor for Options.
     *
     * @param main plugin class
     */
    public Options(Main main) {
        this.main = main;
        initializeOptions();
    }

    /**
     * Initializes options from config file, called during startup and reload.
     */
    public void initializeOptions() {
        FileConfiguration config = main.getConfig();
        this.useGuiStats = config.getBoolean("use-gui-stats", true);
        this.cacheIsEnabled = config.getBoolean("enable-cache", true);
        this.filterLastJoin = config.getBoolean("filter-last-join", false);
        this.filterPlayerTime = config.getLong("filter-player-time", 2592000);
        this.enableGroup = config.getBoolean("enable-group", false);
        this.groupType = config.getString("group-type", "FactionsUuid");
        this.includeBal = config.getBoolean("include-bal", false);
        this.includeLand = config.getBoolean("include-land", false);
        this.landType = config.getString("land-type", "GriefPrevention");
        this.includeSpawners = config.getBoolean("include-spawners", false);
        this.includeContainers = config.getBoolean("include-containers", false);
        this.includeInventory = config.getBoolean("include-inventory", false);
        this.updateInterval = config.getInt("update-interval", 3600);
        this.updateOnStart = config.getBoolean("update-on-start", false);
        this.minimumWealth = config.getInt("minimum-wealth", 0);
        this.useGuiLeaderboard = config.getBoolean("use-gui-leaderboard", false);
        this.storageType = config.getString("storage-type", "None");
        this.lastLoadTime = Instant.now().getEpochSecond();
        this.cacheDuration = config.getInt("cache-duration", 1800);
        setMaxLandHeight();
        setMinLandHeight();
    }

    // getters below

    public boolean balIsIncluded() {
        return includeBal;
    }

    public boolean landIsIncluded() {
        return includeLand;
    }

    public boolean spawnerIsIncluded() {
        return includeSpawners;
    }

    public boolean containerIsIncluded() {
        return includeContainers;
    }

    public boolean inventoryIsIncluded() {
        return includeInventory;
    }

    public boolean groupIsEnabled() {
        return enableGroup;
    }

    public boolean isUseGuiStats() {
        return useGuiStats;
    }

    public boolean cacheIsEnabled() {
        return cacheIsEnabled;
    }

    public boolean isUseGuiLeaderboard() {
        return useGuiLeaderboard;
    }

    public double getMaxLandHeight() {
        return maxLandHeight;
    }

    public double getMinLandHeight() {
        return minLandHeight;
    }

    public boolean filterLastJoin() {
        return filterLastJoin;
    }

    public long filterPlayerTime() {
        return filterPlayerTime;
    }

    public String getGroupType() {
        return groupType;
    }

    public String getLandType() {
        return landType;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public boolean updateOnStart() {
        return updateOnStart;
    }

    public double getMinimumWealth() {
        return minimumWealth;
    }

    public String getStorageType() {
        return storageType;
    }

    public long getLastLoadTime() {
        return lastLoadTime;
    }

    public int getCacheDuration() {
        return cacheDuration;
    }

    // setters below

    private void setMaxLandHeight() {
        if (Bukkit.getVersion().contains("1.18") || Bukkit.getVersion().contains("1.19")) {
            this.maxLandHeight = 320;
        } else {
            this.maxLandHeight = 256;
        }

        if (!main.getConfig().getString("max-land-height", "default")
            .equalsIgnoreCase("default")) {
            this.maxLandHeight = main.getConfig().getInt("max-land-height", this.maxLandHeight);
        }
    }

    private void setMinLandHeight() {
        if (Bukkit.getVersion().contains("1.18") || Bukkit.getVersion().contains("1.19")) {
            this.minLandHeight = -64;
        } else {
            this.minLandHeight = 0;
        }

        if (!main.getConfig().getString("min-land-height", "default")
            .equalsIgnoreCase("default")) {
            this.minLandHeight = main.getConfig().getInt("min-land-height", this.minLandHeight);
        }
    }

    public void disableBal() {
        this.includeBal = false;
    }

    public void disableLand() {
        this.includeLand = false;
    }

    public void disableGroup() {
        this.enableGroup = false;
    }
}
