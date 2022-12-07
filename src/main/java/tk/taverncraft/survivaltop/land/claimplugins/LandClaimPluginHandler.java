package tk.taverncraft.survivaltop.land.claimplugins;

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
     * @return size 2 array with 1st element = number of claims and 2nd element = number of blocks
     */
    Long[] getClaimsInfo(String name);
}
