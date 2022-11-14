package tk.taverncraft.survivaltop.land.operations;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

import org.bukkit.Material;
import org.bukkit.block.Block;

import tk.taverncraft.survivaltop.land.operations.holders.BlockHolder;

/**
 * Handles the logic for performing block operations when scanning locations.
 */
public class BlockOperations {
    private LinkedHashMap<Material, Double> blockWorth;

    // holders containing count of each material mapped to uuid
    private HashMap<UUID, BlockHolder> blockHolderMapForLeaderboard = new HashMap<>();
    private HashMap<UUID, BlockHolder> blockHolderMapForStats = new HashMap<>();

    /**
     * Constructor for BlockOperations.
     *
     * @param blockWorth map of block names to their values
     */
    public BlockOperations(LinkedHashMap<Material, Double> blockWorth) {
        this.blockWorth = blockWorth;
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
     * @return block operation for stats.
     */
    public BiFunction<UUID, Block, Boolean> getStatsOperation() {
        return processBlockForStats;
    }

    /**
     * Creates holders for leaderboard.
     *
     * @param uuid uuid of each entities
     */
    public void createHolderForLeaderboard(UUID uuid) {
        blockHolderMapForLeaderboard.put(uuid, new BlockHolder(blockWorth.keySet()));
    }

    /**
     * Creates holders for stats.
     *
     * @param uuid uuid of sender, not to confused with the entity itself!
     */
    public void createHolderForStats(UUID uuid) {
        blockHolderMapForStats.put(uuid, new BlockHolder(blockWorth.keySet()));
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
     * Process the worth of blocks.
     *
     * @param blockHolder holder containing block count
     *
     * @return double value representing total worth of blocks
     */
    public double getAllBlocksWorth(BlockHolder blockHolder) {
        double totalBlockWorth = 0;
        HashMap<Material, Integer> counter = blockHolder.getCounter();
        for (Map.Entry<Material, Integer> map : counter.entrySet()) {
            // count multiply by worth, then added to total
            totalBlockWorth += map.getValue() * blockWorth.get(map.getKey());
        }
        return totalBlockWorth;
    }

    /**
     * Process blocks immediately (and asynchronously).
     */
    private BiFunction<UUID, Block, Boolean> processBlockForLeaderboard = (uuid, block) -> {
        Material material = block.getType();
        if (blockWorth.containsKey(material)) {
            blockHolderMapForLeaderboard.get(uuid).addToHolder(material);
            return true;
        }
        return false;
    };

    /**
     * Process blocks immediately (and asynchronously). Check if gui is enabled and prepare
     * them for gui view if so.
     */
    private BiFunction<UUID, Block, Boolean> processBlockForStats = (uuid, block) -> {
        Material material = block.getType();
        if (blockWorth.containsKey(material)) {
            blockHolderMapForStats.get(uuid).addToHolder(material);
            return true;
        }
        return false;
    };
}
