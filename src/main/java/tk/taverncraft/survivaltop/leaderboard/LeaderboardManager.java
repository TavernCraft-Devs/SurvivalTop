package tk.taverncraft.survivaltop.leaderboard;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import tk.taverncraft.survivaltop.stats.cache.EntityCache;

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

    // map used during an ongoing leaderboard update
    private ConcurrentHashMap<String, EntityCache> entityCacheMap;

    // maps below are used for papi

    // copy is used for consistent papi values
    private ConcurrentHashMap<String, EntityCache> entityCacheMapCopy;

    // cached positions
    private ConcurrentHashMap<String, Integer> positionCacheMap;
    private ArrayList<EntityCache> entityCacheList;

    /**
     * Constructor for LeaderboardManager.
     *
     * @param main plugin class
     */
    public LeaderboardManager(Main main) {
        this.main = main;
        stopExistingScheduleTasks();
        initializeValues();
    }

    /**
     * Initializes all values to default.
     */
    public void initializeValues() throws NullPointerException {
        positionCacheMap = new ConcurrentHashMap<>();
        entityCacheMap = new ConcurrentHashMap<>();
        entityCacheMapCopy = new ConcurrentHashMap<>();
        entityCacheList = new ArrayList<>();
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
                main.getStatsManager().getStatsForLeaderboard(sender, leaderboardTaskQueue.next());
            }
        } catch (Exception e) {
            LogManager.error(e.getMessage());
            this.main.getLeaderboardManager().stopExistingScheduleTasks();
        }
    }

    /**
     * Sets the leaderboard task queue for players.
     */
    private void setTaskQueueForPlayers() {
        boolean filterLastJoin = this.main.getConfig().getBoolean("filter-last-join",
                false);
        long lastJoinTime = this.main.getConfig().getLong("last-join-time", 2592000) *
                1000;

        // path for if last join filter is off or if last join time is set <= 0 (cannot filter)
        if (!filterLastJoin || lastJoinTime <= 0) {
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
    public void completeLeaderboardUpdate(CommandSender sender,
            HashMap<String, EntityCache> tempSortedCache) {
        if (main.getOptions().isUseHoverableLeaderboard()) {
            MessageManager.setUpHoverableLeaderboard(tempSortedCache, main.getConfig().getDouble(
                    "minimum-wealth", 0.0),
                    main.getOptions().getLeaderboardPositionsPerPage());
        } else {
            MessageManager.setUpLeaderboard(tempSortedCache, main.getConfig().getDouble(
                    "minimum-wealth", 0.0),
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
     * Sorts entities by total wealth and filters for total leaderboard position limit shown
     * if applicable.
     *
     * @param hm hashmap of entity wealth to sort
     *
     * @return sorted total wealth hashmap
     */
    private HashMap<String, EntityCache> sortAndFilterEntities(ConcurrentHashMap<String,
        EntityCache> hm) {
        List<Map.Entry<String, EntityCache> > list =
            new LinkedList<>(hm.entrySet());

        list.sort((o1, o2) -> (o2.getValue().getTotalWealth())
            .compareTo(o1.getValue().getTotalWealth()));

        HashMap<String, EntityCache> temp = new LinkedHashMap<>();
        int totalLeaderboardPositions = main.getOptions().getTotalLeaderboardPositions();
        if (totalLeaderboardPositions >= 0) {
            for (Map.Entry<String, EntityCache> aa : list) {
                if (totalLeaderboardPositions == 0) {
                    break;
                }
                temp.put(aa.getKey(), aa.getValue());
                totalLeaderboardPositions--;
            }
        } else {
            for (Map.Entry<String, EntityCache> aa : list) {
                temp.put(aa.getKey(), aa.getValue());
            }
        }
        return temp;
    }

    /**
     * Sets entity position and entity cache list for easy papi access.
     *
     * @param tempSortedCache hashmap to use to generate cache for
     */
    private void setUpEntityCache(HashMap<String, EntityCache> tempSortedCache) {
        this.positionCacheMap = new ConcurrentHashMap<>();
        int i = 0;
        for (String nameKey : tempSortedCache.keySet()) {
            this.positionCacheMap.put(nameKey, i);
            i++;
        }
        this.entityCacheList = new ArrayList<>(tempSortedCache.values());
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
        entityCacheMap.put(name.toUpperCase(), eCache);
        if (!leaderboardTaskQueue.hasNext()) {
            HashMap<String, EntityCache> sortedMap = sortAndFilterEntities(entityCacheMap);
            setUpEntityCache(sortedMap);
            entityCacheMapCopy = entityCacheMap;
            completeLeaderboardUpdate(leaderboardSender, sortedMap);
            runCommandsOnEnd();
            main.getStorageManager().saveToStorage(entityCacheList);
        } else {
            main.getStatsManager().getStatsForLeaderboard(leaderboardSender,
                    leaderboardTaskQueue.next());
        }
    }

    /**
     * Gets the entity cache in the leaderboard.
     *
     * @param name name of entity
     *
     * @return cache of entity
     */
    public EntityCache getEntityCache(String name) {
        return entityCacheMap.get(name);
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
                EntityCache eCache = entityCacheList.get(index);
                command = command.replaceAll(placeholder, eCache.getName());
            }
        }

        Pattern playerPattern = Pattern.compile("%(player-\\d+)%");
        Matcher playerMatcher = playerPattern.matcher(command);

        if (playerMatcher.find()) {
            String placeholder = playerMatcher.group(0);
            int index = Integer.parseInt(playerMatcher.group(1).split("-")[1]) - 1;
            EntityCache eCache = entityCacheList.get(index);
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

    // functions below are called by the papi manager to retrieve leaderboard values

    /**
     * Gets the name of an entity at given position.
     *
     * @param index position to get entity name at
     *
     * @return name of entity at specified position
     */
    public String getEntityNameAtPosition(int index) {
        EntityCache eCache = this.entityCacheList.get(index);
        String name = eCache.getName();

        if (name == null) {
            return "None";
        }

        return name;
    }

    /**
     * Gets the wealth of an entity at given position.
     *
     * @param index position to get entity wealth at
     *
     * @return wealth of entity at specified position
     */
    public String getEntityWealthAtPosition(int index) {
        EntityCache eCache = this.entityCacheList.get(index);
        Double value = eCache.getTotalWealth();

        if (value != null) {
            return String.format("%.02f", value);
        } else {
            return "None";
        }
    }

    /**
     * Gets the position of an entity with given name.
     *
     * @param name of entity
     *
     * @return position of given entity
     */
    public String getPositionOfEntity(String name) {
        Integer position = this.positionCacheMap.get(name);
        if (position != null) {
            position = position + 1; // index 0
            return String.format("%d", position);
        } else {
            return "None";
        }
    }

    /**
     * Gets the balance wealth of an entity with given name.
     *
     * @param name of entity
     *
     * @return balance wealth of given entity
     */
    public String getEntityBalWealth(String name) {
        EntityCache eCache = entityCacheMapCopy.get(name);
        return String.format("%.02f", eCache.getBalWealth());
    }

    /**
     * Gets the land wealth of an entity with given name.
     *
     * @param name of entity
     *
     * @return land wealth of given entity
     */
    public String getEntityLandWealth(String name) {
        EntityCache eCache = entityCacheMapCopy.get(name);
        return String.format("%.02f", eCache.getLandWealth());
    }

    /**
     * Gets the block wealth of an entity with given name.
     *
     * @param name of entity
     *
     * @return block wealth of given entity
     */
    public String getEntityBlockWealth(String name) {
        EntityCache eCache = entityCacheMapCopy.get(name);
        return String.format("%.02f", eCache.getBlockWealth());
    }

    /**
     * Gets the spawner wealth of an entity with given name.
     *
     * @param name of entity
     *
     * @return spawner wealth of given entity
     */
    public String getEntitySpawnerWealth(String name) {
        EntityCache eCache = entityCacheMapCopy.get(name);
        return String.format("%.02f", eCache.getSpawnerWealth());
    }

    /**
     * Gets the container wealth of an entity with given name.
     *
     * @param name of entity
     *
     * @return container wealth of given entity
     */
    public String getEntityContainerWealth(String name) {
        EntityCache eCache = entityCacheMapCopy.get(name);
        return String.format("%.02f", eCache.getContainerWealth());
    }

    /**
     * Gets the inventory wealth of an entity with given name.
     *
     * @param name name of entity
     *
     * @return inventory wealth of given entity
     */
    public String getEntityInvWealth(String name) {
        EntityCache eCache = entityCacheMapCopy.get(name);
        return String.format("%.02f", eCache.getInventoryWealth());
    }

    /**
     * Gets the total wealth of an entity with given name.
     *
     * @param name of entity
     *
     * @return total wealth of given entity
     */
    public String getEntityTotalWealth(String name) {
        EntityCache eCache = entityCacheMapCopy.get(name);
        return String.format("%.02f", eCache.getTotalWealth());
    }
}
