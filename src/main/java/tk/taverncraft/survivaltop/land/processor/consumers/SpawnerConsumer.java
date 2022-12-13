package tk.taverncraft.survivaltop.land.processor.consumers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;

import dev.rosewood.rosestacker.api.RoseStackerAPI;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.land.processor.LandProcessor;
import tk.taverncraft.survivaltop.land.processor.holders.SpawnerHolder;
import tk.taverncraft.survivaltop.utils.types.MutableInt;

/**
 * Handles the logic for performing spawner operations when scanning locations.
 */
public class SpawnerConsumer {
    private final Main main;
    private final LandProcessor landProcessor;
    private final LinkedHashMap<String, Double> spawnerWorth;
    private final Set<String> spawnerEntityType;
    private RoseStackerAPI rApi;

    // holders containing count of each material mapped to uuid
    private final ConcurrentHashMap<Integer, SpawnerHolder> spawnerHolderMap =
            new ConcurrentHashMap<>();

    // populated from main thread and processed on async thread later
    private final ConcurrentHashMap<Integer, ArrayList<Block>> preprocessedSpawners =
            new ConcurrentHashMap<>();

    /**
     * Constructor for SpawnerConsumer.
     *
     * @param main plugin class
     * @param landProcessor helper for land operations
     * @param spawnerWorth map of spawner names to their values
     */
    public SpawnerConsumer(Main main, LandProcessor landProcessor,
                           LinkedHashMap<String, Double> spawnerWorth) {
        this.main = main;
        this.landProcessor = landProcessor;
        this.spawnerWorth = spawnerWorth;
        this.spawnerEntityType = spawnerWorth.keySet();
        if (main.getDependencyManager().hasDependencyLoaded("RoseStacker")) {
            rApi = RoseStackerAPI.getInstance();
        }
    }

    /**
     * Returns spawner operation for stats.
     *
     * @return spawner operation for stats
     */
    public BiFunction<Integer, Block, Boolean> getOperation() {
        if (main.getDependencyManager().hasDependencyLoaded("RoseStacker")) {
            return preprocessRoseStackers;
        }
        return preprocessSpawner;
    }

    /**
     * Creates holders for stats.
     *
     * @param id key to identify task
     */
    public void createHolder(int id) {
        spawnerHolderMap.put(id, new SpawnerHolder(spawnerEntityType));

        // temp array list for tracking containers
        preprocessedSpawners.put(id, new ArrayList<>());
    }

    /**
     * Returns spawner holder for given uuid.
     *
     * @param id key to identify task
     *
     * @return spawner holder for given uuid
     */
    public SpawnerHolder getSpawnerHolder(int id) {
        return spawnerHolderMap.get(id);
    }

    /**
     * Cleans up holders and preprocessed spawners after stats update.
     *
     * @param id key to identify task
     */
    public void doCleanUp(int id) {
        spawnerHolderMap.remove(id);
        preprocessedSpawners.remove(id);
    }

    /**
     * Calculates spawner worth for a specified entity.
     *
     * @param id key to identify task
     *
     * @return map of sender uuid to the calculated spawner worth
     */
    public double calculateSpawnerWorth(int id) {
        return getAllSpawnersWorth(spawnerHolderMap.get(id));
    }

    /**
     * Gets the total worth of spawners.
     *
     * @param spawnerHolder holder containing spawner count
     *
     * @return double value representing total worth of spawners
     */
    public double getAllSpawnersWorth(SpawnerHolder spawnerHolder) {
        double totalSpawnerWorth = 0;
        if (spawnerHolder == null) {
            return 0;
        }
        HashMap<String, MutableInt> counter = spawnerHolder.getCounter();
        for (Map.Entry<String, MutableInt> map : counter.entrySet()) {
            // count multiply by worth, then added to total
            totalSpawnerWorth += map.getValue().get() * spawnerWorth.get(map.getKey());
        }
        return totalSpawnerWorth;
    }

    /**
     * Processes the worth of spawners.
     *
     * @param id key to identify task
     */
    public void processSpawnerTypes(int id) {
        ArrayList<Block> blocks = preprocessedSpawners.get(id);
        int numBlocks = blocks.size();
        for (int i = 0; i < numBlocks; i++) {
            if (landProcessor.getStopOperations()) {
                return;
            }
            Block block = blocks.get(i);
            try {
                CreatureSpawner spawner = (CreatureSpawner) block.getState();
                String mobType = spawner.getSpawnedType().name();
                if (spawnerEntityType.contains(mobType)) {
                    spawnerHolderMap.get(id).addToHolder(mobType);
                }
            } catch (ClassCastException e) {
                // error thrown if player breaks spawner just as calculation is taking place
            }
        }
    }

    /**
     * Preprocesses spawners to be handled on main thread later for when the stats command is
     * executed. Uuid here belongs to the sender and comes with the block that is being checked.
     * This always returns 0 since if a block is not a spawner (ignored) and if it is
     * a spawner, then it is set to be processed later anyways
     */
    private final BiFunction<Integer, Block, Boolean> preprocessSpawner = (id, block) -> {
        Material material = block.getType();
        if (material.equals(Material.SPAWNER)) {
            preprocessedSpawners.get(id).add(block);
            return true;
        }
        return false;
    };

    /**
     * Variation of preprocessSpawner for RoseStacker support.
     */
    private final BiFunction<Integer, Block, Boolean> preprocessRoseStackers = (id, block) -> {
        Material material = block.getType();
        if (material.equals(Material.SPAWNER)) {
            if (rApi.isSpawnerStacked(block)) {
                int stackSize = rApi.getStackedSpawner(block).getStackSize();
                for (int i = 0; i < stackSize; i++) {
                    preprocessedSpawners.get(id).add(block);
                }
            } else {
                preprocessedSpawners.get(id).add(block);
            }
            return true;
        }
        return false;
    };
}
