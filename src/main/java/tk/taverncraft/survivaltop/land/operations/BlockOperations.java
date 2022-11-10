package tk.taverncraft.survivaltop.land.operations;

import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.function.BiFunction;

import org.bukkit.block.Block;

import tk.taverncraft.survivaltop.Main;

/**
 * Handles the logic for performing block operations when scanning locations.
 */
public class BlockOperations {
    private Main main;
    private LinkedHashMap<String, Double> blockWorth;

    /**
     * Constructor for BlockOperations.
     *
     * @param main plugin class
     * @param blockWorth map of block names to their values
     */
    public BlockOperations(Main main, LinkedHashMap<String, Double> blockWorth) {
        this.main = main;
        this.blockWorth = blockWorth;
    }

    /**
     * Returns block operation for leaderboard.
     *
     * @return block operation for leaderboard
     */
    public BiFunction<UUID, Block, Double> getLeaderboardOperation() {
        return processBlockForLeaderboard;
    }

    /**
     * Returns block operation for stats.
     *
     * @return block operation for stats.
     */
    public BiFunction<UUID, Block, Double> getStatsOperation() {
        return processBlockForStats;
    }

    /**
     * Process blocks immediately (and asynchronously).
     */
    private BiFunction<UUID, Block, Double> processBlockForLeaderboard = (uuid, block) -> {
        Double worth = blockWorth.get(block.getType().toString());
        if (worth == null) {
            return 0.0;
        }
        return worth;
    };

    /**
     * Process blocks immediately (and asynchronously). Check if gui is enabled and prepare
     * them for gui view if so.
     */
    private BiFunction<UUID, Block, Double> processBlockForStats = (uuid, block) -> {
        Double worth = blockWorth.get(block.getType().toString());
        if (worth == null) {
            return 0.0;
        }

        if (main.isUseGuiStats() && worth != 0) {
            main.getEntityStatsManager().setBlocksForGuiStats(uuid, block);
        }
        return worth;
    };
}
