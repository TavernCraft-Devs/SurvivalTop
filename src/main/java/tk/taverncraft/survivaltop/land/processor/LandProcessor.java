package tk.taverncraft.survivaltop.land.processor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.function.BiFunction;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.land.processor.consumers.BlockConsumer;
import tk.taverncraft.survivaltop.land.processor.consumers.ContainerConsumer;
import tk.taverncraft.survivaltop.land.processor.consumers.SpawnerConsumer;
import tk.taverncraft.survivaltop.logs.LogManager;
import tk.taverncraft.survivaltop.utils.types.MutableInt;

/**
 * Helper function for loading the logic of calculations.
 */
public class LandProcessor {
    private final Main main;
    private BlockConsumer blockConsumer;
    private SpawnerConsumer spawnerConsumer;
    private ContainerConsumer containerConsumer;

    // boolean to allow reloads to stop current operations
    private boolean stopOperations = false;

    // list of operations to perform for calculating land wealth
    ArrayList<BiFunction<Integer, Block, Boolean>> landOperations = new ArrayList<>();

    // worth of blocks, spawners and containers
    private LinkedHashMap<String, Double> blockWorth = new LinkedHashMap<>();
    private LinkedHashMap<String, Double> spawnerWorth = new LinkedHashMap<>();
    private LinkedHashMap<String, Double> containerWorth = new LinkedHashMap<>();

    /**
     * Constructor for LandProcessor.
     *
     * @param main plugin class
     */
    public LandProcessor(Main main) {
        this.main = main;
        initializeWorth();
        initializeLandSubOperations();
        initializeLandOperations();
    }

    /**
     * Initializes worth of blocks/spawners/containers.
     */
    private void initializeWorth() {
        this.loadBlockWorth();
        this.loadSpawnerWorth();
        this.loadContainerWorth();
    }

    /**
     * Initializes block, spawner and container operations for land.
     */
    private void initializeLandSubOperations() {
        blockConsumer = new BlockConsumer(blockWorth);
        spawnerConsumer = new SpawnerConsumer(main, this, spawnerWorth);
        containerConsumer = new ContainerConsumer(main, this, containerWorth);
    }

    /**
     * Initializes land operations to be included.
     */
    private void initializeLandOperations() {
        landOperations = new ArrayList<>();

        if (main.getOptions().landIsIncluded()) {
            landOperations.add(blockConsumer.getOperation());
        } else {
            return;
        }
        if (main.getOptions().spawnerIsIncluded()) {
            landOperations.add(spawnerConsumer.getOperation());
        }

        if (main.getOptions().containerIsIncluded()) {
            landOperations.add(containerConsumer.getOperation());
        }
    }

    /**
     * Resets and loads all block values.
     */
    private void loadBlockWorth() {
        blockWorth = new LinkedHashMap<>();
        FileConfiguration config = main.getConfigManager().getBlocksConfig();
        for (String key : config.getConfigurationSection("").getKeys(false)) {
            try {
                Material material = Material.getMaterial(key);
                if (material == null || !material.isBlock() || !material.isSolid()) {
                    continue;
                }
                blockWorth.put(key.toUpperCase(), config.getDouble(key));
            } catch (Exception e) {
                LogManager.warn(e.getMessage());
            }
        }
    }

    /**
     * Resets and loads all spawner values.
     */
    private void loadSpawnerWorth() {
        spawnerWorth = new LinkedHashMap<>();
        FileConfiguration config = main.getConfigManager().getSpawnersConfig();
        for (String key : config.getConfigurationSection("").getKeys(false)) {
            try {
                EntityType entityType = EntityType.fromName(key);
                if (entityType == null) {
                    continue;
                }
                spawnerWorth.put(key.toUpperCase(), config.getDouble(key));
            } catch (Exception e) {
                LogManager.warn(e.getMessage());
            }
        }
    }

    /**
     * Resets and loads all container values.
     */
    private void loadContainerWorth() {
        containerWorth = new LinkedHashMap<>();
        FileConfiguration config = main.getConfigManager().getContainersConfig();
        for (String key : config.getConfigurationSection("").getKeys(false)) {
            try {
                Material material = Material.getMaterial(key);
                if (material == null) {
                    continue;
                }
                containerWorth.put(key.toUpperCase(), config.getDouble(key));
            } catch (Exception e) {
                LogManager.warn(e.getMessage());
            }
        }
    }

    /**
     * Creates holders for stats.
     *
     * @param id key to identify task
     */
    public void createHolder(int id) {
        blockConsumer.createHolder(id);
        spawnerConsumer.createHolder(id);
        containerConsumer.createHolder(id);
    }

    /**
     * Cleans up holders after stats update.
     *
     * @param id key to identify task
     */
    public void doCleanUp(int id) {
        blockConsumer.doCleanUp(id);
        spawnerConsumer.doCleanUp(id);
        containerConsumer.doCleanUp(id);
    }

    /**
     * Gets worth of a claim with possible inclusion of search for spawners/containers.
     *
     * @param id key to identify task
     * @param maxX max x coordinate
     * @param minX min x coordinate
     * @param maxY max y coordinate
     * @param minY min y coordinate
     * @param maxZ max z coordinate
     * @param minZ min z coordinate
     * @param world world to search in
     */
    public void processEntityClaim(int id, double maxX, double minX, double maxY, double minY,
            double maxZ, double minZ, World world) {

        for (int i = (int) minX; i < maxX; i++) {
            for (int j = (int) minZ; j < maxZ; j++) {
                if (stopOperations) {
                    return;
                }
                for (int k = (int) minY; k < maxY; k++) {
                    Block block = world.getBlockAt(i, k, j);
                    for (BiFunction<Integer, Block, Boolean> f : landOperations) {
                        if (f.apply(id, block)) {
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets worth of a claim with possible inclusion of search for spawners/containers.
     * Only plugins that claim lands in chunks uses this which is faster.
     *
     * @param id key to identify task
     * @param chunk chunk to get worth for.
     * @param world world to search in
     */
    public void processEntityChunk(int id, Chunk chunk, World world) {

        int x = chunk.getX() << 4;
        int z = chunk.getZ() << 4;
        int maxHeight = (int) main.getOptions().getMaxLandHeight();
        int minHeight = (int) main.getOptions().getMinLandHeight();
        for (int i = x; i < x + 16; ++i) {
            for (int j = z; j < z + 16; ++j) {
                if (stopOperations) {
                    return;
                }
                for (int k = minHeight; k < maxHeight; ++k) {
                    Block block = world.getBlockAt(i, k, j);
                    for (BiFunction<Integer, Block, Boolean> f : landOperations) {
                        if (f.apply(id, block)) {
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets the map of worth for all blocks.
     *
     * @return map of block material to value
     */
    public LinkedHashMap<String, Double> getBlockWorth() {
        return this.blockWorth;
    }

    /**
     * Gets the worth of a block.
     *
     * @param material material of block
     *
     * @return double representing its worth
     */
    public double getBlockWorth(String material) {
        Double value = this.blockWorth.get(material);
        if (value == null) {
            return 0;
        }
        return value;
    }

    /**
     * Gets the map of worth for all spawners.
     *
     * @return map of spawner entity type to value
     */
    public LinkedHashMap<String, Double> getSpawnerWorth() {
        return this.spawnerWorth;
    }

    /**
     * Gets the worth of a spawner.
     *
     * @param entityType entity type of spawner
     *
     * @return double representing its worth
     */
    public double getSpawnerWorth(String entityType) {
        Double value = this.spawnerWorth.get(entityType);
        if (value == null) {
            return 0;
        }
        return value;
    }

    /**
     * Gets the map of worth for all container items.
     *
     * @return map of container item material to value
     */
    public LinkedHashMap<String, Double> getContainerWorth() {
        return this.containerWorth;
    }

    /**
     * Gets the worth of a container item.
     *
     * @param material material of container item
     *
     * @return double representing its worth
     */
    public double getContainerWorth(String material) {
        Double value = this.containerWorth.get(material);
        if (value == null) {
            return 0;
        }
        return value;
    }

    /**
     * Gets the blocks to show sender in GUI.
     *
     * @param id key to identify task
     *
     * @return hashmap of block material to its worth
     */
    public HashMap<String, MutableInt> getBlocksForGui(int id) {
        return blockConsumer.getBlockHolder(id).getCounter();
    }

    /**
     * Gets the spawners to show sender in GUI.
     *
     * @param id key to identify task
     *
     * @return hashmap of spawner entity type to its worth
     */
    public HashMap<String, MutableInt> getSpawnersForGui(int id) {
        return spawnerConsumer.getSpawnerHolder(id).getCounter();
    }

    /**
     * Gets the container items to show sender in GUI.
     *
     * @param id key to identify task
     *
     * @return hashmap of container item material to its worth
     */
    public HashMap<String, MutableInt> getContainersForGui(int id) {
        return containerConsumer.getContainerHolder(id).getCounter();
    }


    /**
     * Processes spawner types on the main thread for stats.
     *
     * @param id key to identify task
     */
    public void processSpawnerTypes(int id) {
        spawnerConsumer.processSpawnerTypes(id);
    }

    /**
     * Processes container items on the main thread for stats.
     *
     * @param id key to identify task
     */
    public void processContainerItems(int id) {
        containerConsumer.processContainerItems(id);
    }

    /**
     * Calculates block worth for a specified entity.
     *
     * @param id key to identify task
     *
     * @return map of sender uuid to the calculated block worth
     */
    public double calculateBlockWorth(int id) {
        return blockConsumer.calculateBlockWorth(id);
    }

    /**
     * Calculates spawner worth for a specified entity.
     *
     * @param id key to identify task
     *
     * @return map of sender uuid to the calculated spawner worth
     */
    public double calculateSpawnerWorth(int id) {
        return spawnerConsumer.calculateSpawnerWorth(id);
    }

    /**
     * Calculates container worth for a specified entity.
     *
     * @param id key to identify task
     *
     * @return map of sender uuid to the calculated container worth
     */
    public double calculateContainerWorth(int id) {
        return containerConsumer.calculateContainerWorth(id);
    }

    /**
     * Gets the state for operations to stop or continue. Mainly used for spawner and container
     * operations.
     *
     * @return state of operations
     */
    public boolean getStopOperations() {
        return stopOperations;
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
