package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.BiFunction;

import org.bukkit.block.Block;

/**
 * Interface to get land worth from different land claim plugins.
 */
public interface LandClaimPluginHandler {

    /**
     * Get the worth of a land.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     * @param name name of entity to get land worth for
     * @param blockOperations operations to perform
     *
     * @return double representing its worth
     */
    double getLandWorth(UUID uuid, String name,
            ArrayList<BiFunction<UUID, Block, Double>> blockOperations);
}
