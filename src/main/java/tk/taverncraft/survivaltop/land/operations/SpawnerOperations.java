package tk.taverncraft.survivaltop.land.operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;

import dev.rosewood.rosestacker.api.RoseStackerAPI;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.land.operations.holders.SpawnerHolder;
import tk.taverncraft.survivaltop.utils.MutableInt;

/**
 * Handles the logic for performing spawner operations when scanning locations.
 */
public class SpawnerOperations {
    private Main main;
    private LinkedHashMap<String, Double> spawnerWorth;
    private Set<String> spawnerEntityType;
    private RoseStackerAPI rApi;

    // holders containing count of each material mapped to uuid
    private HashMap<UUID, SpawnerHolder> spawnerHolderMapForLeaderboard = new HashMap<>();
    private HashMap<UUID, SpawnerHolder> spawnerHolderMapForStats = new HashMap<>();

    // populated from main thread and processed on async thread later
    private HashMap<UUID, ArrayList<Block>> preprocessedSpawnersForLeaderboard = new HashMap<>();
    private HashMap<UUID, ArrayList<Block>> preprocessedSpawnersForStats = new HashMap<>();

    /**
     * Constructor for SpawnerOperations.
     *
     * @param main plugin class
     * @param spawnerWorth map of spawner names to their values
     */
    public SpawnerOperations(Main main, LinkedHashMap<String, Double> spawnerWorth) {
        this.main = main;
        this.spawnerWorth = spawnerWorth;
        this.spawnerEntityType = spawnerWorth.keySet();
        if (main.getDependencyManager().hasDependencyLoaded("RoseStacker")) {
            rApi = RoseStackerAPI.getInstance();
        }
    }

    /**
     * Returns spawner holder for given uuid.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     *
     * @return spawner holder for given uuid
     */
    public SpawnerHolder getSpawnerHolderForStats(UUID uuid) {
        return spawnerHolderMapForStats.get(uuid);
    }

    /**
     * Returns spawner operation for leaderboard.
     *
     * @return spawner operation for leaderboard
     */
    public BiFunction<UUID, Block, Boolean> getLeaderboardOperation() {
        if (main.getDependencyManager().hasDependencyLoaded("RoseStacker")) {
            return preprocessRoseStackerForLeaderboard;
        }
        return preprocessSpawnerForLeaderboard;
    }

    /**
     * Returns spawner operation for stats.
     *
     * @return spawner operation for stats
     */
    public BiFunction<UUID, Block, Boolean> getStatsOperation() {
        if (main.getDependencyManager().hasDependencyLoaded("RoseStacker")) {
            return preprocessRoseStackerForStats;
        }
        return preprocessSpawnerForStats;
    }

    /**
     * Cleans up holders and preprocessed spawners after leaderboard update.
     */
    public void doCleanUpForLeaderboard() {
        spawnerHolderMapForLeaderboard = new HashMap<>();
        preprocessedSpawnersForLeaderboard = new HashMap<>();
    }

    /**
     * Cleans up holders and preprocessed spawners after stats update.
     *
     * @param uuid uuid of sender
     */
    public void doCleanUpForStats(UUID uuid) {
        spawnerHolderMapForStats.remove(uuid);
        preprocessedSpawnersForStats.remove(uuid);
    }

    /**
     * Creates holders for leaderboard.
     *
     * @param uuid uuid of each entity
     */
    public void createHolderForLeaderboard(UUID uuid) {
        spawnerHolderMapForLeaderboard.put(uuid, new SpawnerHolder(spawnerEntityType));

        // temp array list for tracking containers
        preprocessedSpawnersForLeaderboard.put(uuid, new ArrayList<>());
    }

    /**
     * Creates holders for stats.
     *
     * @param uuid uuid of sender, not to confused with the entity itself!
     */
    public void createHolderForStats(UUID uuid) {
        spawnerHolderMapForStats.put(uuid, new SpawnerHolder(spawnerEntityType));

        // temp array list for tracking containers
        preprocessedSpawnersForStats.put(uuid, new ArrayList<>());
    }

    /**
     * Calculates spawner worth for all entities.
     *
     * @return map of entities uuid to their spawner worth
     */
    public HashMap<UUID, Double> calculateSpawnerWorthForLeaderboard() {
        HashMap<UUID, Double> spawnerWorthMap = new HashMap<>();
        for (Map.Entry<UUID, SpawnerHolder> map : spawnerHolderMapForLeaderboard.entrySet()) {
            double value = getAllSpawnersWorth(map.getValue());
            spawnerWorthMap.put(map.getKey(), value);
        }
        return spawnerWorthMap;
    }

    /**
     * Calculates spawner worth for a specified entity.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     *
     * @return map of sender uuid to the calculated spawner worth
     */
    public double calculateSpawnerWorthForStats(UUID uuid) {
        return getAllSpawnersWorth(spawnerHolderMapForStats.get(uuid));
    }

    /**
     * Process the worth of spawners.
     *
     * @param spawnerHolder holder containing spawner count
     *
     * @return double value representing total worth of spawners
     */
    public double getAllSpawnersWorth(SpawnerHolder spawnerHolder) {
        double totalSpawnerWorth = 0;
        HashMap<String, MutableInt> counter = spawnerHolder.getCounter();
        for (Map.Entry<String, MutableInt> map : counter.entrySet()) {
            // count multiply by worth, then added to total
            totalSpawnerWorth += map.getValue().get() * spawnerWorth.get(map.getKey());
        }
        return totalSpawnerWorth;
    }

    /**
     * Process the worth of spawners.
     */
    public void processSpawnerTypesForLeaderboard() {
        for (Map.Entry<UUID, ArrayList<Block>> map : preprocessedSpawnersForLeaderboard.entrySet()) {
            UUID uuid = map.getKey();
            ArrayList<Block> blocks = map.getValue();
            int numBlocks = blocks.size();
            for (int i = 0; i < numBlocks; i++) {
                Block block = blocks.get(i);
                try {
                    CreatureSpawner spawner = (CreatureSpawner) block.getState();
                    String mobType = spawner.getSpawnedType().name();
                    if (spawnerEntityType.contains(mobType)) {
                        spawnerHolderMapForLeaderboard.get(uuid).addToHolder(mobType);
                    }
                } catch (ClassCastException e) {
                    // error thrown if player breaks spawner just as calculation is taking place
                }
            }
        }
    }

    /**
     * Process the worth of spawners.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     *
     * @return double value representing total worth of spawners
     */
    public void processSpawnerTypesForStats(UUID uuid) {
        ArrayList<Block> blocks = preprocessedSpawnersForStats.get(uuid);
        int numBlocks = blocks.size();
        for (int i = 0; i < numBlocks; i++) {
            Block block = blocks.get(i);
            try {
                CreatureSpawner spawner = (CreatureSpawner) block.getState();
                String mobType = spawner.getSpawnedType().getName();
                if (spawnerEntityType.contains(mobType)) {
                    spawnerHolderMapForStats.get(uuid).addToHolder(mobType);
                }
            } catch (ClassCastException e) {
                // error thrown if player breaks spawner just as calculation is taking place
            }
        }
    }

    /**
     * Preprocess spawners to be handled on main thread later for when the leaderboard command is
     * being updated. Uuid here belongs to the sender and comes with the block that is being
     * checked. This always returns 0 since if a block is not a spawner (ignored) and if it is
     * a spawner, then it is set to be processed later anyways
     */
    private BiFunction<UUID, Block, Boolean> preprocessSpawnerForLeaderboard = (uuid, block) -> {
        Material material = block.getType();
        if (material.equals(Material.SPAWNER)) {
            preprocessedSpawnersForLeaderboard.get(uuid).add(block);
            return true;
        }
        return false;
    };

    /**
     * Preprocess spawners to be handled on main thread later for when the stats command is
     * executed. Uuid here belongs to the sender and comes with the block that is being checked.
     * This always returns 0 since if a block is not a spawner (ignored) and if it is
     * a spawner, then it is set to be processed later anyways
     */
    private BiFunction<UUID, Block, Boolean> preprocessSpawnerForStats = (uuid, block) -> {
        Material material = block.getType();
        if (material.equals(Material.SPAWNER)) {
            preprocessedSpawnersForStats.get(uuid).add(block);
            return true;
        }
        return false;
    };

    /**
     * Variation of preprocessSpawnerForLeaderboard for RoseStacker support.
     */
    private BiFunction<UUID, Block, Boolean> preprocessRoseStackerForLeaderboard = (uuid, block) -> {
        Material material = block.getType();
        if (material.equals(Material.SPAWNER)) {
            if (rApi.isSpawnerStacked(block)) {
                int stackSize = rApi.getStackedSpawner(block).getStackSize();
                for (int i = 0; i < stackSize; i++) {
                    preprocessedSpawnersForLeaderboard.get(uuid).add(block);
                }
            } else {
                preprocessedSpawnersForLeaderboard.get(uuid).add(block);
            }
            return true;
        }
        return false;
    };

    /**
     * Variation of preprocessSpawnerForStats for RoseStacker support.
     */
    private BiFunction<UUID, Block, Boolean> preprocessRoseStackerForStats = (uuid, block) -> {
        Material material = block.getType();
        if (material.equals(Material.SPAWNER)) {
            if (rApi.isSpawnerStacked(block)) {
                int stackSize = rApi.getStackedSpawner(block).getStackSize();
                for (int i = 0; i < stackSize; i++) {
                    preprocessedSpawnersForStats.get(uuid).add(block);
                }
            } else {
                preprocessedSpawnersForStats.get(uuid).add(block);
            }
            return true;
        }
        return false;
    };
}
