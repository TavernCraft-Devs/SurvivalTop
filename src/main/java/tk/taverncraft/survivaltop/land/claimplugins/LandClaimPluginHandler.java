package tk.taverncraft.survivaltop.land.claimplugins;

import tk.taverncraft.survivaltop.utils.types.ClaimInfo;

/**
 * Interface to get land worth from different land claim plugins.
 */
public interface LandClaimPluginHandler {

    /**
     * Processes the worth of a land.
     *
     * @param name name of entity to get land worth for
     * @param id key to identify task
     */
    void processEntityLand(String name, int id);

    /**
     * Gets the claim info for an entity.
     *
     * @param name name of entity to get claim info for
     *
     * @return information for claim
     */
    ClaimInfo getClaimsInfo(String name);
}
