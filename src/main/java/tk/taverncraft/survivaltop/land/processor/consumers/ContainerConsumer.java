package tk.taverncraft.survivaltop.land.processor.consumers;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import tk.taverncraft.survivaltop.land.processor.LandProcessor;
import tk.taverncraft.survivaltop.land.processor.holders.ContainerHolder;
import tk.taverncraft.survivaltop.utils.types.MutableInt;

/**
 * Handles the logic for performing container operations when scanning locations.
 */
public class ContainerConsumer {
    private final Main main;
    private final LandProcessor landProcessor;
    private final LinkedHashMap<String, Double> containerWorth;
    private final Set<String> containerMaterial;

    // holders containing count of each material mapped to uuid
    private final ConcurrentHashMap<Integer, ContainerHolder> containerHolderMap =
            new ConcurrentHashMap<>();

    // populated from main thread and processed on async thread later
    private final ConcurrentHashMap<Integer, ArrayList<Block>> preprocessedContainers =
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
     * Constructor for ContainerConsumer.
     *
     * @param main plugin class
     * @param landProcessor helper for land operations
     * @param containerWorth map of container item names to their values
     */
    public ContainerConsumer(Main main, LandProcessor landProcessor,
                             LinkedHashMap<String, Double> containerWorth) {
        this.main = main;
        this.landProcessor = landProcessor;
        this.containerWorth = containerWorth;
        this.containerTypes = new HashSet<>();
        this.containerMaterial = containerWorth.keySet();
        setUpContainerType();
    }

    /**
     * Sets up the containers chosen to be included.
     */
    private void setUpContainerType() {
        List<String> chosenContainers = main.getOptions().getContainerTypes();
        for (String container : chosenContainers) {
            Material material = Material.valueOf(container);
            if (allowedTypes.contains(material)) {
                containerTypes.add(material.name());
            }
        }
    }

    /**
     * Returns container operation for stats.
     *
     * @return container operation for stats
     */
    public BiFunction<Integer, Block, Boolean> getOperation() {
        return preprocessContainer;
    }

    /**
     * Creates holders for stats.
     *
     * @param id key to identify task
     */
    public void createHolder(int id) {
        containerHolderMap.put(id, new ContainerHolder(containerMaterial));

        // temp array list also needed for tracking containers
        preprocessedContainers.put(id, new ArrayList<>());
    }

    /**
     * Returns container holder for given uuid.
     *
     * @param id key to identify task
     *
     * @return container holder for given uuid
     */
    public ContainerHolder getContainerHolder(int id) {
        return containerHolderMap.get(id);
    }

    /**
     * Cleans up holders and preprocessed containers after stats update.
     *
     * @param id key to identify task
     */
    public void doCleanUp(int id) {
        containerHolderMap.remove(id);
        preprocessedContainers.remove(id);
    }

    /**
     * Calculates container worth for a specified entity.
     *
     * @param id key to identify task
     *
     * @return map of sender uuid to the calculated container worth
     */
    public double calculateContainerWorth(int id) {
        return getAllContainersWorth(containerHolderMap.get(id));
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
        if (containerHolder == null) {
            return 0;
        }
        HashMap<String, MutableInt> counter = containerHolder.getCounter();
        for (Map.Entry<String, MutableInt> map : counter.entrySet()) {
            // count multiply by worth, then added to total
            totalContainerWorth += map.getValue().get() * containerWorth.get(map.getKey());
        }
        return totalContainerWorth;
    }

    /**
     * Processes the worth of container items.
     *
     * @param id key to identify task
     */
    public void processContainerItems(int id) {
        ArrayList<Block> blocks = preprocessedContainers.get(id);
        int numBlocks = blocks.size();
        for (int i = 0; i < numBlocks; i++) {
            if (landProcessor.getStopOperations()) {
                return;
            }
            Inventory inventory = getBlockInventory(blocks.get(i));
            for (ItemStack itemStack : inventory) {
                if (itemStack == null) {
                    continue;
                }
                String material = itemStack.getType().name();
                if (containerMaterial.contains(material)) {
                    containerHolderMap.get(id).addToHolder(material, itemStack.getAmount());
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
     * Preprocesses containers to be handled on main thread later for when the stats command is
     * executed. Uuid here belongs to the sender and comes with the block that is being checked.
     * This always returns 0 since if a block is not a container (ignored) and if it is
     * a container, then it is set to be processed later anyways
     */
    private final BiFunction<Integer, Block, Boolean> preprocessContainer = (id, block) -> {
        if (containerTypes.contains(block.getType().name())) {
            preprocessedContainers.get(id).add(block);
            return true;
        }
        return false;
    };
}
