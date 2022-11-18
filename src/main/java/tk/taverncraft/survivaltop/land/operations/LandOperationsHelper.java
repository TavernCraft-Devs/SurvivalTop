package tk.taverncraft.survivaltop.land.operations;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.BiFunction;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.logs.LogManager;
import tk.taverncraft.survivaltop.utils.types.MutableInt;

/**
 * Helper function for loading the logic of calculations.
 */
public class LandOperationsHelper {
    private final Main main;
    private BlockOperations blockOperations;
    private SpawnerOperations spawnerOperations;
    private ContainerOperations containerOperations;

    // boolean to allow reloads to stop current operations
    private boolean stopOperations = false;

    // list of operations to perform for calculating land wealth
    // leaderboard and stats uses different calculations because of gui setups and
    // different tracking mechanisms (i.e. for stats, inventory needs to be shown to sender)
    ArrayList<BiFunction<UUID, Block, Boolean>> landOperationsForLeaderboard = new ArrayList<>();
    ArrayList<BiFunction<UUID, Block, Boolean>> landOperationsForStats = new ArrayList<>();

    // worth of blocks, spawners and containers
    private LinkedHashMap<String, Double> blockWorth = new LinkedHashMap<>();
    private LinkedHashMap<String, Double> spawnerWorth = new LinkedHashMap<>();
    private LinkedHashMap<String, Double> containerWorth = new LinkedHashMap<>();

    /**
     * Constructor for LandOperationsHelper.
     *
     * @param main plugin class
     */
    public LandOperationsHelper(Main main) {
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
        blockOperations = new BlockOperations(blockWorth);
        spawnerOperations = new SpawnerOperations(main, this, spawnerWorth);
        containerOperations = new ContainerOperations(main, this, containerWorth);
    }

    /**
     * Initializes land operations to be included.
     */
    private void initializeLandOperations() {
        landOperationsForLeaderboard = new ArrayList<>();
        landOperationsForStats = new ArrayList<>();

        if (main.getOptions().landIsIncluded()) {
            landOperationsForLeaderboard.add(blockOperations.getLeaderboardOperation());
            landOperationsForStats.add(blockOperations.getStatsOperation());
        } else {
            return;
        }
        if (main.getOptions().spawnerIsIncluded()) {
            landOperationsForLeaderboard.add(spawnerOperations.getLeaderboardOperation());
            landOperationsForStats.add(spawnerOperations.getStatsOperation());
        }

        if (main.getOptions().containerIsIncluded()) {
            landOperationsForLeaderboard.add(containerOperations.getLeaderboardOperation());
            landOperationsForStats.add(containerOperations.getStatsOperation());
        }
    }

    /**
     * Resets and loads all block values.
     */
    private void loadBlockWorth() {
        blockWorth = new LinkedHashMap<>();
        for (String key : main.getBlocksConfig().getConfigurationSection("")
                .getKeys(false)) {
            try {
                Material material = Material.getMaterial(key);
                if (material == null || !material.isBlock() || !material.isSolid()) {
                    continue;
                }
                blockWorth.put(key.toUpperCase(), main.getBlocksConfig().getDouble(key));
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
        for (String key : main.getSpawnersConfig().getConfigurationSection("")
                .getKeys(false)) {
            try {
                EntityType entityType = EntityType.fromName(key);
                if (entityType == null) {
                    continue;
                }
                spawnerWorth.put(key.toUpperCase(), main.getSpawnersConfig().getDouble(key));
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
        for (String key : main.getContainersConfig().getConfigurationSection("")
                .getKeys(false)) {
            try {
                Material material = Material.getMaterial(key);
                if (material == null) {
                    continue;
                }
                containerWorth.put(key.toUpperCase(), main.getContainersConfig().getDouble(key));
            } catch (Exception e) {
                LogManager.warn(e.getMessage());
            }
        }
    }

    /**
     * Cleans up holders after leaderboard update.
     */
    public void doCleanUpForLeaderboard() {
        blockOperations.doCleanUpForLeaderboard();
        spawnerOperations.doCleanUpForLeaderboard();
        containerOperations.doCleanUpForLeaderboard();
    }

    /**
     * Cleans up holders after stats update.
     *
     * @param uuid uuid of sender
     */
    public void doCleanUpForStats(UUID uuid) {
        blockOperations.doCleanUpForStats(uuid);
        spawnerOperations.doCleanUpForStats(uuid);
        containerOperations.doCleanUpForStats(uuid);
    }

    /**
     * Creates holders for leaderboard.
     *
     * @param uuid uuid of each entities
     */
    public void createHoldersForLeaderboard(UUID uuid) {
        blockOperations.createHolderForLeaderboard(uuid);
        spawnerOperations.createHolderForLeaderboard(uuid);
        containerOperations.createHolderForLeaderboard(uuid);
    }

    /**
     * Creates holders for stats.
     *
     * @param uuid uuid of sender, not to confused with the entity itself!
     */
    public void createHoldersForStats(UUID uuid) {
        blockOperations.createHolderForStats(uuid);
        spawnerOperations.createHolderForStats(uuid);
        containerOperations.createHolderForStats(uuid);
    }

    /**
     * Gets worth of a claim with possible inclusion of search for spawners/containers.
     *
     * @param uuid uuid of sender if this is run through stats command; otherwise entities
     * @param maxX max x coordinate
     * @param minX min x coordinate
     * @param maxY max y coordinate
     * @param minY min y coordinate
     * @param maxZ max z coordinate
     * @param minZ min z coordinate
     * @param world world to search in
     * @param isLeaderboardUpdate true if is a leaderboard update, false otherwise (i.e. stats)
     */
    public void processEntityClaim(UUID uuid, double maxX, double minX, double maxY, double minY,
                                   double maxZ, double minZ, World world, boolean isLeaderboardUpdate) {
        ArrayList<BiFunction<UUID, Block, Boolean>> blockOperations;
        if (isLeaderboardUpdate) {
            blockOperations = landOperationsForLeaderboard;
        } else {
            blockOperations = landOperationsForStats;
        }

        for (int i = (int) minX; i < maxX; i++) {
            for (int j = (int) minY; j < maxY; j++) {
                for (int k = (int) minZ; k < maxZ; k++) {
                    if (stopOperations) {
                        return;
                    }
                    Block block = world.getBlockAt(i, j, k);
                    for (BiFunction<UUID, Block, Boolean> f : blockOperations) {
                        if (f.apply(uuid, block)) {
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
     * @param uuid uuid of sender if this is run through stats command; otherwise entities
     * @param chunk chunk to get worth for.
     * @param world world to search in
     * @param isLeaderboardUpdate true if is a leaderboard update, false otherwise (i.e. stats)
     */
    public void processEntityChunk(UUID uuid, Chunk chunk, World world, boolean isLeaderboardUpdate) {
        ArrayList<BiFunction<UUID, Block, Boolean>> blockOperations;
        if (isLeaderboardUpdate) {
            blockOperations = landOperationsForLeaderboard;
        } else {
            blockOperations = landOperationsForStats;
        }

        int x = chunk.getX() << 4;
        int z = chunk.getZ() << 4;
        int maxHeight = (int) main.getOptions().getMaxLandHeight();
        int minHeight = (int) main.getOptions().getMinLandHeight();
        for (int i = x; i < x + 16; ++i) {
            for (int j = z; j < z + 16; ++j) {
                for (int k = minHeight; k < maxHeight; ++k) {
                    if (stopOperations) {
                        return;
                    }
                    Block block = world.getBlockAt(i, k, j);
                    for (BiFunction<UUID, Block, Boolean> f : blockOperations) {
                        if (f.apply(uuid, block)) {
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
     * @return hashmap of block material to its worth
     */
    public HashMap<String, MutableInt> getBlocksForGuiStats(UUID uuid) {
        return blockOperations.getBlockHolderForStats(uuid).getCounter();
    }

    /**
     * Gets the spawners to show sender in GUI.
     *
     * @return hashmap of spawner entity type to its worth
     */
    public HashMap<String, MutableInt> getSpawnersForGuiStats(UUID uuid) {
        return spawnerOperations.getSpawnerHolderForStats(uuid).getCounter();
    }

    /**
     * Gets the container items to show sender in GUI.
     *
     * @return hashmap of container item material to its worth
     */
    public HashMap<String, MutableInt> getContainersForGuiStats(UUID uuid) {
        return containerOperations.getContainerHolderForStats(uuid).getCounter();
    }

    /**
     * Processes spawner types on the main thread for leaderboard.
     */
    public void processSpawnerTypesForLeaderboard() {
        spawnerOperations.processSpawnerTypesForLeaderboard();
    }

    /**
     * Processes spawner types on the main thread for stats.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     */
    public void processSpawnerTypesForStats(UUID uuid) {
        spawnerOperations.processSpawnerTypesForStats(uuid);
    }

    /**
     * Processes container items on the main thread for leaderboard,
     */
    public void processContainerItemsForLeaderboard() {
        containerOperations.processContainerItemsForLeaderboard();
    }

    /**
     * Processes container items on the main thread for stats.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     */
    public void processContainerItemsForStats(UUID uuid) {
        containerOperations.processContainerItemsForStats(uuid);
    }

    /**
     * Calculates block worth for all entities.
     *
     * @return map of entities uuid to their block worth
     */
    public HashMap<UUID, Double> calculateBlockWorthForLeaderboard() {
        return blockOperations.calculateBlockWorthForLeaderboard();
    }

    /**
     * Calculates block worth for a specified entity.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     *
     * @return map of sender uuid to the calculated block worth
     */
    public double calculateBlockWorthForStats(UUID uuid) {
        return blockOperations.calculateBlockWorthForStats(uuid);
    }

    /**
     * Calculates spawner worth for all entities.
     *
     * @return map of entities uuid to their spawner worth
     */
    public HashMap<UUID, Double> calculateSpawnerWorthForLeaderboard() {
        return spawnerOperations.calculateSpawnerWorthForLeaderboard();
    }

    /**
     * Calculates spawner worth for a specified entity.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     *
     * @return map of sender uuid to the calculated spawner worth
     */
    public double calculateSpawnerWorthForStats(UUID uuid) {
        return spawnerOperations.calculateSpawnerWorthForStats(uuid);
    }

    /**
     * Calculates container worth for all entities.
     *
     * @return map of entities uuid to their container worth
     */
    public HashMap<UUID, Double> calculateContainerWorthForLeaderboard() {
        return containerOperations.calculateContainerWorthForLeaderboard();
    }

    /**
     * Calculates container worth for a specified entity.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     *
     * @return map of sender uuid to the calculated container worth
     */
    public double calculateContainerWorthForStats(UUID uuid) {
        return containerOperations.calculateContainerWorthForStats(uuid);
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
