package tk.taverncraft.survivaltop.logs;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.messages.MessageManager;

/**
 * Handles the dumping of information for debugging.
 */
public class LogManager {
    private final Main main;
    private long numClaims = 0;
    private long numBlocks = 0;
    private int numEntities = 0;
    private String worldRadius;
    private String minecraftVersion;
    private String survivalTopVersion;

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
                if (main.groupIsEnabled()) {
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
        LogFile logFile = new LogFile(minecraftVersion, survivalTopVersion, worldRadius,
                numEntities, numClaims, numBlocks);
        main.getConfigManager().dumpToLogFile(logFile);
        MessageManager.sendMessage(sender, "log-complete");
        this.isLogging = false;
    }

    /**
     * Processes claim info by players to dump for debugging.
     */
    private void processByPlayers() {

        boolean filterLastJoin = this.main.getConfig().getBoolean("filter-last-join", false);
        long lastJoinTime = this.main.getConfig().getLong("last-join-time", 2592000) * 1000;

        // code intentionally duplicated to keep the if condition outside loop to save check time

        // path for if last join filter is off or if last join time is set <= 0 (cannot filter)
        if (!filterLastJoin || lastJoinTime <= 0) {
            Arrays.stream(this.main.getServer().getOfflinePlayers()).forEach(offlinePlayer -> {
                Long[] claimsInfo = this.main.getLandManager().getClaimsInfo(offlinePlayer.getName());
                numClaims = claimsInfo[0];
                numBlocks = claimsInfo[1];
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
            Long[] claimsInfo = this.main.getLandManager().getClaimsInfo(offlinePlayer.getName());
            numClaims = claimsInfo[0];
            numBlocks = claimsInfo[1];
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
            Long[] claimsInfo = this.main.getLandManager().getClaimsInfo(group);
            numClaims += claimsInfo[0];
            numBlocks += claimsInfo[1];
        }
    }

    /**
     * Processes server properties to include for debugging.
     */
    private void processServerProperties() {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("server.properties"));
            this.worldRadius = props.getProperty("max-world-size");
        } catch (IOException e) {
            this.worldRadius = "not found";
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

    public static void log() {

    }
}
