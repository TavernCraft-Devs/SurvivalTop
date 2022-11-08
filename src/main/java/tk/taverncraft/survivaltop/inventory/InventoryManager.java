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

    // for tracking inventory items to be used in gui
    // note that uuid below is uuid of the sender
    private HashMap<UUID, HashMap<String, Integer>> senderInventoryForGui = new HashMap<>();

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
        senderInventoryForGui = new HashMap<>();
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
                Bukkit.getConsoleSender().sendMessage(e.getMessage());
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
     * @param uuid uuid of sender, not to be confused with the entity itself!
     * @param name name of entity to get inventory worth for
     *
     * @return double representing its worth
     */
    public double getEntityInventoryWorth(UUID uuid, String name) {
        if (this.main.groupIsEnabled()) {
            List<OfflinePlayer> players = this.main.getGroupManager().getPlayers(name);
            double totalWorth = 0;
            for (OfflinePlayer player : players) {
                totalWorth += getByPlayer(uuid, player.getName());
            }
            return totalWorth;
        } else {
            return getByPlayer(uuid, name);
        }
    }

    /**
     * Get the inventory worth for given player name.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     * @param name name of player to get inventory worth for
     * @return
     */
    private double getByPlayer(UUID uuid, String name) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            Inventory inventory = player.getInventory();
            double totalInventoryWorth = 0;
            for (ItemStack itemStack : inventory) {
                if (itemStack == null) {
                    continue;
                }
                String itemName = itemStack.getType().toString().toUpperCase();
                Double worth = inventoryWorth.get(itemName);
                if (worth == null) {
                    worth = (double) 0;
                } else if (true) {
                    setSenderInventoryForGui(uuid, itemName, itemStack.getAmount());
                }
                totalInventoryWorth += worth * itemStack.getAmount();
            }
            return totalInventoryWorth;
        } else {
            return 0;
        }
    }

    /**
     * Sets inventory item count and value to be used in gui.
     *
     * @param uuid sender to link inventory to
     * @param itemName item name to check for
     */
    public void setSenderInventoryForGui(UUID uuid, String itemName, int amount) {
        senderInventoryForGui.computeIfAbsent(uuid, k -> new HashMap<>());
        Integer currentCount = senderInventoryForGui.get(uuid).get(itemName);
        if (currentCount == null) {
            currentCount = 0;
        }
        senderInventoryForGui.get(uuid).put(itemName, currentCount + amount);
    }

    /**
     * Gets the inventory items to show sender in GUI.
     *
     * @return hashmap of inventory item name to its worth
     */
    public HashMap<String, Integer> getSenderInventoryForGui(UUID uuid) {
        return this.senderInventoryForGui.get(uuid);
    }

    /**
     * Resets a specific sender's info list after calculating stats.
     */
    public void resetSenderLists(UUID uuid) {
        senderInventoryForGui.remove(uuid);
    }
}
