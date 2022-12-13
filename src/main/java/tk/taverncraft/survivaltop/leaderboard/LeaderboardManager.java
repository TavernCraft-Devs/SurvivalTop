package tk.taverncraft.survivaltop.leaderboard;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.logs.LogManager;
import tk.taverncraft.survivaltop.messages.MessageManager;
import tk.taverncraft.survivaltop.cache.EntityCache;

import static tk.taverncraft.survivaltop.task.queue.TaskType.LEADERBOARD;

/**
 * LeaderboardManager contains the main logic related to updating the leaderboard.
 */
public class LeaderboardManager {
    private final Main main;
    private boolean isUpdating;
    private BukkitTask scheduleTask;
    private long leaderboardUpdateStartTime = -1;
    private long lastUpdateDuration = -1;
    private Iterator<String> leaderboardTaskQueue;
    CommandSender leaderboardSender;

    /**
     * Constructor for LeaderboardManager.
     *
     * @param main plugin class
     */
    public LeaderboardManager(Main main) {
        this.main = main;
        stopExistingScheduleTasks();
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
            scheduleTask = new BukkitRunnable() {

                @Override
                public void run() {
                if (isUpdating) {
                    main.getLogger().info("Scheduled leaderboard update could not be " +
                            "carried out because an existing update is in progress.");
                    return;
                }
                isUpdating = true;
                Bukkit.getScheduler().runTask(main, () ->
                        initiateLeaderboardUpdate(Bukkit.getConsoleSender()));
                }

            }.runTaskAsynchronously(main);
            return;
        }
        long interval = frequency * 20L;
        long delayTicks = delay * 20L;
        scheduleTask = new BukkitRunnable() {

            @Override
            public void run() {
            if (isUpdating) {
                main.getLogger().info("Scheduled leaderboard update could not be " +
                        "carried out because an existing update is in progress.");
                return;
            }
            isUpdating = true;
            Bukkit.getScheduler().runTask(main, () ->
                    initiateLeaderboardUpdate(Bukkit.getConsoleSender()));
            }

        }.runTaskTimerAsynchronously(main, delayTicks, interval);
    }

    /**
     * Manual entry point for updating leaderboard.
     *
     * @param sender user executing the update
     */
    public void doManualLeaderboardUpdate(CommandSender sender) {
        isUpdating = true;
        initiateLeaderboardUpdate(sender);
    }

    /**
     * Initiates the leaderboard update.
     *
     * @param sender user executing the update
     */
    public void initiateLeaderboardUpdate(CommandSender sender) {
        leaderboardUpdateStartTime = Instant.now().getEpochSecond();
        try {
            MessageManager.sendMessage(sender, "update-started");
            leaderboardSender = sender;
            if (this.main.getOptions().groupIsEnabled()) {
                setTaskQueueForGroups();
            } else {
                setTaskQueueForPlayers();
            }
            runCommandsOnStart();
            if (leaderboardTaskQueue.hasNext()) {
                main.getTaskManager().createTask(sender, leaderboardTaskQueue.next(),
                        LEADERBOARD);
            }
        } catch (Exception e) {
            LogManager.error(e.getMessage());
            stopExistingScheduleTasks();
        }
    }

    /**
     * Sets the leaderboard task queue for players.
     */
    private void setTaskQueueForPlayers() {
        long lastJoinTime = main.getOptions().filterPlayerTime() * 1000;

        // path for if last join filter is off or if last join time is set <= 0 (cannot filter)
        if (!main.getOptions().filterLastJoin() || lastJoinTime <= 0) {
            leaderboardTaskQueue = Arrays.stream(this.main.getServer().getOfflinePlayers())
                    .map(OfflinePlayer::getName).iterator();
            return;
        }

        // path for if last join filter is on
        Instant instant = Instant.now();
        long currentTime = instant.getEpochSecond() * 1000;
        leaderboardTaskQueue = Arrays.stream(this.main.getServer().getOfflinePlayers())
                .filter(p -> currentTime - p.getLastPlayed() <= lastJoinTime)
                .map(OfflinePlayer::getName).iterator();
    }

    /**
     * Sets the leaderboard task queue for groups.
     */
    private void setTaskQueueForGroups() {
        List<String> groups = this.main.getGroupManager().getGroups();
        leaderboardTaskQueue = groups.iterator();
    }

    /**
     * Callback function for updating leaderboard message and leaderboard signs.
     *
     * @param sender user executing the update
     * @param tempSortedCache temporary cache for sorted player wealth to set the leaderboard
     */
    public void completeLeaderboardUpdate(CommandSender sender) {
        ArrayList<EntityCache> entityCacheList = main.getCacheManager().getEntityCacheList();
        if (main.getOptions().isUseInteractiveLeaderboard()) {
            MessageManager.setUpInteractiveLeaderboard(entityCacheList,
                    main.getOptions().getMinimumWealth(),
                    main.getOptions().getLeaderboardPositionsPerPage());
        } else {
            MessageManager.setUpLeaderboard(entityCacheList, main.getOptions().getMinimumWealth(),
                    main.getOptions().getLeaderboardPositionsPerPage());
        }
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
     * Stops all existing schedule tasks.
     */
    public void stopExistingScheduleTasks() {
        this.leaderboardUpdateStartTime = -1;
        if (scheduleTask != null) {
            scheduleTask.cancel();
            scheduleTask = null;
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
     * Processes task queue at end of each leaderboard task.
     *
     * @param name name of entity for which task just finished
     * @param eCache entity cache of entity
     */
    public void processLeaderboardUpdate(String name, EntityCache eCache) {
        main.getCacheManager().saveToLeaderboardCache(name.toUpperCase(), eCache);
        if (!leaderboardTaskQueue.hasNext()) {
            main.getCacheManager().processLeaderboardCache();
            completeLeaderboardUpdate(leaderboardSender);
            runCommandsOnEnd();
        } else {
            main.getTaskManager().createTask(leaderboardSender, leaderboardTaskQueue.next(),
                    LEADERBOARD);
        }
    }

    /**
     * Runs user specified commands at the start of a leaderboard update.
     */
    private void runCommandsOnStart() {
        List<String> commands = main.getOptions().getCommandsOnStart();
        for (String command : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    /**
     * Runs user specified commands after finishing a leaderboard update.
     */
    private void runCommandsOnEnd() {
        List<String> commands = main.getOptions().getCommandsOnEnd();
        for (String command : commands) {
            parseAndRunCommand(command);
        }
    }

    /**
     * Parses command placeholders for leaderboard entities before executing them. Supports only
     * {player-?} and {group-?} placeholders.
     *
     * @param command command to parse
     */
    private void parseAndRunCommand(String command) {
        if (main.getOptions().groupIsEnabled()) {
            Pattern groupPattern = Pattern.compile("%(group-\\d+)%");
            Matcher groupMatcher = groupPattern.matcher(command);

            if (groupMatcher.find()) {
                String placeholder = groupMatcher.group(0);
                int index = Integer.parseInt(groupMatcher.group(1).split("-")[1]) - 1;
                EntityCache eCache = main.getCacheManager().getCacheAtPosition(index);
                command = command.replaceAll(placeholder, eCache.getName());
            }
        }

        Pattern playerPattern = Pattern.compile("%(player-\\d+)%");
        Matcher playerMatcher = playerPattern.matcher(command);

        if (playerMatcher.find()) {
            String placeholder = playerMatcher.group(0);
            int index = Integer.parseInt(playerMatcher.group(1).split("-")[1]) - 1;
            EntityCache eCache = main.getCacheManager().getCacheAtPosition(index);
            if (eCache == null) {
                return;
            }
            String entityName = eCache.getName();
            if (main.getOptions().groupIsEnabled()) {
                List<OfflinePlayer> players = main.getGroupManager().getPlayers(entityName);
                for (OfflinePlayer player : players) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        command.replaceAll(placeholder, player.getName()));
                }
            } else {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        command.replaceAll(placeholder, entityName));
            }
            return;
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
