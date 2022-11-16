package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.UUID;

/**
 * Interface to get land worth from different land claim plugins.
 */
public interface LandClaimPluginHandler {

    /**
     * Processes the worth of a land.
     *
     * @param uuid uuid of sender if this is run through stats command; otherwise entities
     * @param name name of entity to get land worth for
     * @param isLeaderboardUpdate true if is a leaderboard update, false otherwise (i.e. stats)
     */
    void processEntityLand(UUID uuid, String name, boolean isLeaderboardUpdate);

    /**
     * Gets the claim info for an entity.
     *
     * @param name name of entity to get claim info for
     *
     * @return size 2 array with 1st element = number of claims and 2nd element = number of blocks
     */
    Long[] getClaimsInfo(String name);
}
