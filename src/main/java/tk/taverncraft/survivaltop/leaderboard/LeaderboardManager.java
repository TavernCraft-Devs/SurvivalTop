package tk.taverncraft.survivaltop.leaderboard;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.storage.SqlHelper;
import tk.taverncraft.survivaltop.utils.MessageManager;
import tk.taverncraft.survivaltop.utils.ValidationManager;

import java.util.*;

public class LeaderboardManager {
    Main main;
    ValidationManager validationManager;
    private boolean isUpdating;
    BukkitTask scheduledTask;
    BukkitTask updateTask;

    public LeaderboardManager(Main main) {
        this.main = main;
        this.validationManager = new ValidationManager(main);
        initializeValues();
    }

    public void initializeValues() {
        cancelAllTasks();
    }

    /**
     * Scheduled entry point for updating leaderboard.
     *
     * @param frequency frequency of update
     * @param delay the delay before first update
     */
    public void scheduleLeaderboardUpdate(int frequency, int delay) {
        if (frequency == -1 && delay == 0) {
            scheduledTask = new BukkitRunnable() {

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
        scheduledTask = new BukkitRunnable() {

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
    public void manualUpdateLeaderboard(CommandSender sender) {
        new BukkitRunnable() {

            @Override
            public void run() {
                isUpdating = true;
                initiateLeaderboardUpdate(sender);
            }

        }.runTaskAsynchronously(main);
    }

    /**
     * Updates the entire leaderboard. May take a while to run if player-base is large.
     *
     * @param sender user executing the update
     */
    public void initiateLeaderboardUpdate(CommandSender sender) {
        main.getServerStatsManager().updateWealthStats(sender);
    }

    public void completeLeaderboardUpdate(CommandSender sender, HashMap<UUID, Double> tempSortedCache) {
        MessageManager.setUpLeaderboard(tempSortedCache, main.getConfig().getDouble("minimum" +
            "-wealth", 0.0), main.groupIsEnabled(), this.main.getServerStatsManager().getGroupUuidToNameMap());
        MessageManager.sendMessage(sender, "update-complete");
        Bukkit.getScheduler().runTask(main, () -> {
            try {
                new SignHelper(main).updateSigns();
            } catch (NullPointerException e) {
            }
        });
        isUpdating = false;
    }

    public void cancelAllTasks() {
        if (scheduledTask != null) {
            scheduledTask.cancel();
            scheduledTask = null;
        }

        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }

        SqlHelper.query = "";
        this.isUpdating = false;
    }

    public boolean isUpdating() {
        return this.isUpdating;
    }
}
