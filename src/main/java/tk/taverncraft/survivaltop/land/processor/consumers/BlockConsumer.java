package tk.taverncraft.survivaltop.land.processor.consumers;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import org.bukkit.block.Block;

import tk.taverncraft.survivaltop.land.processor.holders.BlockHolder;
import tk.taverncraft.survivaltop.utils.types.MutableInt;

/**
 * Handles the logic for performing block operations when scanning locations.
 */
public class BlockConsumer {
    private final LinkedHashMap<String, Double> blockWorth;
    private Set<String> blockMaterial;

    // holders containing count of each material mapped to uuid
    private final ConcurrentHashMap<Integer, BlockHolder> blockHolderMap = new ConcurrentHashMap<>();

    /**
     * Constructor for BlockConsumer.
     *
     * @param blockWorth map of block materials to their values
     */
    public BlockConsumer(LinkedHashMap<String, Double> blockWorth) {
        this.blockWorth = blockWorth;
        this.blockMaterial = blockWorth.keySet();
    }

    /**
     * Returns block operation for stats.
     *
     * @return block operation for stats
     */
    public BiFunction<Integer, Block, Boolean> getOperation() {
        return processBlock;
    }

    /**
     * Creates holders for stats.
     *
     * @param id key to identify task
     */
    public void createHolder(int id) {
        blockHolderMap.put(id, new BlockHolder(blockMaterial));
    }

    /**
     * Returns block holder for given uuid.
     *
     * @param id key to identify task
     *
     * @return block holder for given uuid
     */
    public BlockHolder getBlockHolder(int id) {
        return blockHolderMap.get(id);
    }

    /**
     * Cleans up holders after stats update.
     *
     * @param id key to identify task
     */
    public void doCleanUp(int id) {
        blockHolderMap.remove(id);
    }

    /**
     * Calculates block worth for a specified entity.
     *
     * @param id key to identify task
     *
     * @return map of sender uuid to the calculated block worth
     */
    public double calculateBlockWorth(int id) {
        return getAllBlocksWorth(blockHolderMap.get(id));
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
        if (blockHolder == null) {
            return 0;
        }
        HashMap<String, MutableInt> counter = blockHolder.getCounter();
        for (Map.Entry<String, MutableInt> map : counter.entrySet()) {
            // count multiply by worth, then added to total
            totalBlockWorth += map.getValue().get() * blockWorth.get(map.getKey());
        }
        return totalBlockWorth;
    }

    /**
     * Processes blocks immediately (and asynchronously) for stats.
     */
    private final BiFunction<Integer, Block, Boolean> processBlock = (id, block) -> {
        String material = block.getType().name();
        if (blockMaterial.contains(material)) {
            blockHolderMap.get(id).addToHolder(material);
            return true;
        }
        return false;
    };
}
