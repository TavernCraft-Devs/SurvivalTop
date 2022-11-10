package tk.taverncraft.survivaltop.land.operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

import dev.rosewood.rosestacker.api.RoseStackerAPI;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

import tk.taverncraft.survivaltop.Main;

/**
 * Handles the logic for performing spawner operations when scanning locations.
 */
public class SpawnerOperations {
    private Main main;
    private LinkedHashMap<String, Double> spawnerWorth;
    private RoseStackerAPI rApi;

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
        if (main.getDependencyManager().hasDependencyLoaded("RoseStacker")) {
            rApi = RoseStackerAPI.getInstance();
        }
    }

    public void doLeaderboardCleanup() {
        preprocessedSpawnersForLeaderboard = new HashMap<>();
    }

    public void doStatsCleanup(UUID uuid) {
        preprocessedSpawnersForStats.remove(uuid);
    }

    /**
     * Returns spawner operation for leaderboard.
     *
     * @return spawner operation for leaderboard
     */
    public BiFunction<UUID, Block, Double> getLeaderboardOperation() {
        if (main.getDependencyManager().hasDependencyLoaded("RoseStacker")) {
            return preprocessRoseStackerForLeaderboard;
        }
        return preprocessSpawnerForLeaderboard;
    }

    /**
     * Returns spawner operation for stats.
     *
     * @return spawner operation for stats.
     */
    public BiFunction<UUID, Block, Double> getStatsOperation() {
        if (main.getDependencyManager().hasDependencyLoaded("RoseStacker")) {
            return preprocessRoseStackerForStats;
        }
        return preprocessSpawnerForStats;
    }

    /**
     * Calculates spawner worth for all entities.
     *
     * @return map of entities uuid to their spawner worth
     */
    public HashMap<UUID, Double> calculateSpawnerWorthForLeaderboard() {
        HashMap<UUID, Double> tempSpawnerCache = new HashMap<>();
        for (Map.Entry<UUID, ArrayList<Block>> map : preprocessedSpawnersForLeaderboard.entrySet()) {
            UUID uuid = map.getKey();
            ArrayList<Block> blocks = map.getValue();
            double value = processSpawnerWorth(blocks, uuid, false);
            tempSpawnerCache.put(map.getKey(), value);
        }
        return tempSpawnerCache;
    }

    /**
     * Calculates spawner worth for a specified entity.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     *
     * @return map of sender uuid to the calculated spawner worth
     */
    public double calculateSpawnerWorthForStats(UUID uuid) {
        ArrayList<Block> blocks = preprocessedSpawnersForStats.get(uuid);
        if (blocks == null) {
            return 0;
        }
        return processSpawnerWorth(blocks, uuid, true);
    }

    /**
     * Process the worth of spawners (for individuals which may need GUI).
     *
     * @param blocks list of spawner blocks
     *
     * @return double value representing total worth of spawners
     */
    public double processSpawnerWorth(ArrayList<Block> blocks, UUID uuid,
                                      boolean isStatsAction) {
        if (blocks == null) {
            return 0;
        }
        double totalSpawnerWorth = 0;
        for (Block block : blocks) {
            try {
                CreatureSpawner spawner = (CreatureSpawner) block.getState();
                EntityType mobType = spawner.getSpawnedType();
                String mobName = mobType.getName();
                if (mobName != null) {
                    String mobNameUpperCase = mobName.toUpperCase();
                    Double value = spawnerWorth.get(mobNameUpperCase);
                    if (value == null) {
                        value = 0.0;
                    } else if (main.isUseGuiStats() && isStatsAction) {
                        main.getEntityStatsManager().setSpawnersForGuiStats(uuid, mobNameUpperCase);
                    }
                    totalSpawnerWorth += value;
                }
            } catch (ClassCastException e) {
                // error thrown if player breaks spawner just as calculation is taking place
            }
        }
        return totalSpawnerWorth;
    }

    /**
     * Preprocess spawners to be handled on main thread later for when the leaderboard command is
     * being updated. Uuid here belongs to the sender and comes with the block that is being
     * checked. This always returns 0 since if a block is not a spawner (ignored) and if it is
     * a spawner, then it is set to be processed later anyways
     */
    private BiFunction<UUID, Block, Double> preprocessSpawnerForLeaderboard = (uuid, block) -> {
        Material material = block.getType();
        if (material.equals(Material.SPAWNER)) {
            preprocessedSpawnersForLeaderboard.computeIfAbsent(uuid, k -> new ArrayList<>());
            preprocessedSpawnersForLeaderboard.get(uuid).add(block);
        }
        return 0.0;
    };

    /**
     * Preprocess spawners to be handled on main thread later for when the stats command is
     * executed. Uuid here belongs to the sender and comes with the block that is being checked.
     * This always returns 0 since if a block is not a spawner (ignored) and if it is
     * a spawner, then it is set to be processed later anyways
     */
    private BiFunction<UUID, Block, Double> preprocessSpawnerForStats = (uuid, block) -> {
        Material material = block.getType();
        if (material.equals(Material.SPAWNER)) {
            preprocessedSpawnersForStats.computeIfAbsent(uuid, k -> new ArrayList<>());
            preprocessedSpawnersForStats.get(uuid).add(block);
        }
        return 0.0;
    };

    /**
     * Variation of preprocessSpawnerForLeaderboard for RoseStacker support.
     */
    private BiFunction<UUID, Block, Double> preprocessRoseStackerForLeaderboard = (uuid, block) -> {
        Material material = block.getType();
        if (material.equals(Material.SPAWNER)) {
            if (rApi.isSpawnerStacked(block)) {
                int stackSize = rApi.getStackedSpawner(block).getStackSize();
                for (int i = 0; i < stackSize; i++) {
                    preprocessedSpawnersForLeaderboard.computeIfAbsent(uuid, k -> new ArrayList<>());
                    preprocessedSpawnersForLeaderboard.get(uuid).add(block);
                }
            } else {
                preprocessedSpawnersForLeaderboard.computeIfAbsent(uuid, k -> new ArrayList<>());
                preprocessedSpawnersForLeaderboard.get(uuid).add(block);
            }
        }
        return 0.0;
    };

    /**
     * Variation of preprocessSpawnerForStats for RoseStacker support.
     */
    private BiFunction<UUID, Block, Double> preprocessRoseStackerForStats = (uuid, block) -> {
        Material material = block.getType();
        if (material.equals(Material.SPAWNER)) {
            if (rApi.isSpawnerStacked(block)) {
                int stackSize = rApi.getStackedSpawner(block).getStackSize();
                for (int i = 0; i < stackSize; i++) {
                    preprocessedSpawnersForStats.computeIfAbsent(uuid, k -> new ArrayList<>());
                    preprocessedSpawnersForStats.get(uuid).add(block);
                }
            } else {
                preprocessedSpawnersForStats.computeIfAbsent(uuid, k -> new ArrayList<>());
                preprocessedSpawnersForStats.get(uuid).add(block);
            }
        }
        return 0.0;
    };
}
