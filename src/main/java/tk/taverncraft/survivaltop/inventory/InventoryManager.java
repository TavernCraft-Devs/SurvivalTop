package tk.taverncraft.survivaltop.inventory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.inventory.holders.InventoryHolder;
import tk.taverncraft.survivaltop.utils.types.MutableInt;

/**
 * Handles logic for calculating the worth of entity inventory.
 */
public class InventoryManager {
    private final Main main;
    private LinkedHashMap<String, Double> inventoryWorth;
    private Set<String> inventoryMaterial;

    // boolean to allow reloads to stop current operations
    private boolean stopOperations = false;

    // holders containing count of each material mapped to uuid
    private HashMap<UUID, InventoryHolder> inventoryHolderMapForLeaderboard = new HashMap<>();
    private final ConcurrentHashMap<UUID, InventoryHolder> inventoryHolderMapForStats =
            new ConcurrentHashMap<>();

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
     * Cleans up holders after leaderboard update.
     */
    public void doCleanUpForLeaderboard() {
        inventoryHolderMapForLeaderboard = new HashMap<>();
    }

    /**
     * Cleans up holders after stats update.
     *
     * @param uuid uuid of sender
     */
    public void doCleanUpForStats(UUID uuid) {
        inventoryHolderMapForStats.remove(uuid);
    }

    /**
     * Creates holders for leaderboard.
     *
     * @param uuid uuid of each entity
     */
    public void createHolderForLeaderboard(UUID uuid) {
        inventoryHolderMapForLeaderboard.put(uuid, new InventoryHolder(inventoryMaterial));
    }

    /**
     * Creates holders for stats.
     *
     * @param uuid uuid of sender, not to confused with the entity itself!
     */
    public void createHolderForStats(UUID uuid) {
        inventoryHolderMapForStats.put(uuid, new InventoryHolder(inventoryMaterial));
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
        inventoryMaterial = inventoryWorth.keySet();
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
     * @param material material of inventory item
     *
     * @return double representing its worth
     */
    public double getInventoryItemWorth(String material) {
        return this.inventoryWorth.get(material);
    }

    /**
     * Processes the worth of inventories for given entity.
     *
     * @param uuid uuid of each entity
     * @param name name of entity to get inventory worth for
     */
    public void processInvWorthForLeaderboard(UUID uuid, String name) {
        if (this.main.getOptions().groupIsEnabled()) {
            List<OfflinePlayer> players = this.main.getGroupManager().getPlayers(name);
            for (OfflinePlayer player : players) {
                processPlayerForLeaderboard(uuid, player.getName());
            }
        } else {
            processPlayerForLeaderboard(uuid, name);
        }
    }

    /**
     * Processes the worth of inventories for given entity.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     * @param name name of entity to get inventory worth for
     */
    public void processInvWorthForStats(UUID uuid, String name) {
        if (this.main.getOptions().groupIsEnabled()) {
            List<OfflinePlayer> players = this.main.getGroupManager().getPlayers(name);
            for (OfflinePlayer player : players) {
                if (stopOperations) {
                    return;
                }
                processPlayerForStats(uuid, player.getName());
            }
        } else {
            if (stopOperations) {
                return;
            }
            processPlayerForStats(uuid, name);
        }
    }

    /**
     * Processes the inventory worth for given player name.
     *
     * @param uuid uuid of each entity
     * @param name name of player to get inventory worth for
     */
    private void processPlayerForLeaderboard(UUID uuid, String name) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            Inventory inventory = player.getInventory();
            processInventoryItemsForLeaderboard(uuid, inventory);
        }
    }

    /**
     * Processes the inventory worth for given player name.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     * @param name name of player to get inventory worth for
     */
    private void processPlayerForStats(UUID uuid, String name) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            Inventory inventory = player.getInventory();
            processInventoryItemsForStats(uuid, inventory);
        }
    }

    /**
     * Processes the worth of inventory items.
     *
     * @param uuid uuid of each entity
     * @param inventory inventory to process
     */
    private void processInventoryItemsForLeaderboard(UUID uuid, Inventory inventory) {
        for (ItemStack itemStack : inventory) {
            if (itemStack == null) {
                continue;
            }
            String material = itemStack.getType().name();
            if (inventoryMaterial.contains(material)) {
                inventoryHolderMapForLeaderboard.get(uuid).addToHolder(material,
                    itemStack.getAmount());
            }
        }
    }

    /**
     * Processes the worth of inventory items.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     * @param inventory inventory to process
     */
    private void processInventoryItemsForStats(UUID uuid, Inventory inventory) {
        for (ItemStack itemStack : inventory) {
            if (itemStack == null) {
                continue;
            }
            String material = itemStack.getType().name();
            if (inventoryMaterial.contains(material)) {
                inventoryHolderMapForStats.get(uuid).addToHolder(material, itemStack.getAmount());
            }
        }
    }

    /**
     * Calculates inventory worth for all entities.
     *
     * @return map of entities uuid to their inventory worth
     */
    public HashMap<UUID, Double> calculateInventoryWorthForLeaderboard() {
        HashMap<UUID, Double> inventoryWorthMap = new HashMap<>();
        for (Map.Entry<UUID, InventoryHolder> map : inventoryHolderMapForLeaderboard.entrySet()) {
            double value = getAllInventoriesWorth(map.getValue());
            inventoryWorthMap.put(map.getKey(), value);
        }
        return inventoryWorthMap;
    }

    /**
     * Calculates inventory worth for a specified entity.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     *
     * @return map of sender uuid to the calculated inventory worth
     */
    public double calculateInventoryWorthForStats(UUID uuid) {
        return getAllInventoriesWorth(inventoryHolderMapForStats.get(uuid));
    }

    /**
     * Gets the total worth of inventories.
     *
     * @param inventoryHolder holder containing inventory item count
     *
     * @return double value representing total worth of inventories
     */
    public double getAllInventoriesWorth(InventoryHolder inventoryHolder) {
        double totalInventoryWorth = 0;
        HashMap<String, MutableInt> counter = inventoryHolder.getCounter();
        for (Map.Entry<String, MutableInt> map : counter.entrySet()) {
            // count multiply by worth, then added to total
            totalInventoryWorth += map.getValue().get() * inventoryWorth.get(map.getKey());
        }
        return totalInventoryWorth;
    }

    /**
     * Gets the inventory counter to show in GUI.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     *
     * @return inventory counter
     */
    public HashMap<String, MutableInt> getInventoriesForGuiStats(UUID uuid) {
        return inventoryHolderMapForStats.get(uuid).getCounter();
    }

    /**
     * Sets the state for operations to stop or continue.
     *
     * @param state state to set operations to
     */
    public void setStopOperations(boolean state) {
        this.stopOperations = state;
    }
}
