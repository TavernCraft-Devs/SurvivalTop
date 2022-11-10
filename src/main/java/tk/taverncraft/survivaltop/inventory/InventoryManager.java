package tk.taverncraft.survivaltop.inventory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import tk.taverncraft.survivaltop.Main;

/**
 * Handles logic for calculating the worth of entity inventory.
 */
public class InventoryManager {
    private Main main;
    private LinkedHashMap<String, Double> inventoryWorth = new LinkedHashMap<>();

    /**
     * Constructor for InventoryManager.
     *
     * @param main plugin class
     */
    public InventoryManager(Main main) {
        this.main = main;
        initializeWorth();
    }

    /**
     * Initializes values of inventory items.
     */
    public void initializeWorth() {
        this.loadInventoryWorth();
    }

    /**
     * Resets and loads all inventory item values.
     */
    private void loadInventoryWorth() {
        inventoryWorth = new LinkedHashMap<>();
        for (String key : main.getInventoriesConfig().getConfigurationSection("")
                .getKeys(false)) {
            try {
                Material material = Material.getMaterial(key);
                if (material == null) {
                    continue;
                }
                inventoryWorth.put(key.toUpperCase(), main.getInventoriesConfig().getDouble(key));
            } catch (Exception e) {
                Bukkit.getLogger().info(e.getMessage());
            }
        }
    }

    /**
     * Gets the map of worth for all inventory items.
     *
     * @return map of inventory item name to value
     */
    public LinkedHashMap<String, Double> getInventoryItemWorth() {
        return this.inventoryWorth;
    }

    /**
     * Gets the worth of an inventory item.
     *
     * @param name name of inventory item
     *
     * @return double representing its worth
     */
    public double getInventoryItemWorth(String name) {
        return this.inventoryWorth.get(name);
    }

    /**
     * Gets total worth of inventories for given entity.
     *
     * @param uuid uuid of sender if this is run through stats command; otherwise entities
     * @param name name of entity to get inventory worth for
     * @param isLeaderboardUpdate true if is a leaderboard update, false otherwise (i.e. stats)
     *
     * @return double representing its worth
     */
    public double getInventoryWorthForEntity(UUID uuid, String name, boolean isLeaderboardUpdate) {
        if (this.main.groupIsEnabled()) {
            List<OfflinePlayer> players = this.main.getGroupManager().getPlayers(name);
            double totalWorth = 0;
            for (OfflinePlayer player : players) {
                totalWorth += getByPlayer(uuid, player.getName(), isLeaderboardUpdate);
            }
            return totalWorth;
        } else {
            return getByPlayer(uuid, name, isLeaderboardUpdate);
        }
    }

    /**
     * Get the inventory worth for given player name.
     *
     * @param uuid uuid of sender if this is run through stats command; otherwise entities
     * @param name name of player to get inventory worth for
     *
     * @return double representing total worth
     */
    private double getByPlayer(UUID uuid, String name, boolean isLeaderboardUpdate) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            Inventory inventory = player.getInventory();
            double totalInventoryWorth = processInventoryWorth(uuid, inventory, !isLeaderboardUpdate);
            return totalInventoryWorth;
        } else {
            return 0;
        }
    }

    /**
     * Goes through every item in inventory to get total values.
     *
     * @param uuid uuid of sender if this is run through stats command; otherwise entities
     * @param inventory inventory of player to get worth for
     *
     * @return double representing total worth
     */
    private double processInventoryWorth(UUID uuid, Inventory inventory, boolean isStatsAction) {
        double totalInventoryWorth = 0;
        for (ItemStack itemStack : inventory) {
            if (itemStack == null) {
                continue;
            }
            String itemName = itemStack.getType().toString().toUpperCase();
            Double worth = inventoryWorth.get(itemName);
            if (worth == null) {
                worth = (double) 0;
            } else if (main.isUseGuiStats() && isStatsAction) {
                main.getEntityStatsManager().setInventoriesForGuiStats(uuid, itemName,
                    itemStack.getAmount());
            }
            totalInventoryWorth += worth * itemStack.getAmount();
        }
        return totalInventoryWorth;
    }
}
