package tk.taverncraft.survivaltop.land.operations;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import org.bukkit.block.Block;

import tk.taverncraft.survivaltop.land.operations.holders.BlockHolder;
import tk.taverncraft.survivaltop.utils.MutableInt;

/**
 * Handles the logic for performing block operations when scanning locations.
 */
public class BlockOperations {
    private final LinkedHashMap<String, Double> blockWorth;
    private Set<String> blockMaterial;

    // holders containing count of each material mapped to uuid
    private HashMap<UUID, BlockHolder> blockHolderMapForLeaderboard = new HashMap<>();
    private final ConcurrentHashMap<UUID, BlockHolder> blockHolderMapForStats = new ConcurrentHashMap<>();

    /**
     * Constructor for BlockOperations.
     *
     * @param blockWorth map of block materials to their values
     */
    public BlockOperations(LinkedHashMap<String, Double> blockWorth) {
        this.blockWorth = blockWorth;
        this.blockMaterial = blockWorth.keySet();
    }

    /**
     * Returns block holder for given uuid.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     *
     * @return block holder for given uuid
     */
    public BlockHolder getBlockHolderForStats(UUID uuid) {
        return blockHolderMapForStats.get(uuid);
    }

    /**
     * Returns block operation for leaderboard.
     *
     * @return block operation for leaderboard
     */
    public BiFunction<UUID, Block, Boolean> getLeaderboardOperation() {
        return processBlockForLeaderboard;
    }

    /**
     * Returns block operation for stats.
     *
     * @return block operation for stats
     */
    public BiFunction<UUID, Block, Boolean> getStatsOperation() {
        return processBlockForStats;
    }

    /**
     * Cleans up holders after leaderboard update.
     */
    public void doCleanUpForLeaderboard() {
        blockHolderMapForLeaderboard = new HashMap<>();
    }

    /**
     * Cleans up holders after stats update.
     *
     * @param uuid uuid of sender
     */
    public void doCleanUpForStats(UUID uuid) {
        blockHolderMapForStats.remove(uuid);
    }

    /**
     * Creates holders for leaderboard.
     *
     * @param uuid uuid of each entities
     */
    public void createHolderForLeaderboard(UUID uuid) {
        blockHolderMapForLeaderboard.put(uuid, new BlockHolder(blockMaterial));
    }

    /**
     * Creates holders for stats.
     *
     * @param uuid uuid of sender, not to confused with the entity itself!
     */
    public void createHolderForStats(UUID uuid) {
        blockHolderMapForStats.put(uuid, new BlockHolder(blockMaterial));
    }

    /**
     * Calculates block worth for all entities.
     *
     * @return map of entities uuid to their block worth
     */
    public HashMap<UUID, Double> calculateBlockWorthForLeaderboard() {
        HashMap<UUID, Double> blockWorthMap = new HashMap<>();
        for (Map.Entry<UUID, BlockHolder> map : blockHolderMapForLeaderboard.entrySet()) {
            double value = getAllBlocksWorth(map.getValue());
            blockWorthMap.put(map.getKey(), value);
        }
        return blockWorthMap;
    }

    /**
     * Calculates block worth for a specified entity.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     *
     * @return map of sender uuid to the calculated block worth
     */
    public double calculateBlockWorthForStats(UUID uuid) {
        return getAllBlocksWorth(blockHolderMapForStats.get(uuid));
    }

    /**
     * Gets the total worth of blocks.
     *
     * @param blockHolder holder containing block count
     *
     * @return double value representing total worth of blocks
     */
    public double getAllBlocksWorth(BlockHolder blockHolder) {
        double totalBlockWorth = 0;
        HashMap<String, MutableInt> counter = blockHolder.getCounter();
        for (Map.Entry<String, MutableInt> map : counter.entrySet()) {
            // count multiply by worth, then added to total
            totalBlockWorth += map.getValue().get() * blockWorth.get(map.getKey());
        }
        return totalBlockWorth;
    }

    /**
     * Processes blocks immediately (and asynchronously) for leaderboard.
     */
    private final BiFunction<UUID, Block, Boolean> processBlockForLeaderboard = (uuid, block) -> {
        String material = block.getType().name();
        if (blockMaterial.contains(material)) {
            blockHolderMapForLeaderboard.get(uuid).addToHolder(material);
            return true;
        }
        return false;
    };

    /**
     * Processes blocks immediately (and asynchronously) for stats.
     */
    private final BiFunction<UUID, Block, Boolean> processBlockForStats = (uuid, block) -> {
        String material = block.getType().name();
        if (blockMaterial.contains(material)) {
            blockHolderMapForStats.get(uuid).addToHolder(material);
            return true;
        }
        return false;
    };
}
