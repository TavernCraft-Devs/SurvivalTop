package tk.taverncraft.survivaltop.land.operations;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import tk.taverncraft.survivaltop.Main;

/**
 * Handles the logic for performing container operations when scanning locations.
 */
public class ContainerOperations {
    private Main main;
    private LinkedHashMap<String, Double> containerWorth;

    // populated from main thread and processed on async thread later
    private HashMap<UUID, ArrayList<Block>> preprocessedContainersForLeaderboard = new HashMap<>();
    private HashMap<UUID, ArrayList<Block>> preprocessedContainersForStats = new HashMap<>();

    // todo: is there a better way?
    private final Set<Material> allowedTypes = EnumSet.of(
        Material.CHEST,
        Material.DROPPER,
        Material.HOPPER,
        Material.DISPENSER,
        Material.TRAPPED_CHEST,
        Material.FURNACE,
        Material.SHULKER_BOX,
        Material.WHITE_SHULKER_BOX,
        Material.ORANGE_SHULKER_BOX,
        Material.MAGENTA_SHULKER_BOX,
        Material.LIGHT_BLUE_SHULKER_BOX,
        Material.YELLOW_SHULKER_BOX,
        Material.LIME_SHULKER_BOX,
        Material.PINK_SHULKER_BOX,
        Material.GRAY_SHULKER_BOX,
        Material.LIGHT_GRAY_BANNER,
        Material.CYAN_SHULKER_BOX,
        Material.PURPLE_SHULKER_BOX,
        Material.BLUE_SHULKER_BOX,
        Material.BROWN_SHULKER_BOX,
        Material.GREEN_SHULKER_BOX,
        Material.RED_SHULKER_BOX,
        Material.BLACK_SHULKER_BOX
    );

    private Set<Material> containerTypes;

    /**
     * Constructor for ContainerOperations.
     *
     * @param main plugin class
     * @param containerWorth map of container item names to their values
     */
    public ContainerOperations(Main main, LinkedHashMap<String, Double> containerWorth) {
        this.main = main;
        this.containerWorth = containerWorth;
        this.containerTypes = new HashSet<>();
        setUpContainerType();
    }

    /**
     * Set up the containers chosen to be included.
     */
    private void setUpContainerType() {
        List<String> chosenContainers = main.getConfig().getStringList("container-type");
        for (String container : chosenContainers) {
            Material material = Material.valueOf(container);
            if (allowedTypes.contains(material)) {
                containerTypes.add(material);
            }
        }
    }

    public void doLeaderboardCleanup() {
        preprocessedContainersForLeaderboard = new HashMap<>();
    }

    public void doStatsCleanup(UUID uuid) {
        preprocessedContainersForStats.remove(uuid);
    }

    /**
     * Returns container operation for leaderboard.
     *
     * @return container operation for leaderboard
     */
    public BiFunction<UUID, Block, Double> getLeaderboardOperation() {
        return preprocessContainerForLeaderboard;
    }

    /**
     * Returns container operation for stats.
     *
     * @return container operation for stats.
     */
    public BiFunction<UUID, Block, Double> getStatsOperation() {
        return preprocessContainerForStats;
    }

    /**
     * Calculates container worth for all entities.
     *
     * @return map of entities uuid to their container worth
     */
    public HashMap<UUID, Double> calculateContainerWorthForLeaderboard() {
        HashMap<UUID, Double> tempContainerCache = new HashMap<>();
        for (Map.Entry<UUID, ArrayList<Block>> map : preprocessedContainersForLeaderboard.entrySet()) {
            UUID uuid = map.getKey();
            ArrayList<Block> blocks = map.getValue();
            double value = processContainerWorth(blocks, uuid, false);
            tempContainerCache.put(map.getKey(), value);
        }
        return tempContainerCache;
    }

    /**
     * Calculates container worth for a specified entity.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     *
     * @return map of sender uuid to the calculated container worth
     */
    public double calculateContainerWorthForStats(UUID uuid) {
        ArrayList<Block> blocks = preprocessedContainersForStats.get(uuid);
        if (blocks == null) {
            return 0;
        }
        return processContainerWorth(blocks, uuid, true);
    }

    /**
     * Process the worth of container items (for individuals who may need GUI).
     *
     * @param blocks list of containers
     *
     * @return double value representing total worth of container items
     */
    public double processContainerWorth(ArrayList<Block> blocks, UUID uuid, boolean isStatsAction) {
        if (blocks == null) {
            return 0;
        }
        double totalContainerWorth = 0;
        int numBlocks = blocks.size();
        for (int i = 0; i < numBlocks; i++) {
            Block block = blocks.get(i);
            BlockState blockstate = block.getState();
            Inventory inventory;
            if (blockstate instanceof Chest) {
                Chest chest = (Chest) blockstate;
                inventory = chest.getBlockInventory();
            } else {
                InventoryHolder inventoryHolder = (InventoryHolder) blockstate;
                inventory = inventoryHolder.getInventory();
            }
            for (ItemStack itemStack : inventory) {
                if (itemStack == null) {
                    continue;
                }
                String itemName = itemStack.getType().toString().toUpperCase();
                Double worth = containerWorth.get(itemName);
                if (worth == null) {
                    worth = (double) 0;
                } else if (main.isUseGuiStats() && isStatsAction) {
                    main.getEntityStatsManager().setContainersForGuiStats(uuid, itemName,
                        itemStack.getAmount());
                }
                totalContainerWorth += worth * itemStack.getAmount();
            }
        }
        return totalContainerWorth;
    }

    /**
     * Preprocess containers to be handled on main thread later for when the leaderboard command is
     * being updated. Uuid here belongs to the sender and comes with the block that is being
     * checked. This always returns 0 since if a block is not a container (ignored) and if it is
     * a container, then it is set to be processed later anyways
     */
    private BiFunction<UUID, Block, Double> preprocessContainerForLeaderboard = (uuid, block) -> {
        Material material = block.getType();
        if (containerTypes.contains(material)) {
            preprocessedContainersForLeaderboard.computeIfAbsent(uuid, k -> new ArrayList<>());
            preprocessedContainersForLeaderboard.get(uuid).add(block);
        }
        return 0.0;
    };


    /**
     * Preprocess containers to be handled on main thread later for when the stats command is
     * executed. Uuid here belongs to the sender and comes with the block that is being checked.
     * This always returns 0 since if a block is not a container (ignored) and if it is
     * a container, then it is set to be processed later anyways
     */
    private BiFunction<UUID, Block, Double> preprocessContainerForStats = (uuid, block) -> {
        Material material = block.getType();
        if (containerTypes.contains(material)) {
            preprocessedContainersForStats.computeIfAbsent(uuid, k -> new ArrayList<>());
            preprocessedContainersForStats.get(uuid).add(block);
        }
        return 0.0;
    };
}
