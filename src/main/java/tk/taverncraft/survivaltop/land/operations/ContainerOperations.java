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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.land.operations.holders.ContainerHolder;
import tk.taverncraft.survivaltop.utils.types.MutableInt;

/**
 * Handles the logic for performing container operations when scanning locations.
 */
public class ContainerOperations {
    private final Main main;
    private final LandOperationsHelper landOperationsHelper;
    private final LinkedHashMap<String, Double> containerWorth;
    private final Set<String> containerMaterial;

    // holders containing count of each material mapped to uuid
    private HashMap<UUID, ContainerHolder> containerHolderMapForLeaderboard = new HashMap<>();
    private final ConcurrentHashMap<UUID, ContainerHolder> containerHolderMapForStats =
            new ConcurrentHashMap<>();

    // populated from main thread and processed on async thread later
    private HashMap<UUID, ArrayList<Block>> preprocessedContainersForLeaderboard = new HashMap<>();
    private final ConcurrentHashMap<UUID, ArrayList<Block>> preprocessedContainersForStats =
            new ConcurrentHashMap<>();

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

    private Set<String> containerTypes;

    /**
     * Constructor for ContainerOperations.
     *
     * @param main plugin class
     * @param landOperationsHelper helper for land operations
     * @param containerWorth map of container item names to their values
     */
    public ContainerOperations(Main main, LandOperationsHelper landOperationsHelper,
            LinkedHashMap<String, Double> containerWorth) {
        this.main = main;
        this.landOperationsHelper = landOperationsHelper;
        this.containerWorth = containerWorth;
        this.containerTypes = new HashSet<>();
        this.containerMaterial = containerWorth.keySet();
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
                containerTypes.add(material.name());
            }
        }
    }

    /**
     * Returns container holder for given uuid.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     *
     * @return container holder for given uuid
     */
    public ContainerHolder getContainerHolderForStats(UUID uuid) {
        return containerHolderMapForStats.get(uuid);
    }

    /**
     * Returns container operation for leaderboard.
     *
     * @return container operation for leaderboard
     */
    public BiFunction<UUID, Block, Boolean> getLeaderboardOperation() {
        return preprocessContainerForLeaderboard;
    }

    /**
     * Returns container operation for stats.
     *
     * @return container operation for stats
     */
    public BiFunction<UUID, Block, Boolean> getStatsOperation() {
        return preprocessContainerForStats;
    }

    /**
     * Cleans up holders and preprocessed containers after leaderboard update.
     */
    public void doCleanUpForLeaderboard() {
        containerHolderMapForLeaderboard = new HashMap<>();
        preprocessedContainersForLeaderboard = new HashMap<>();
    }

    /**
     * Cleans up holders and preprocessed containers after stats update.
     *
     * @param uuid uuid of sender
     */
    public void doCleanUpForStats(UUID uuid) {
        containerHolderMapForStats.remove(uuid);
        preprocessedContainersForStats.remove(uuid);
    }

    /**
     * Creates holders for leaderboard.
     *
     * @param uuid uuid of each entity
     */
    public void createHolderForLeaderboard(UUID uuid) {
        containerHolderMapForLeaderboard.put(uuid, new ContainerHolder(containerMaterial));

        // temp array list also needed for tracking containers
        preprocessedContainersForLeaderboard.put(uuid, new ArrayList<>());
    }

    /**
     * Creates holders for stats.
     *
     * @param uuid uuid of sender, not to confused with the entity itself!
     */
    public void createHolderForStats(UUID uuid) {
        containerHolderMapForStats.put(uuid, new ContainerHolder(containerMaterial));

        // temp array list also needed for tracking containers
        preprocessedContainersForStats.put(uuid, new ArrayList<>());
    }

    /**
     * Calculates container worth for all entities.
     *
     * @return map of entities uuid to their container worth
     */
    public HashMap<UUID, Double> calculateContainerWorthForLeaderboard() {
        HashMap<UUID, Double> containerWorthMap = new HashMap<>();
        for (Map.Entry<UUID, ContainerHolder> map : containerHolderMapForLeaderboard.entrySet()) {
            double value = getAllContainersWorth(map.getValue());
            containerWorthMap.put(map.getKey(), value);
        }
        return containerWorthMap;
    }

    /**
     * Calculates container worth for a specified entity.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     *
     * @return map of sender uuid to the calculated container worth
     */
    public double calculateContainerWorthForStats(UUID uuid) {
        return getAllContainersWorth(containerHolderMapForStats.get(uuid));
    }

    /**
     * Gets the total worth of container items.
     *
     * @param containerHolder holder containing container item count
     *
     * @return double value representing total worth of containers
     */
    public double getAllContainersWorth(ContainerHolder containerHolder) {
        double totalContainerWorth = 0;
        HashMap<String, MutableInt> counter = containerHolder.getCounter();
        for (Map.Entry<String, MutableInt> map : counter.entrySet()) {
            // count multiply by worth, then added to total
            totalContainerWorth += map.getValue().get() * containerWorth.get(map.getKey());
        }
        return totalContainerWorth;
    }

    /**
     * Processes the worth of container items.
     */
    public void processContainerItemsForLeaderboard() {
        for (Map.Entry<UUID, ArrayList<Block>> map : preprocessedContainersForLeaderboard.entrySet()) {
            UUID uuid = map.getKey();
            ArrayList<Block> blocks = map.getValue();
            int numBlocks = blocks.size();
            for (int i = 0; i < numBlocks; i++) {
                if (landOperationsHelper.getStopOperations()) {
                    return;
                }
                Inventory inventory = getBlockInventory(blocks.get(i));
                for (ItemStack itemStack : inventory) {
                    if (itemStack == null) {
                        continue;
                    }
                    String material = itemStack.getType().name();
                    if (containerMaterial.contains(material)) {
                        containerHolderMapForLeaderboard.get(uuid).addToHolder(material);
                    }
                }
            }
        }
    }

    /**
     * Processes the worth of container items.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     */
    public void processContainerItemsForStats(UUID uuid) {
        ArrayList<Block> blocks = preprocessedContainersForStats.get(uuid);
        int numBlocks = blocks.size();
        for (int i = 0; i < numBlocks; i++) {
            if (landOperationsHelper.getStopOperations()) {
                return;
            }
            Inventory inventory = getBlockInventory(blocks.get(i));
            for (ItemStack itemStack : inventory) {
                if (itemStack == null) {
                    continue;
                }
                String material = itemStack.getType().name();
                if (containerMaterial.contains(material)) {
                    containerHolderMapForStats.get(uuid).addToHolder(material);
                }
            }
        }
    }

    /**
     * Helper method for getting inventory of container block.
     *
     * @param block block to get inventory for
     *
     * @return inventory of given block
     */
    private Inventory getBlockInventory(Block block) {
        BlockState blockstate = block.getState();
        Inventory inventory;
        if (blockstate instanceof Chest) {
            Chest chest = (Chest) blockstate;
            inventory = chest.getBlockInventory();
        } else {
            InventoryHolder inventoryHolder = (InventoryHolder) blockstate;
            inventory = inventoryHolder.getInventory();
        }
        return inventory;
    }

    /**
     * Preprocesses containers to be handled on main thread later for when the leaderboard command is
     * being updated. Uuid here belongs to the sender and comes with the block that is being
     * checked. This always returns 0 since if a block is not a container (ignored) and if it is
     * a container, then it is set to be processed later anyways
     */
    private final BiFunction<UUID, Block, Boolean> preprocessContainerForLeaderboard = (uuid, block) -> {
        if (containerTypes.contains(block.getType().name())) {
            preprocessedContainersForLeaderboard.get(uuid).add(block);
            return true;
        }
        return false;
    };


    /**
     * Preprocesses containers to be handled on main thread later for when the stats command is
     * executed. Uuid here belongs to the sender and comes with the block that is being checked.
     * This always returns 0 since if a block is not a container (ignored) and if it is
     * a container, then it is set to be processed later anyways
     */
    private final BiFunction<UUID, Block, Boolean> preprocessContainerForStats = (uuid, block) -> {
        if (containerTypes.contains(block.getType().name())) {
            preprocessedContainersForStats.get(uuid).add(block);
            return true;
        }
        return false;
    };
}
