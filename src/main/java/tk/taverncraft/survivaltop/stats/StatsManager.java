package tk.taverncraft.survivaltop.stats;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.papi.PapiManager;
import tk.taverncraft.survivaltop.stats.cache.EntityCache;
import tk.taverncraft.survivaltop.stats.task.Task;
import tk.taverncraft.survivaltop.stats.task.TaskType;
import tk.taverncraft.survivaltop.utils.types.MutableInt;
import tk.taverncraft.survivaltop.messages.MessageManager;

import static tk.taverncraft.survivaltop.stats.task.TaskType.LEADERBOARD;
import static tk.taverncraft.survivaltop.stats.task.TaskType.PLAYER;

/**
 * StatsManager handles all logic for getting entity (player/group) stats but does not
 * store any information. Information storage belongs to the overall ServerStatsManager.
 */
public class StatsManager {
    private final Main main;

    // prevent stats command spam by tracking stats tasks
    private final ConcurrentHashMap<Integer, Task> taskMap = new ConcurrentHashMap<>();

    // used to track creators of tasks that are ongoing
    private final Set<UUID> creatorList = new HashSet<>();

    // entity cache, used if realtime stats are disabled
    private ConcurrentHashMap<String, EntityCache> entityCacheMap = new ConcurrentHashMap<>();

    /**
     * Constructor for StatsManager.
     *
     * @param main plugin class
     */
    public StatsManager(Main main) {
        this.main = main;
    }

    /**
     * Gets stats for an entity to update the leaderboard.
     *
     * @param sender sender who requested for stats
     * @param name name of entity to get stats for
     */
    public void getStatsForLeaderboard(CommandSender sender, String name) {
        getRealTimeStats(sender, name, LEADERBOARD);
    }

    /**
     * Gets stats for an entity for sender who requested via stats command.
     *
     * @param sender sender who requested for stats
     * @param name name of entity to get stats for
     */
    public void getStatsForPlayer(CommandSender sender, String name) {
        creatorList.add(main.getSenderUuid(sender));
        if (main.getOptions().isCalculationMode0()) {
            MessageManager.sendMessage(sender, "start-calculating-stats");
            getRealTimeStats(sender, name, PLAYER);
        } else {
            getCachedStats(sender, name);
        }
    }

    /**
     * Entry point for getting the real time stats of an entity.
     *
     * @param sender sender who requested for stats
     * @param name name of entity
     * @param type type of task
     */
    public void getRealTimeStats(CommandSender sender, String name, TaskType type) {
        int id = createTask(name, type);
        new BukkitRunnable() {
            @Override
            public void run() {
                calculateEntityStats(sender, name, id);
            }
        }.runTaskAsynchronously(main);
    }

    /**
     * Entry point for getting the cached stats of an entity.
     *
     * @param sender sender who requested for stats
     * @param name name of entity
     */
    public void getCachedStats(CommandSender sender, String name) {
        EntityCache eCache = entityCacheMap.get(name.toUpperCase());
        // if stats cache not found or invalid, look into leaderboard cache
        if (eCache == null || eCache.isExpired(main.getOptions().getCacheDuration())) {
            eCache = main.getLeaderboardManager().getEntityCache(name.toUpperCase());
            // if leaderboard cache also not found or invalid, calculate again
            if (eCache == null || eCache.isExpired(main.getOptions().getCacheDuration())) {
                if (main.getOptions().isCalculationMode1()) {
                    MessageManager.sendMessage(sender, "start-calculating-stats");
                    getRealTimeStats(sender, name, PLAYER);
                } else {
                    MessageManager.sendMessage(sender, "no-updated-leaderboard");
                    creatorList.remove(main.getSenderUuid(sender));
                    return;
                }
                return;
            }

            if (main.getOptions().isUseGuiStats() && sender instanceof Player) {
                eCache.setGui(main);
            } else {
                eCache.setChat();
            }
            entityCacheMap.put(name.toUpperCase(), eCache);
        }

        long timeElapsed = Instant.now().getEpochSecond() - eCache.getCacheTime();
        if (main.getOptions().isUseGuiStats() && sender instanceof Player) {
            main.getGuiManager().setSenderGui(main.getSenderUuid(sender), eCache);
            MessageManager.sendMessage(sender, "calculation-complete-cache",
                new String[]{"%time%"},
                new String[]{String.valueOf(timeElapsed)});
            main.getGuiManager().setSenderGui(main.getSenderUuid(sender), eCache);
            MessageManager.sendGuiStatsReadyMessage(sender);
        } else {
            MessageManager.sendMessage(sender, "calculation-complete-cache",
                new String[]{"%time%"},
                new String[]{String.valueOf(timeElapsed)});
            MessageManager.sendMessage(sender, "entity-stats", eCache.getPlaceholders(),
                eCache.getValues());
        }
        creatorList.remove(main.getSenderUuid(sender));
    }

    /**
     * Calculates the stats of an entity.
     *
     * @param sender sender who requested for stats
     * @param name name of entity
     * @param id key to identify task
     */
    public void calculateEntityStats(CommandSender sender, String name, int id) {
        main.getLandManager().setStopOperations(false);
        main.getInventoryManager().setStopOperations(false);
        double balWealth = 0;
        double blockValue = 0;
        double inventoryValue = 0;
        LinkedHashMap<String, Double> papiWealth = new LinkedHashMap<>();
        HashMap<String, MutableInt> blockCounter = new HashMap<>();
        HashMap<String, MutableInt> inventoryCounter = new HashMap<>();
        try {
            if (main.getOptions().landIsIncluded()) {
                // land calculations are done async and will be retrieved later
                processEntityLandWealth(name, id);
                blockValue = main.getLandManager().calculateBlockWorth(id);
                blockCounter = main.getLandManager().getBlocksForGui(id);
            }
            if (main.getOptions().balIsIncluded()) {
                balWealth = getEntityBalWealth(name);
            }
            if (main.getOptions().inventoryIsIncluded()) {
                processEntityInvWealth(name, id);
                inventoryValue = main.getInventoryManager().calculateInventoryWorth(id);
                inventoryCounter = main.getInventoryManager().getInventoriesForGui(id);
            }
            if (main.getOptions().papiIsIncluded()) {
                papiWealth = getEntityPapiWealth(name);
            }
        } catch (NullPointerException ignored) {

        }

        if (!taskMap.containsKey(id)) {
            stopAllCalculations(sender);
            return;
        }

        executePostCalculationActions(sender, name, id, balWealth, papiWealth, blockValue,
                inventoryValue, blockCounter, inventoryCounter);
    }

    /**
     * Handles post calculation actions after land has been processed (if applicable).
     *
     * @param sender sender who requested for stats
     * @param name name of entity
     * @param id key to identify task
     * @param papiWealth papi wealth of entity
     * @param balWealth bal wealth of the entity
     * @param blockWealth block wealth of entity
     * @param inventoryWealth inventory wealth of entity
     * @param blockCounter blocks counter of entity
     * @param inventoryCounter inventories counter of entity
     */
    private void executePostCalculationActions(CommandSender sender, String name, int id,
            double balWealth, LinkedHashMap<String, Double> papiWealth, double blockWealth,
            double inventoryWealth, HashMap<String, MutableInt> blockCounter,
            HashMap<String, MutableInt> inventoryCounter) {
        new BukkitRunnable() {
            @Override
            public void run() {
                double spawnerValue = 0;
                double containerValue = 0;
                HashMap<String, MutableInt> spawnerCounter = new HashMap<>();
                HashMap<String, MutableInt> containerCounter = new HashMap<>();
                if (main.getOptions().spawnerIsIncluded()) {
                    main.getLandManager().processSpawnerTypes(id);
                    spawnerValue = main.getLandManager().calculateSpawnerWorth(id);
                    spawnerCounter = main.getLandManager().getSpawnersForGui(id);
                }
                if (main.getOptions().containerIsIncluded()) {
                    main.getLandManager().processContainerItems(id);
                    containerValue = main.getLandManager().calculateContainerWorth(id);
                    containerCounter = main.getLandManager().getContainersForGui(id);
                }

                if (!taskMap.containsKey(id)) {
                    stopAllCalculations(sender);
                    return;
                }

                EntityCache eCache = new EntityCache(name, balWealth, papiWealth, blockWealth,
                        spawnerValue, containerValue, inventoryWealth);
                eCache.setCounters(blockCounter, spawnerCounter, containerCounter, inventoryCounter);
                Task task = taskMap.remove(id);

                // logic stops after this for leaderboard type
                if (task.getType() == LEADERBOARD) {
                    main.getLeaderboardManager().processLeaderboardUpdate(name, eCache);
                    return;
                }

                // remaining logic for player type
                long timeTaken = Instant.now().getEpochSecond() - task.getStartTime();
                MessageManager.sendMessage(sender, "calculation-complete-realtime",
                    new String[]{"%time%"},
                    new String[]{String.valueOf(timeTaken)});
                creatorList.remove(main.getSenderUuid(sender));

                // handle gui or non-gui results
                if (main.getOptions().isUseGuiStats() && sender instanceof Player) {
                    processStatsForGui(sender, name, id, eCache);
                } else {
                    processStatsForChat(sender, name, id, eCache);
                }
            }
        }.runTask(main);
    }

    /**
     * Cleans up after an entity's stats has been retrieved. Also updates spawner/container
     * values if applicable and sends link to gui stats in chat.
     *
     * @param sender sender who checked for stats
     * @param name name of entity
     * @param id key to identify task
     * @param eCache entity cache
     */
    private void processStatsForGui(CommandSender sender, String name, int id, EntityCache eCache) {
        new BukkitRunnable() {
            @Override
            public void run() {
                eCache.setGui(main);
                entityCacheMap.put(name.toUpperCase(), eCache);
                main.getGuiManager().setSenderGui(main.getSenderUuid(sender), eCache);
                MessageManager.sendGuiStatsReadyMessage(sender);
                doCleanUp(id);
            }
        }.runTaskAsynchronously(main);
    }

    /**
     * Cleans up after an entity's stats has been retrieved. Also updates spawner/container
     * values if applicable and sends stats in chat.
     *
     * @param sender user who requested for stats
     * @param name name of entity
     * @param id key to identify task
     * @param eCache entity cache
     */
    private void processStatsForChat(CommandSender sender, String name, int id,
            EntityCache eCache) {
        eCache.setChat();
        entityCacheMap.put(name.toUpperCase(), eCache);
        MessageManager.sendChatStatsReadyMessage(sender, eCache);
        doCleanUp(id);
    }

    /**
     * Cleans up trackers and lists used in calculating stats.
     *
     * @param id key to identify task
     */
    private void doCleanUp(int id) {
        main.getLandManager().doCleanUp(id);
        main.getInventoryManager().doCleanUp(id);
        taskMap.remove(id);
    }

    /**
     * Processes the land wealth of an entity.
     *
     * @param name name of entity
     * @param id key to identify task
     */
    private void processEntityLandWealth(String name, int id) {
        main.getLandManager().createHolder(id);
        this.main.getLandManager().processEntityLand(name, id);
    }

    /**
     * Processes the inventory wealth of an entity.
     *
     * @param name name of entity
     * @param id key to identify task
     */
    private void processEntityInvWealth(String name, int id) {
        this.main.getInventoryManager().createHolder(id);
        this.main.getInventoryManager().processInvWorth(name, id);
    }

    /**
     * Gets the balance wealth of an entity.
     *
     * @param name name of entity
     *
     * @return double value representing entity balance wealth
     */
    private double getEntityBalWealth(String name) {
        return main.getBalanceManager().getBalanceForEntity(name);
    }

    /**
     * Gets the papi wealth of an entity.
     *
     * @param name name of entity
     *
     * @return double value representing entity papi wealth
     */
    private LinkedHashMap<String, Double> getEntityPapiWealth(String name) {
        return main.getPapiManager().getPlaceholderValForEntity(name);
    }

    /**
     * Checks if sender has an ongoing calculation.
     *
     * @param sender sender who requested for stats
     *
     * @return true if there is an ongoing calculation for the sender, false otherwise
     */
    public boolean senderHasCalculationInProgress(CommandSender sender) {
        return creatorList.contains(main.getSenderUuid(sender));
    }

    /**
     * Sets the state for calculations to stop or continue.
     */
    public void stopAllCalculations(CommandSender sender) {
        taskMap.clear();
        this.entityCacheMap = new ConcurrentHashMap<>();
        creatorList.clear();
        if (sender != null) {
            MessageManager.sendMessage(sender, "calculation-interrupted");
        }
    }

    /**
     * Creates task
     *
     * @param name name of entity
     * @param type type of task
     *
     * @return number representing task id
     */
    private int createTask(String name, TaskType type) {
        Task task = new Task(name, type);
        int id = task.getTaskId();
        taskMap.put(id, task);
        return id;
    }
}


