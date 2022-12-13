package tk.taverncraft.survivaltop.inventory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.inventory.holders.InventoryHolder;
import tk.taverncraft.survivaltop.logs.LogManager;
import tk.taverncraft.survivaltop.utils.types.MutableInt;

/**
 * Handles logic for calculating the worth of entity inventory.
 */
public class InventoryManager {
    private final Main main;
    private LinkedHashMap<String, Double> inventoryWorth;
    private Set<String> inventoryMaterial;

    // boolean to allow reloads to stop current inventory operations
    private boolean stopOperations = false;

    // holders containing count of each material mapped to uuid
    private final ConcurrentHashMap<Integer, InventoryHolder> inventoryHolderMap =
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
        FileConfiguration config = main.getConfigManager().getInventoriesConfig();
        for (String key : config.getConfigurationSection("").getKeys(false)) {
            try {
                Material material = Material.getMaterial(key);
                if (material == null) {
                    continue;
                }
                inventoryWorth.put(key.toUpperCase(), config.getDouble(key));
            } catch (Exception e) {
                LogManager.warn(e.getMessage());
            }
        }
        inventoryMaterial = inventoryWorth.keySet();
    }

    /**
     * Creates holders for stats.
     *
     * @param id key to identify task
     */
    public void createHolder(int id) {
        inventoryHolderMap.put(id, new InventoryHolder(inventoryMaterial));
    }

    /**
     * Cleans up holders after stats update.
     *
     * @param id key to identify task
     */
    public void doCleanUp(int id) {
        inventoryHolderMap.remove(id);
    }

    /**
     * Calculates inventory worth for a specified entity.
     *
     * @param id key to identify task
     *
     * @return map of sender uuid to the calculated inventory worth
     */
    public double calculateInventoryWorth(int id) {
        return getAllInventoriesWorth(inventoryHolderMap.get(id));
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
     * @param name name of entity to get inventory worth for
     * @param id key to identify task
     */
    public void processInvWorth(String name, int id) {
        if (this.main.getOptions().groupIsEnabled()) {
            List<OfflinePlayer> players = this.main.getGroupManager().getPlayers(name);
            for (OfflinePlayer player : players) {
                if (stopOperations) {
                    return;
                }
                processPlayer(player.getName(), id);
            }
        } else {
            if (stopOperations) {
                return;
            }
            processPlayer(name, id);
        }
    }

    /**
     * Processes the inventory worth for given player name.
     *
     * @param name name of player to get inventory worth for
     * @param id key to identify task
     */
    private void processPlayer(String name, int id) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            Inventory inventory = player.getInventory();
            processInventoryItems(id, inventory);
        }
    }

    /**
     * Processes the worth of inventory items.
     *
     * @param id key to identify task
     * @param inventory inventory to process
     */
    private void processInventoryItems(int id, Inventory inventory) {
        for (ItemStack itemStack : inventory) {
            if (itemStack == null) {
                continue;
            }
            String material = itemStack.getType().name();
            if (inventoryMaterial.contains(material)) {
                inventoryHolderMap.get(id).addToHolder(material,
                    itemStack.getAmount());
            }
        }
    }

    /**
     * Gets the inventory counter to show in GUI.
     *
     * @param id key to identify task
     *
     * @return inventory counter
     */
    public HashMap<String, MutableInt> getInventoriesForGui(int id) {
        return inventoryHolderMap.get(id).getCounter();
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
