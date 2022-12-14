package tk.taverncraft.survivaltop.leaderboard;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.stats.cache.EntityLeaderboardCache;
import tk.taverncraft.survivaltop.messages.MessageManager;

/**
 * LeaderboardManager contains the main logic related to updating the leaderboard.
 */
public class LeaderboardManager {
    private final Main main;
    private boolean isUpdating;
    private BukkitTask leaderboardTask;
    private long leaderboardUpdateStartTime = -1;
    private long lastUpdateDuration = -1;

    /**
     * Constructor for LeaderboardManager.
     *
     * @param main plugin class
     */
    public LeaderboardManager(Main main) {
        this.main = main;
        stopExistingTasks();
    }

    /**
     * Scheduled entry point for updating leaderboard.
     *
     * @param frequency frequency of update
     * @param delay the delay before first update
     */
    public void scheduleLeaderboardUpdate(int frequency, int delay) {

        // todo: clean up code logic here

        // if frequency is -1, then no need to schedule repeating updates
        if (frequency == -1) {
            leaderboardTask = new BukkitRunnable() {

                @Override
                public void run() {
                if (isUpdating) {
                    main.getLogger().info("Scheduled leaderboard update could not be " +
                            "carried out because an existing update is in progress.");
                    return;
                }
                isUpdating = true;
                initiateLeaderboardUpdate(Bukkit.getConsoleSender());
                }

            }.runTaskAsynchronously(main);
            return;
        }
        long interval = frequency * 20L;
        long delayTicks = delay * 20L;
        leaderboardTask = new BukkitRunnable() {

            @Override
            public void run() {
            if (isUpdating) {
                main.getLogger().info("Scheduled leaderboard update could not be " +
                        "carried out because an existing update is in progress.");
                return;
            }
            isUpdating = true;
            initiateLeaderboardUpdate(Bukkit.getConsoleSender());
            }

        }.runTaskTimerAsynchronously(main, delayTicks, interval);
    }

    /**
     * Manual entry point for updating leaderboard.
     *
     * @param sender user executing the update
     */
    public void doManualLeaderboardUpdate(CommandSender sender) {
        leaderboardTask = new BukkitRunnable() {
            @Override
            public void run() {
                isUpdating = true;
                initiateLeaderboardUpdate(sender);
            }

        }.runTaskAsynchronously(main);
    }

    /**
     * Initiates the leaderboard update.
     *
     * @param sender user executing the update
     */
    public void initiateLeaderboardUpdate(CommandSender sender) {
        leaderboardUpdateStartTime = Instant.now().getEpochSecond();
        main.getServerStatsManager().updateWealthStats(sender);
    }

    /**
     * Callback function for updating leaderboard message and leaderboard signs.
     *
     * @param sender user executing the update
     * @param tempSortedCache temporary cache for sorted player wealth to set the leaderboard
     */
    public void completeLeaderboardUpdate(CommandSender sender,
            HashMap<UUID, EntityLeaderboardCache> tempSortedCache) {
        MessageManager.setUpLeaderboard(tempSortedCache, main.getConfig().getDouble(
                "minimum-wealth", 0.0), main.getOptions().groupIsEnabled(),
                main.getServerStatsManager().getGroupUuidToNameMap());
        lastUpdateDuration = Instant.now().getEpochSecond() - leaderboardUpdateStartTime;
        MessageManager.sendMessage(sender, "update-complete",
                new String[]{"%time%"},
                new String[]{String.valueOf(lastUpdateDuration)});
        Bukkit.getScheduler().runTask(main, () -> {
            try {
                new SignHelper(main).updateSigns();
            } catch (NullPointerException e) {
                main.getLogger().warning(e.getMessage());
            }
        });
        isUpdating = false;
    }

    /**
     * Stops all existing leaderboard tasks.
     */
    public void stopExistingTasks() {
        this.leaderboardUpdateStartTime = -1;
        if (leaderboardTask != null) {
            leaderboardTask.cancel();
            leaderboardTask = null;
        }
        this.isUpdating = false;
    }

    /**
     * Checks if there is an ongoing leaderboard task.
     *
     * @return true if the leaderboard update is in progress, false otherwise
     */
    public boolean isUpdating() {
        return this.isUpdating;
    }

    /**
     * Gets the start time of the last leaderboard update.
     *
     * @return time since epoch for when the last leaderboard update was started
     */
    public long getLeaderboardUpdateStartTime() {
        return this.leaderboardUpdateStartTime;
    }

    /**
     * Gets the duration for the last leaderboard update.
     *
     * @return time in seconds taken for last leaderboard update
     */
    public long getLastUpdateDuration() {
        return this.lastUpdateDuration;
    }

    /**
     * Handles interruption of leaderboard update.
     *
     * @param sender sender who initiated the leaderboard update
     */
    public void interruptLeaderboardUpdate(CommandSender sender) {
        MessageManager.sendMessage(sender, "update-interrupted");
        lastUpdateDuration = -1;
        this.isUpdating = false;
    }
}
