package tk.taverncraft.survivaltop.task.processor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.cache.EntityCache;
import tk.taverncraft.survivaltop.messages.MessageManager;
import tk.taverncraft.survivaltop.task.queue.Task;
import tk.taverncraft.survivaltop.task.queue.TaskQueue;
import tk.taverncraft.survivaltop.utils.types.MutableInt;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static tk.taverncraft.survivaltop.task.queue.TaskType.LEADERBOARD;

public class TaskProcessor {
    private final Main main;
    private final TaskQueue taskQueue;

    public TaskProcessor(Main main, TaskQueue taskQueue) {
        this.main = main;
        this.taskQueue = taskQueue;
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

        if (!taskQueue.hasTask(id)) {
            MessageManager.sendMessage(sender, "calculation-interrupted");
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

                if (!taskQueue.hasTask(id)) {
                    MessageManager.sendMessage(sender, "calculation-interrupted");
                    return;
                }

                EntityCache eCache = new EntityCache(name, balWealth, papiWealth, blockWealth,
                    spawnerValue, containerValue, inventoryWealth);
                eCache.setCounters(blockCounter, spawnerCounter, containerCounter, inventoryCounter);
                Task task = taskQueue.removeTask(id);

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
                taskQueue.removeCreator(main.getSenderUuid(sender));

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
                main.getCacheManager().saveToStatsCache(name.toUpperCase(), eCache);
                MessageManager.sendGuiStatsReadyMessage(sender, name.toUpperCase());
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
        main.getCacheManager().saveToStatsCache(name.toUpperCase(), eCache);
        MessageManager.sendChatStatsReadyMessage(sender, eCache);
        doCleanUp(id);
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
     * Cleans up trackers and lists used in calculating stats.
     *
     * @param id key to identify task
     */
    private void doCleanUp(int id) {
        main.getLandManager().doCleanUp(id);
        main.getInventoryManager().doCleanUp(id);
        taskQueue.removeTask(id);
    }
}
