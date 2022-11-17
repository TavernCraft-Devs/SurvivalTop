package tk.taverncraft.survivaltop.stats;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.logs.LogManager;
import tk.taverncraft.survivaltop.utils.types.MutableInt;
import tk.taverncraft.survivaltop.stats.cache.EntityLeaderboardCache;
import tk.taverncraft.survivaltop.ui.EntityStatsGui;
import tk.taverncraft.survivaltop.messages.MessageManager;

/**
 * EntityStatsManager handles all logic for getting entity (player/group) stats but does not
 * store any information. Information storage belongs to the overall ServerStatsManager.
 */
public class EntityStatsManager {
    private final Main main;

    // boolean to allow reloads to stop current calculations
    private boolean stopCalculations = false;

    // prevent stats command spam by tracking stats tasks
    private final HashMap<UUID, Long> calculationStartTime = new HashMap<>();

    // map of sender uuid to the gui to show sender
    private final HashMap<UUID, EntityStatsGui> senderGui = new HashMap<>();

    /**
     * Constructor for EntityStatsManager.
     *
     * @param main plugin class
     */
    public EntityStatsManager(Main main) {
        this.main = main;
    }

    /**
     * Entry point for getting the real time stats of an entity.
     *
     * @param sender sender who requested for stats
     * @param name name of entity
     */
    public void getRealTimeEntityStats(CommandSender sender, String name) {
        setCalculatingStats(sender);
        new BukkitRunnable() {
            @Override
            public void run() {
                calculateEntityStats(sender, name);
            }
        }.runTaskAsynchronously(main);
    }

    /**
     * Entry point for getting the cached stats of an entity.
     *
     * @param sender sender who requested for stats
     * @param uuid uuid of sender
     * @param name name of entity
     */
    public void getCachedEntityStats(CommandSender sender, UUID uuid, String name) {
        setCalculatingStats(sender);
        EntityLeaderboardCache eCache = main.getServerStatsManager().getEntityCache(name);
        // default to real time values if cache not found i.e. leaderboard not updated
        if (eCache == null) {
            getRealTimeEntityStats(sender, name);
            return;
        }

        if (main.getOptions().isUseGuiStats()) {
            handleCachedStatsInGui(sender, name, eCache);
        } else {
            handleCachedStatsInChat(sender, name, eCache);
        }
        calculationStartTime.remove(uuid);
    }

    /**
     * Handles sending cached entity stats in a gui.
     *
     * @param sender sender who checked for stats
     * @param name name of entity to get stats for
     * @param eCache cache for the entity
     */
    private void handleCachedStatsInGui(CommandSender sender, String name, EntityLeaderboardCache eCache) {
        UUID uuid = this.main.getSenderUuid(sender);
        new BukkitRunnable() {
            @Override
            public void run() {
                EntityStatsGui gui = new EntityStatsGui(main, name, eCache);
                senderGui.put(uuid, gui);
                showEntityStatsToUser(sender);
            }
        }.runTaskAsynchronously(main);
    }

    /**
     * Handles sending cached entity stats in chat.
     *
     * @param sender sender who checked for stats
     * @param name name of entity to get stats for
     * @param eCache cache for the entity
     */
    private void handleCachedStatsInChat(CommandSender sender, String name, EntityLeaderboardCache eCache) {
        double balValue = eCache.getBalWealth();
        double landValue = eCache.getLandWealth();
        double blockValue = eCache.getBlockWealth();
        double spawnerValue = eCache.getSpawnerWealth();
        double containerValue = eCache.getContainerWealth();
        double inventoryValue = eCache.getInventoryWealth();
        double totalValue = eCache.getTotalWealth();
        sendStatsAsMessage(sender, name, balValue, landValue, blockValue, spawnerValue,
                containerValue, inventoryValue, totalValue);
    }

    /**
     * Calculates the stats of an entity.
     *
     * @param sender user who requested for stats
     * @param name name of entity to get stats for
     */
    private void calculateEntityStats(CommandSender sender, String name) {
        try {
            main.getLandManager().setStopOperations(false);
            main.getInventoryManager().setStopOperations(false);
            stopCalculations = false;
            UUID uuid = main.getSenderUuid(sender);
            double balWealth = 0;
            double blockValue = 0;
            double inventoryValue = 0;
            if (main.getOptions().landIsIncluded()) {
                // land calculations are done async and will be retrieved later
                processEntityLandWealth(sender, name);
                blockValue = main.getLandManager().calculateBlockWorthForStats(uuid);
            }
            if (main.getOptions().balIsIncluded()) {
                balWealth = getEntityBalWealth(name);
            }
            if (main.getOptions().inventoryIsIncluded()) {
                processEntityInvWealth(sender, name);
                inventoryValue = main.getInventoryManager().calculateInventoryWorthForStats(uuid);
            }
            executePostCalculationActions(sender, uuid, name, balWealth, blockValue, inventoryValue);
        } catch (Exception ignored) {
            interruptStatsCalculations(sender);
            // sometimes exception is thrown when reloading is done in the middle of calculations
        }
    }

    /**
     * Handles post calculation actions after land has been processed (if applicable).
     *
     * @param sender sender who requested for stats
     * @param uuid uuid of sender
     * @param name name of entity whose stats are being retrieved
     * @param balWealth bal wealth of the entity
     */
    private void executePostCalculationActions(CommandSender sender, UUID uuid, String name,
            double balWealth, double blockWealth, double inventoryWealth) {
        if (stopCalculations) {
            interruptStatsCalculations(sender);
            return;
        }
         new BukkitRunnable() {
            @Override
            public void run() {
                if (main.getOptions().spawnerIsIncluded()) {
                    main.getLandManager().processSpawnerTypesForStats(uuid);
                }
                if (main.getOptions().containerIsIncluded()) {
                    main.getLandManager().processContainerItemsForStats(uuid);
                }
                double spawnerValue = main.getLandManager().calculateSpawnerWorthForStats(uuid);
                double containerValue = main.getLandManager().calculateContainerWorthForStats(uuid);

                if (stopCalculations) {
                    interruptStatsCalculations(sender);
                    return;
                }

                long timeTaken = Instant.now().getEpochSecond() - calculationStartTime.get(uuid);
                MessageManager.sendMessage(sender, "calculation-complete",
                    new String[]{"%time%"},
                    new String[]{String.valueOf(timeTaken)});

                // handle gui or non-gui results
                if (main.getOptions().isUseGuiStats() && sender instanceof Player) {
                    processStatsGui(sender, name, balWealth, blockWealth, spawnerValue,
                        containerValue, inventoryWealth);
                    return;
                }

                showEntityStatsToUser(sender, name, balWealth, blockWealth, spawnerValue,
                        containerValue, inventoryWealth);
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
        new BukkitRunnable() {
            @Override
            public void run() {
                EntityStatsGui gui = new EntityStatsGui(main, uuid, name, values);
                senderGui.put(uuid, gui);
                showEntityStatsToUser(sender);
            }
        }.runTaskAsynchronously(main);
    }

    /**
     * Cleans up after an entity's stats has been retrieved. Also updates spawner/container
     * values if applicable and sends message for user to access the gui.
     *
     * @param sender user who requested for stats
     */
    private void showEntityStatsToUser(CommandSender sender) {
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
    private void showEntityStatsToUser(CommandSender sender, String name, double... values) {
        double balValue = values[0];
        double blockValue = values[1];
        double spawnerValue = values[2];
        double containerValue = values[3];
        double inventoryValue = values[4];
        double landValue = blockValue + spawnerValue + containerValue;
        double totalValue = balValue + landValue + inventoryValue;

        sendStatsAsMessage(sender, name, balValue, landValue, blockValue, spawnerValue,
                containerValue, inventoryValue, totalValue);
        doCleanUp(sender);
    }

    /**
     * Helper method for sending stats in chat.
     *
     * @param sender sender who checked for stats
     * @param name name of entity to get stats  for
     * @param balValue balance value of entity
     * @param landValue land value of entity
     * @param blockValue block value of entity
     * @param spawnerValue spawner value of entity
     * @param containerValue container value of entity
     * @param inventoryValue inventory value of entity
     * @param totalValue total value of entity
     */
    private void sendStatsAsMessage(CommandSender sender, String name, double balValue,
            double landValue, double blockValue, double spawnerValue, double containerValue,
            double inventoryValue, double totalValue) {
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
    }

    /**
     * Cleans up trackers and lists used in calculating stats.
     *
     * @param sender user who requested for stats
     */
    private void doCleanUp(CommandSender sender) {
        UUID uuid = this.main.getSenderUuid(sender);
        main.getLandManager().doCleanUpForStats(uuid);
        main.getInventoryManager().doCleanUpForStats(uuid);
        calculationStartTime.remove(uuid);
    }

    /**
     * Gets the blocks to show sender in GUI.
     *
     * @return hashmap of block name to its worth
     */
    public HashMap<String, MutableInt> getBlocksForGuiStats(UUID uuid) {
        return main.getLandManager().getBlocksForGuiStats(uuid);
    }

    /**
     * Gets the spawners to show sender in GUI.
     *
     * @return hashmap of spawner name to its worth
     */
    public HashMap<String, MutableInt> getSpawnersForGuiStats(UUID uuid) {
        return main.getLandManager().getSpawnersForGuiStats(uuid);
    }

    /**
     * Gets the container items to show sender in GUI.
     *
     * @return hashmap of container item name to its worth
     */
    public HashMap<String, MutableInt> getContainersForGuiStats(UUID uuid) {
        return main.getLandManager().getContainersForGuiStats(uuid);
    }

    /**
     * Gets the inventory items to show sender in GUI.
     *
     * @return hashmap of inventory item name to its worth
     */
    public HashMap<String, MutableInt> getInventoriesForGuiStats(UUID uuid) {
        return main.getInventoryManager().getInventoriesForGuiStats(uuid);
    }

    /**
     * Processes the land wealth of an entity.
     *
     * @param sender sender who checked for stats
     * @param name name of entity to get land wealth for
     */
    private void processEntityLandWealth(CommandSender sender, String name) {
        UUID uuid = this.main.getSenderUuid(sender);
        main.getLandManager().createHoldersForStats(uuid);
        this.main.getLandManager().processEntityLand(uuid, name, false);
    }

    /**
     * Processes the inventory wealth of an entity.
     *
     * @param sender sender who checked for stats
     * @param name name of entity to get inventory wealth for
     */
    private void processEntityInvWealth(CommandSender sender, String name) {
        UUID uuid = this.main.getSenderUuid(sender);
        this.main.getInventoryManager().createHolderForStats(uuid);
        this.main.getInventoryManager().processInvWorthForStats(uuid, name);
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
        calculationStartTime.put(uuid, Instant.now().getEpochSecond());
    }

    /**
     * Checks if sender has an ongoing calculation.
     *
     * @param uuid uuid of sender
     *
     * @return true if there is an ongoing calculation for the sender, false otherwise
     */
    public boolean senderHasCalculationInProgress(UUID uuid) {
        return calculationStartTime.containsKey(uuid);
    }

    /**
     * Disable any existing entity stats calculations.
     */
    public void stopEntityStatsCalculations() {
        for (UUID uuid : calculationStartTime.keySet()) {
            calculationStartTime.remove(uuid);
        }
    }

    // todo: re-look at how gui stats is done in the next gui update

    /**
     * Special handler to open player inventory gui stats main page.
     *
     * @param uuid uuid of sender
     */
    public void openMainStatsPage(UUID uuid) {
        try {
            Bukkit.getPlayer(uuid).openInventory(senderGui.get(uuid).getMainStatsPage());
        } catch (Exception e) {
            LogManager.error(e.getMessage());
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

    /**
     * Sets the state for calculations to stop or continue.
     *
     * @param state state to set calculations to
     */
    public void setStopCalculations(boolean state) {
        this.stopCalculations = state;
    }

    /**
     * Handles interruption of leaderboard update.
     *
     * @param sender sender who initiated the leaderboard update
     */
    public void interruptStatsCalculations(CommandSender sender) {
        MessageManager.sendMessage(sender, "calculation-interrupted");
        calculationStartTime.remove(main.getSenderUuid(sender));
    }
}


