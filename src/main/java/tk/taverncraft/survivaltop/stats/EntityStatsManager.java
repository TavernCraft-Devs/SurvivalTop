package tk.taverncraft.survivaltop.stats;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.ui.EntityStatsGui;
import tk.taverncraft.survivaltop.utils.MessageManager;

/**
 * EntityStatsManager handles all logic for getting entity (player/group) stats but does not
 * store any information. Information storage belongs to the overall ServerStatsManager.
 */
public class EntityStatsManager {
    private Main main;

    // prevent stats command spam by tracking stats tasks
    public final HashMap<UUID, Boolean> isCalculatingStats = new HashMap<>();
    public final HashMap<UUID, BukkitTask> statsInitialTask = new HashMap<>();
    public final HashMap<UUID, BukkitTask> statsUiTask = new HashMap<>();

    // for tracking blocks, spawners and containers to be used in gui
    // the below maps are used by EntityStatsGui to create the user interface
    // note that uuid below is uuid of the sender
    private HashMap<UUID, HashMap<String, Integer>> blocksForGuiStats = new HashMap<>();
    private HashMap<UUID, HashMap<String, Integer>> spawnersForGuiStats = new HashMap<>();
    private HashMap<UUID, HashMap<String, Integer>> containersForGuiStats = new HashMap<>();
    private HashMap<UUID, HashMap<String, Integer>> inventoriesForGuiStats = new HashMap<>();

    // map of sender uuid to the gui to show sender
    private HashMap<UUID, EntityStatsGui> senderGui = new HashMap<>();

    /**
     * Constructor for EntityStatsManager.
     *
     * @param main plugin class
     */
    public EntityStatsManager(Main main) {
        this.main = main;
        initializeValues();
    }

    public void initializeValues() {
        blocksForGuiStats = new HashMap<>();
        spawnersForGuiStats = new HashMap<>();
        containersForGuiStats = new HashMap<>();
        inventoriesForGuiStats = new HashMap<>();
    }

    /**
     * Entry point for getting the stats of an entity.
     *
     * @param name name of entity
     */
    public void getEntityStats(CommandSender sender, UUID uuid, String name) {
        setCalculatingStats(sender);
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                calculateEntityStats(sender, name);
            }
        }.runTaskAsynchronously(main);
        statsInitialTask.put(uuid, task);
    }

    /**
     * Calculates the stats of an entity.
     *
     * @param sender user who requested for stats
     * @param name name of entity to get stats for
     */
    private void calculateEntityStats(CommandSender sender, String name) {
        double landWealth = 0;
        double balWealth = 0;
        double invWealth = 0;
        if (main.landIsIncluded()) {
            landWealth = getEntityLandWealth(sender, name);
        }
        if (main.balIsIncluded()) {
            balWealth = getEntityBalWealth(name);
        }
        if (main.inventoryIsIncluded()) {
            invWealth = getEntityInvWealth(sender, name);
        }
        processSpawnersAndContainers(sender, name, balWealth, landWealth, invWealth);
    }

    /**
     * Processes spawners and containers on the main thread (required).
     *
     * @param sender user who is getting stats
     * @param name name of entity whose stats is being retrieved
     * @param balWealth bal wealth of the entity
     * @param landWealth land wealth of the entity
     * @param invWealth inv wealth of the entity
     */
    private void processSpawnersAndContainers(CommandSender sender, String name, double balWealth,
            double landWealth, double invWealth) {
        new BukkitRunnable() {
            @Override
            public void run() {
                boolean isPlayer = sender instanceof Player;
                UUID uuid = main.getSenderUuid(sender);
                double spawnerValue = main.getLandManager().calculateSpawnerWorthForStats(uuid);
                double containerValue = main.getLandManager().calculateContainerWorthForStats(uuid);

                // handle gui or non-gui results
                if (main.isUseGuiStats() && isPlayer) {
                    processStatsGui(sender, name, balWealth, landWealth, spawnerValue,
                        containerValue, invWealth);
                    return;
                }
                executePostCalculationActions(sender, name, balWealth, landWealth, spawnerValue,
                        containerValue, invWealth);
            }
        }.runTask(main);
    }

    /**
     * Helper function for preparing the GUI stats for the sender.
     *
     * @param sender sender who checked for stats
     * @param name name of entity to get stats for
     * @param values wealth values of the entity
     */
    private void processStatsGui(CommandSender sender, String name, double... values) {
        UUID uuid = this.main.getSenderUuid(sender);
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                EntityStatsGui gui = new EntityStatsGui(main, uuid, name, values);
                senderGui.put(uuid, gui);
                executePostCalculationActions(sender);
            }
        }.runTaskAsynchronously(main);
        this.statsUiTask.put(uuid, task);
    }

    /**
     * Cleans up after an entity's stats has been retrieved. Also updates spawner/container
     * values if applicable and sends message for user to access the gui.
     *
     * @param sender user who requested for stats
     */
    private void executePostCalculationActions(CommandSender sender) {
        TextComponent message = new TextComponent("Click here to view stats!");
        message.setColor(ChatColor.GOLD);
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/st openstatsinv"));
        sender.spigot().sendMessage(message);
        doCleanUp(sender);
    }

    /**
     * Cleans up after an entity's stats has been retrieved. Also updates spawner/container
     * values if applicable and sends stats in chat.
     *
     * @param sender user who requested for stats
     * @param name name of entity to get stats for
     * @param values balance values breakdown
     */
    private void executePostCalculationActions(CommandSender sender, String name, double... values) {
        double balValue = values[0];
        double blockValue = values[1];
        double spawnerValue = values[2];
        double containerValue = values[3];
        double inventoryValue = values[4];
        double landValue = blockValue + spawnerValue + containerValue;
        double totalValue = balValue + landValue + inventoryValue;

        String strTotalWealth = String.format("%.02f", totalValue);
        String strBalWealth = String.format("%.02f", balValue);
        String strLandWealth = String.format("%.02f", landValue);
        String strBlockWealth = String.format("%.02f", blockValue);
        String strSpawnerWealth = String.format("%.02f", spawnerValue);
        String strContainerWealth = String.format("%.02f", containerValue);
        String strInvWealth = String.format("%.02f", inventoryValue);

        String[] placeholders = new String[]{"%entity%", "%landwealth%", "%balwealth%",
                "%totalwealth%", "%blockwealth%", "%spawnerwealth%", "%containerwealth%",
                "%inventorywealth%"};

        String[] wealthValues = new String[]{name, new BigDecimal(strLandWealth).toPlainString(),
            new BigDecimal(strBalWealth).toPlainString(),
            new BigDecimal(strTotalWealth).toPlainString(),
            new BigDecimal(strBlockWealth).toPlainString(),
            new BigDecimal(strSpawnerWealth).toPlainString(),
            new BigDecimal(strContainerWealth).toPlainString(),
            new BigDecimal(strInvWealth).toPlainString()};

        MessageManager.sendMessage(sender, "entity-stats", placeholders, wealthValues);
        doCleanUp(sender);
    }

    /**
     * Cleans up trackers and lists used in calculating stats.
     *
     * @param sender user who requested for stats
     */
    private void doCleanUp(CommandSender sender) {
        UUID uuid = this.main.getSenderUuid(sender);
        this.main.getLandManager().doCleanup(uuid);
        blocksForGuiStats.remove(uuid);
        spawnersForGuiStats.remove(uuid);
        containersForGuiStats.remove(uuid);
        inventoriesForGuiStats.remove(uuid);
        isCalculatingStats.remove(uuid);
        statsInitialTask.remove(uuid);
    }

    /**
     * Sets block count and value to be used in gui.
     *
     * @param uuid sender to link block to
     * @param block block to check for
     */
    public void setBlocksForGuiStats(UUID uuid, Block block) {
        blocksForGuiStats.computeIfAbsent(uuid, k -> new HashMap<>());
        String name = block.getType().toString();
        Integer currentCount = blocksForGuiStats.get(uuid).get(name);
        if (currentCount == null) {
            currentCount = 0;
        }
        blocksForGuiStats.get(uuid).put(name, currentCount + 1);
    }

    /**
     * Sets spawner count and value to be used in gui.
     *
     * @param uuid sender to link spawner to
     * @param mobName spawner name to check for
     */
    public void setSpawnersForGuiStats(UUID uuid, String mobName) {
        spawnersForGuiStats.computeIfAbsent(uuid, k -> new HashMap<>());
        Integer currentCount = spawnersForGuiStats.get(uuid).get(mobName);
        if (currentCount == null) {
            currentCount = 0;
        }
        spawnersForGuiStats.get(uuid).put(mobName, currentCount + 1);
    }

    /**
     * Sets container count and value to be used in gui.
     *
     * @param uuid sender to link container to
     * @param itemName item name to check for
     */
    public void setContainersForGuiStats(UUID uuid, String itemName, int amount) {
        containersForGuiStats.computeIfAbsent(uuid, k -> new HashMap<>());
        Integer currentCount = containersForGuiStats.get(uuid).get(itemName);
        if (currentCount == null) {
            currentCount = 0;
        }
        containersForGuiStats.get(uuid).put(itemName, currentCount + amount);
    }

    /**
     * Sets inventory item count and value to be used in gui.
     *
     * @param uuid sender to link inventory to
     * @param itemName item name to check for
     */
    public void setInventoriesForGuiStats(UUID uuid, String itemName, int amount) {
        inventoriesForGuiStats.computeIfAbsent(uuid, k -> new HashMap<>());
        Integer currentCount = inventoriesForGuiStats.get(uuid).get(itemName);
        if (currentCount == null) {
            currentCount = 0;
        }
        inventoriesForGuiStats.get(uuid).put(itemName, currentCount + amount);
    }

    /**
     * Gets the blocks to show sender in GUI.
     *
     * @return hashmap of block name to its worth
     */
    public HashMap<String, Integer> getBlocksForGuiStats(UUID uuid) {
        return this.blocksForGuiStats.get(uuid);
    }

    /**
     * Gets the spawners to show sender in GUI.
     *
     * @return hashmap of spawner name to its worth
     */
    public HashMap<String, Integer> getSpawnersForGuiStats(UUID uuid) {
        return this.spawnersForGuiStats.get(uuid);
    }

    /**
     * Gets the container items to show sender in GUI.
     *
     * @return hashmap of container item name to its worth
     */
    public HashMap<String, Integer> getContainersForGuiStats(UUID uuid) {
        return this.containersForGuiStats.get(uuid);
    }

    /**
     * Gets the inventory items to show sender in GUI.
     *
     * @return hashmap of inventory item name to its worth
     */
    public HashMap<String, Integer> getInventoriesForGuiStats(UUID uuid) {
        return this.inventoriesForGuiStats.get(uuid);
    }

    /**
     * Gets the land wealth of an entity.
     *
     * @param sender sender who checked for stats
     * @param name name of entity to get land wealth for
     *
     * @return double value representing entity land wealth
     */
    private double getEntityLandWealth(CommandSender sender, String name) {
        UUID uuid = this.main.getSenderUuid(sender);
        return this.main.getLandManager().getLandWorthForEntity(uuid, name, false);
    }

    /**
     * Gets the inventory wealth of an entity.
     *
     * @param sender sender who checked for stats
     * @param name name of entity to get inventory wealth for
     *
     * @return double value representing entity inventory wealth
     */
    private double getEntityInvWealth(CommandSender sender, String name) {
        UUID uuid = this.main.getSenderUuid(sender);
        return this.main.getInventoryManager().getInventoryWorthForEntity(uuid, name, false);
    }

    /**
     * Gets the balance wealth of an entity.
     *
     * @param name name of entity to get balance wealth for
     *
     * @return double value representing entity balance wealth
     */
    private double getEntityBalWealth(String name) {
        return main.getBalanceManager().getBalanceForEntity(name);
    }

    /**
     * Sets the tracker for calculating stats task.
     *
     * @param sender sender who checked for stats
     */
    public void setCalculatingStats(CommandSender sender) {
        UUID uuid = this.main.getSenderUuid(sender);
        isCalculatingStats.put(uuid, true);
    }

    /**
     * Checks if sender has an ongoing calculation.
     *
     * @param uuid uuid of sender
     *
     * @return true if there is an ongoing calculation for the sender, false otherwise
     */
    public boolean senderHasCalculationInProgress(UUID uuid) {
        if (isCalculatingStats.get(uuid) == null) {
            return false;
        } else {
            return isCalculatingStats.get(uuid);
        }
    }

    /**
     * Disable any existing entity stats calculations.
     */
    public void stopEntityStatsCalculations() {
        for (UUID uuid : isCalculatingStats.keySet()) {
            isCalculatingStats.remove(uuid);
            BukkitTask initialTask = statsInitialTask.get(uuid);
            if (initialTask != null) {
                initialTask.cancel();
            }
            statsInitialTask.remove(uuid);
            BukkitTask finalTask = statsUiTask.get(uuid);
            if (finalTask != null) {
                finalTask.cancel();
            }
            statsUiTask.remove(uuid);
        }
    }

    /**
     * Special handler to open player inventory gui stats main page.
     *
     * @param uuid uuid of sender
     */
    public void openMainStatsPage(UUID uuid) {
        try {
            Bukkit.getPlayer(uuid).openInventory(senderGui.get(uuid).getMainStatsPage());
        } catch (Exception e) {
            Bukkit.getLogger().warning(e.getMessage());
        }
    }

    /**
     * Retrieves player inventory gui stats block page.
     *
     * @param uuid uuid of sender
     * @param pageNum page number to show
     *
     * @return inventory page containing block info for given page
     */
    public Inventory getBlockStatsPage(UUID uuid, int pageNum) {
        return senderGui.get(uuid).getBlockStatsPage(pageNum);
    }

    /**
     * Retrieves player inventory gui stats spawner page.
     *
     * @param uuid uuid of sender
     * @param pageNum page number to show
     *
     * @return inventory page containing spawner info for given page
     */
    public Inventory getSpawnerStatsPage(UUID uuid, int pageNum) {
        return senderGui.get(uuid).getSpawnerStatsPage(pageNum);
    }

    /**
     * Retrieves player inventory gui stats container page.
     *
     * @param uuid uuid of sender
     * @param pageNum page number to show
     *
     * @return inventory page containing container info for given page
     */
    public Inventory getContainerStatsPage(UUID uuid, int pageNum) {
        return senderGui.get(uuid).getContainerStatsPage(pageNum);
    }

    /**
     * Retrieves player inventory gui stats inventory page.
     *
     * @param uuid uuid of sender
     * @param pageNum page number to show
     *
     * @return inventory page containing inventory info for given page
     */
    public Inventory getInventoryStatsPage(UUID uuid, int pageNum) {
        return senderGui.get(uuid).getInventoryStatsPage(pageNum);
    }
}


