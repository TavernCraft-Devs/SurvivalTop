package tk.taverncraft.survivaltop.logs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.messages.MessageManager;
import tk.taverncraft.survivaltop.utils.types.ClaimInfo;

/**
 * Handles the dumping of information for debugging.
 */
public class LogManager {
    private final Main main;
    private final String minecraftVersion;
    private final String survivalTopVersion;
    private String worldSize;
    private long numClaims = 0;
    private long numBlocks = 0;
    private int numEntities = 0;
    private long leaderboardUpdateStartTime = -1;
    private long lastUpdateDuration = -1;
    private long estimatedBlockProcessingRate = -1;

    private BukkitTask logTask;
    private boolean isLogging;

    /**
     * Constructor for LogManager.
     *
     * @param main plugin class
     */
    public LogManager(Main main) {
        this.main = main;
        this.minecraftVersion = Bukkit.getVersion();
        this.survivalTopVersion = main.getDescription().getVersion();
        stopExistingTasks();
    }

    /**
     * Begins the process for log dump.
     *
     * @param sender user who requested for dump
     */
    public void startLogDump(CommandSender sender) {
        this.isLogging = true;
        processServerProperties();
        processClaims(sender);
    }

    /**
     * Processes claim info to dump for debugging.
     *
     * @param sender user who requested for dump
     */
    private void processClaims(CommandSender sender) {
        new BukkitRunnable() {

            @Override
            public void run() {
                if (main.getOptions().groupIsEnabled()) {
                    processByGroups();
                } else {
                    processByPlayers();
                }
                executeClaimsProcessedAction(sender);
            }

        }.runTaskAsynchronously(main);
    }

    /**
     * Handles action after async claim task is done.
     *
     * @param sender user who requested for dump
     */
    private void executeClaimsProcessedAction(CommandSender sender) {
        leaderboardUpdateStartTime = main.getLeaderboardManager().getLeaderboardUpdateStartTime();
        lastUpdateDuration = main.getLeaderboardManager().getLastUpdateDuration();

        if (leaderboardUpdateStartTime != -1 && lastUpdateDuration != -1) {
            estimatedBlockProcessingRate = numBlocks / lastUpdateDuration;
        }
        dumpToLogFile();
        MessageManager.sendMessage(sender, "log-complete");
        this.isLogging = false;
    }

    /**
     * Processes claim info by players to dump for debugging.
     */
    private void processByPlayers() {
        long lastJoinTime = main.getOptions().filterPlayerTime() * 1000;

        // code intentionally duplicated to keep the if condition outside loop to save check time

        // path for if last join filter is off or if last join time is set <= 0 (cannot filter)
        if (!main.getOptions().filterLastJoin() || lastJoinTime <= 0) {
            Arrays.stream(this.main.getServer().getOfflinePlayers()).forEach(offlinePlayer -> {
                ClaimInfo claimInfo = this.main.getLandManager().getClaimsInfo(
                        offlinePlayer.getName());
                numClaims = claimInfo.getNumClaims();
                numBlocks = claimInfo.getNumBlocks();
                numEntities++;
            });
            return;
        }

        // path for if last join filter is on
        Instant instant = Instant.now();
        long currentTime = instant.getEpochSecond() * 1000;
        Arrays.stream(this.main.getServer().getOfflinePlayers()).forEach(offlinePlayer -> {
            if (currentTime - offlinePlayer.getLastPlayed() > lastJoinTime) {
                return;
            }
            ClaimInfo claimInfo = this.main.getLandManager().getClaimsInfo(offlinePlayer.getName());
            numClaims = claimInfo.getNumClaims();
            numBlocks = claimInfo.getNumBlocks();
            numEntities++;
        });
    }

    /**
     * Processes claim info by groups to dump for debugging.
     */
    private void processByGroups() {
        List<String> groups = this.main.getGroupManager().getGroups();
        this.numEntities = groups.size();
        for (int i = 0; i < numEntities; i++) {
            String group = groups.get(i);
            ClaimInfo claimInfo = this.main.getLandManager().getClaimsInfo(group);
            numClaims += claimInfo.getNumClaims();
            numBlocks += claimInfo.getNumBlocks();
        }
    }

    /**
     * Processes server properties to include for debugging.
     */
    private void processServerProperties() {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("server.properties"));
            this.worldSize = props.getProperty("max-world-size");
        } catch (IOException e) {
            this.worldSize = "not found";
        }
    }

    /**
     * Stops existing log task.
     */
    public void stopExistingTasks() {
        if (logTask != null) {
            logTask.cancel();
            logTask = null;
        }
        this.isLogging = false;
    }

    /**
     * Checks if there is an ongoing log task.
     *
     * @return true if the log task is in progress, false otherwise
     */
    public boolean isLogging() {
        return this.isLogging;
    }

    /**
     * Dumps details into a log file, triggered by the dump command.
     */
    public void dumpToLogFile() {
        String fileName = "dump-" + Instant.now().getEpochSecond() + ".yml";
        File configFile = new File(main.getDataFolder() + "/dumps", fileName);
        FileConfiguration config = new YamlConfiguration();
        configFile.getParentFile().mkdirs();

        // logs from plugin
        config.set("minecraft-version", minecraftVersion);
        config.set("survivalTop-version", survivalTopVersion);
        config.set("world-size", worldSize);
        config.set("num-entities", numEntities);
        config.set("num-claims", numClaims);
        config.set("num-blocks", numBlocks);
        config.set("leaderboard-update-start-time", leaderboardUpdateStartTime);
        config.set("last-update-duration", lastUpdateDuration);
        config.set("estimated-block-processing-rate", estimatedBlockProcessingRate);

        // config options
        config.set("use-gui-stats", main.getOptions().isUseGuiStats());
        config.set("calculation-mode", main.getOptions().getCalculationMode());
        config.set("cache-duration", main.getOptions().getCacheDuration());
        config.set("filter-last-join", main.getOptions().filterLastJoin());
        config.set("filter-player-time", main.getOptions().filterPlayerTime());
        config.set("enable-group", main.getOptions().groupIsEnabled());
        config.set("group-type", main.getOptions().getGroupType());
        config.set("include-bal", main.getOptions().balIsIncluded());
        config.set("include-land", main.getOptions().landIsIncluded());
        config.set("land-type", main.getOptions().getLandType());
        config.set("max-land-height", main.getOptions().getMaxLandHeight());
        config.set("min-land-height", main.getOptions().getMinLandHeight());
        config.set("include-spawners", main.getOptions().spawnerIsIncluded());
        config.set("include-containers", main.getOptions().containerIsIncluded());
        config.set("container-type", main.getOptions().getContainerTypes());
        config.set("include-inventory", main.getOptions().inventoryIsIncluded());
        config.set("include-papi", main.getOptions().papiIsIncluded());
        config.set("update-interval", main.getOptions().getUpdateInterval());
        config.set("update-on-start", main.getOptions().updateOnStart());
        config.set("minimum-wealth", main.getOptions().getMinimumWealth());
        config.set("total-leaderboard-positions", main.getOptions().getTotalLeaderboardPositions());
        config.set("leaderboard-positions-per-page",
                main.getOptions().getLeaderboardPositionsPerPage());
        config.set("use-interactive-leaderboard", main.getOptions().isUseInteractiveLeaderboard());
        config.set("commands-on-start", main.getOptions().getCommandsOnStart());
        config.set("commands-on-end", main.getOptions().getCommandsOnEnd());
        config.set("storage-type", main.getOptions().getStorageType());
        config.set("last-load-time", main.getOptions().getLastLoadTime());

        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Logs warnings for the plugin.
     *
     * @param message warning message
     */
    public static void warn(String message) {
        Bukkit.getLogger().info("[SurvivalTop] WARNING: " + message);
    }

    /**
     * Logs information for the plugin.
     *
     * @param message info message
     */
    public static void info(String message) {
        Bukkit.getLogger().info("[SurvivalTop] INFO: " + message);
    }

    /**
     * Logs errors for the plugin.
     *
     * @param message error message
     */
    public static void error(String message) {
        Bukkit.getLogger().info("[SurvivalTop] ERROR: " + message);
    }
}
