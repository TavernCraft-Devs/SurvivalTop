package tk.taverncraft.survivaltop.config;

import java.time.Instant;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import tk.taverncraft.survivaltop.Main;

/**
 * Handles and stores all options loaded from configuration files. Explanations for each field
 * may be found on the wiki and are not repeated here.
 */
public class Options {
    private final Main main;

    // general configurations
    private boolean useGuiStats;
    private int calculationMode;
    private int cacheDuration;

    // filter configurations
    private boolean filterLastJoin;
    private long filterPlayerTime;

    // group configurations
    private boolean enableGroup;
    private String groupType;

    // balance configurations
    private boolean includeBal;

    // land configurations
    private boolean includeLand;
    private String landType;
    private int maxLandHeight;
    private int minLandHeight;
    private boolean includeSpawners;
    private boolean includeContainers;
    private List<String> containerTypes;

    // inventory configurations
    private boolean includeInventory;

    // papi configurations
    private boolean includePapi;

    // leaderboard configurations
    private int updateInterval;
    private boolean updateOnStart;
    private double minimumWealth;
    private int totalLeaderboardPositions;
    private int leaderboardPositionsPerPage;
    private boolean useHoverableLeaderboard;

    // storage configurations
    private String storageType;

    // last plugin load/reload time
    private long lastLoadTime;

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
        this.calculationMode = config.getInt("calculation-mode", 1);
        this.cacheDuration = config.getInt("cache-duration", 1800);
        this.filterLastJoin = config.getBoolean("filter-last-join", false);
        this.filterPlayerTime = config.getLong("filter-player-time", 2592000);
        this.enableGroup = config.getBoolean("enable-group", false);
        this.groupType = config.getString("group-type", "FactionsUuid");
        this.includeBal = config.getBoolean("include-bal", false);
        this.includeLand = config.getBoolean("include-land", false);
        this.landType = config.getString("land-type", "GriefPrevention");
        setMaxLandHeight();
        setMinLandHeight();
        this.includeSpawners = config.getBoolean("include-spawners", false);
        this.includeContainers = config.getBoolean("include-containers", false);
        this.containerTypes = config.getStringList("container-type");
        this.includeInventory = config.getBoolean("include-inventory", false);
        this.includePapi = config.getBoolean("include-papi", false);
        this.updateInterval = config.getInt("update-interval", 3600);
        this.updateOnStart = config.getBoolean("update-on-start", false);
        this.minimumWealth = config.getInt("minimum-wealth", 0);
        this.totalLeaderboardPositions = config.getInt("total-leaderboard-positions", -1);
        this.leaderboardPositionsPerPage = config.getInt("leaderboard-positions-per-page", 10);
        this.useHoverableLeaderboard = config.getBoolean("use-hoverable-leaderboard", false);
        this.storageType = config.getString("storage-type", "None");
        this.lastLoadTime = Instant.now().getEpochSecond();
    }

    // getters below

    public boolean isUseGuiStats() {
        return useGuiStats;
    }

    public int getCalculationMode() {
        return calculationMode;
    }

    public boolean isCalculationMode0() {
        return calculationMode == 0;
    }

    public boolean isCalculationMode1() {
        return calculationMode != 0 && calculationMode != 2;
    }

    public boolean isCalculationMode2() {
        return calculationMode == 2;
    }

    public int getCacheDuration() {
        return cacheDuration;
    }

    public boolean filterLastJoin() {
        return filterLastJoin;
    }

    public long filterPlayerTime() {
        return filterPlayerTime;
    }

    public boolean groupIsEnabled() {
        return enableGroup;
    }

    public String getGroupType() {
        return groupType;
    }

    public boolean balIsIncluded() {
        return includeBal;
    }

    public boolean landIsIncluded() {
        return includeLand;
    }

    public String getLandType() {
        return landType;
    }

    public double getMaxLandHeight() {
        return maxLandHeight;
    }

    public double getMinLandHeight() {
        return minLandHeight;
    }

    public boolean spawnerIsIncluded() {
        return includeLand && includeSpawners;
    }

    public boolean containerIsIncluded() {
        return includeLand && includeContainers;
    }

    public List<String> getContainerTypes() {
        return containerTypes;
    }

    public boolean inventoryIsIncluded() {
        return includeInventory;
    }

    public boolean papiIsIncluded() {
        return includePapi;
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

    public int getTotalLeaderboardPositions() {
        return totalLeaderboardPositions;
    }

    public int getLeaderboardPositionsPerPage() {
        return leaderboardPositionsPerPage;
    }

    public boolean isUseHoverableLeaderboard() {
        return useHoverableLeaderboard;
    }

    public String getStorageType() {
        return storageType;
    }

    public long getLastLoadTime() {
        return lastLoadTime;
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

    public void disablePapi() {
        this.includePapi = false;
    }
}
