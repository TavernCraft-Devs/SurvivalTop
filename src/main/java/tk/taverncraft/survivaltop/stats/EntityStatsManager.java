package tk.taverncraft.survivaltop.stats;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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

    // map of sender uuid to the gui to show sender
    private HashMap<UUID, EntityStatsGui> senderGui = new HashMap<>();

    /**
     * Constructor for EntityStatsManager.
     *
     * @param main plugin class
     */
    public EntityStatsManager(Main main) {
        this.main = main;
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

        final double tempLandWealth = landWealth;
        final double tempBalWealth = balWealth;
        final double tempInvWealth = invWealth;
        boolean useGui = main.getConfig().getBoolean("use-gui-stats", true);
        new BukkitRunnable() {
            @Override
            public void run() {
            boolean isPlayer = sender instanceof Player;
            UUID uuid = main.getSenderUuid(sender);
            double spawnerValue = main.getLandManager().calculateSpawnerWorthForIndividual(
                    uuid, useGui);
            double containerValue = main.getLandManager().calculateContainerWorthForIndividual(
                    uuid, useGui);

            // handle gui or non-gui results
            if (useGui && isPlayer) {
                prepareSenderStatsGui(sender, name, tempBalWealth, tempLandWealth, spawnerValue,
                        containerValue, tempInvWealth);
            } else {
                postEntityStatsProcessing(sender, name, null, tempBalWealth, tempLandWealth,
                        spawnerValue, containerValue, tempInvWealth);
            }
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
    private void prepareSenderStatsGui(CommandSender sender, String name, double... values) {
        UUID uuid = this.main.getSenderUuid(sender);
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                EntityStatsGui gui = new EntityStatsGui(main, uuid, name, values);
                senderGui.put(uuid, gui);
                postEntityStatsProcessing(sender, name, gui, values);
            }
        }.runTaskAsynchronously(main);
        this.statsUiTask.put(uuid, task);
    }

    /**
     * Cleans up after an entity's stats has been retrieved. Also updates spawner values if
     * applicable.
     *
     * @param sender sender who checked for stats
     * @param name name of entity to get stats for
     * @param gui gui to show stats in
     */
    private void postEntityStatsProcessing(CommandSender sender, String name, EntityStatsGui gui,
            double... values) {
        if (gui == null) {
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

            MessageManager.sendMessage(sender, "entity-stats",
                    new String[]{"%entity%", "%landwealth%", "%balwealth%", "%totalwealth%",
                            "%blockwealth%", "%spawnerwealth%", "%containerwealth%",
                            "%inventorywealth%"},
                    new String[]{name, new BigDecimal(strLandWealth).toPlainString(),
                            new BigDecimal(strBalWealth).toPlainString(),
                            new BigDecimal(strTotalWealth).toPlainString(),
                            new BigDecimal(strBlockWealth).toPlainString(),
                            new BigDecimal(strSpawnerWealth).toPlainString(),
                            new BigDecimal(strContainerWealth).toPlainString(),
                            new BigDecimal(strInvWealth).toPlainString()});
        } else {
            TextComponent message = new TextComponent("Click here to view stats!");
            message.setColor(ChatColor.GOLD);
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/st openstatsinv"));
            sender.spigot().sendMessage(message);
        }

        UUID uuid = this.main.getSenderUuid(sender);
        this.main.getLandManager().resetSenderLists(uuid);
        this.main.getInventoryManager().resetSenderLists(uuid);
        isCalculatingStats.remove(uuid);
        statsInitialTask.remove(uuid);
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
        return this.main.getLandManager().getLand(uuid, name,
                this.main.getLandManager().getBlockOperationsForIndividual());
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
        return this.main.getInventoryManager().getEntityInventoryWorth(uuid, name);
    }

    /**
     * Gets the balance wealth of an entity.
     *
     * @param name name of entity to get balance wealth for
     *
     * @return double value representing entity balance wealth
     */
    private double getEntityBalWealth(String name) {
        // handle if group is enabled
        if (this.main.groupIsEnabled()) {
            return main.getBalanceManager().getBalanceByGroup(name);
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        return main.getBalanceManager().getBalanceByPlayer(player);
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


