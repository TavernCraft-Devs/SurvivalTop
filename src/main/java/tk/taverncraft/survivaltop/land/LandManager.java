package tk.taverncraft.survivaltop.land;

import java.util.HashMap;
import java.util.LinkedHashMap;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.land.claimplugins.*;
import tk.taverncraft.survivaltop.land.operations.LandOperationsHelper;
import tk.taverncraft.survivaltop.utils.types.MutableInt;

/**
 * LandManager is responsible for all land value calculations.
 */
public class LandManager {
    private final Main main;

    // helper classes
    private LandOperationsHelper landOperationsHelper;
    private LandClaimPluginHandler landClaimPluginHandler;

    /**
     * Constructor for LandManager.
     *
     * @param main plugin class
     */
    public LandManager(Main main) throws NullPointerException {
        this.main = main;
        initializeLandOperations();
        initializeLandType();
    }

    /**
     * Initializes values for land type depending on which land plugin is used.
     */
    public void initializeLandType() throws NullPointerException {
        String landType = main.getConfig().getString(
                "land-type", "griefprevention").toLowerCase();

        switch (landType) {
        case "residence":
            landClaimPluginHandler = new ResidenceHandler(main, landOperationsHelper);
            return;
        case "ultimateclaims":
            landClaimPluginHandler = new UltimateClaimsHandler(main, landOperationsHelper);
            return;
        case "griefdefender":
            landClaimPluginHandler = new GriefDefenderHandler(main, landOperationsHelper);
            return;
        case "kingdomsx":
            landClaimPluginHandler = new KingdomsXHandler(main, landOperationsHelper);
            return;
        case "redprotect":
            landClaimPluginHandler = new RedProtectHandler(main, landOperationsHelper);
            return;
        case "crashclaim":
            landClaimPluginHandler = new CrashClaimHandler(main, landOperationsHelper);
            return;
        case "factionsuuid":
        case "saberfactions":
            landClaimPluginHandler = new FactionsUuidHandler(main, landOperationsHelper);
            return;
        case "townyadvanced":
            landClaimPluginHandler = new TownyAdvancedHandler(main, landOperationsHelper);
            return;
        default:
            landClaimPluginHandler = new GriefPreventionHandler(main, landOperationsHelper);
        }
    }

    /**
     * Initializes land operations helper.
     */
    public void initializeLandOperations() {
        this.landOperationsHelper = new LandOperationsHelper(main);
    }

    /**
     * Cleans up holders after stats update.
     *
     * @param id key to identify task
     */
    public void doCleanUp(int id) {
        landOperationsHelper.doCleanUp(id);
    }

    /**
     * Processes the worth of a land.
     *
     * @param name name of entity to get land worth for
     * @param id key to identify task
     */
    public void processEntityLand(String name, int id) {
        landClaimPluginHandler.processEntityLand(name, id);
    }

    /**
     * Creates holders for stats.
     *
     * @param id key to identify task
     */
    public void createHolder(int id) {
        landOperationsHelper.createHolder(id);
    }

    /**
     * Gets the blocks to show sender in GUI.
     *
     * @param id key to identify task
     *
     * @return hashmap of block material to its worth
     */
    public HashMap<String, MutableInt> getBlocksForGui(int id) {
        return landOperationsHelper.getBlocksForGui(id);
    }

    /**
     * Gets the spawners to show sender in GUI.
     *
     * @param id key to identify task
     *
     * @return hashmap of spawner entity type to its worth
     */
    public HashMap<String, MutableInt> getSpawnersForGui(int id) {
        return landOperationsHelper.getSpawnersForGui(id);
    }

    /**
     * Gets the container items to show sender in GUI.
     *
     * @param id key to identify task
     *
     * @return hashmap of container item material to its worth
     */
    public HashMap<String, MutableInt> getContainersForGui(int id) {
        return landOperationsHelper.getContainersForGui(id);
    }

    /**
     * Processes spawner types on the main thread for stats.
     *
     * @param id key to identify task
     */
    public void processSpawnerTypes(int id) {
        landOperationsHelper.processSpawnerTypes(id);
    }

    /**
     * Processes container items on the main thread for stats.
     *
     * @param id key to identify task
     */
    public void processContainerItems(int id) {
        landOperationsHelper.processContainerItems(id);
    }

    /**
     * Calculates block worth for a specified entity.
     *
     * @param id key to identify task
     *
     * @return map of sender uuid to the calculated block worth
     */
    public double calculateBlockWorth(int id) {
        return landOperationsHelper.calculateBlockWorth(id);
    }

    /**
     * Calculates spawner worth for a specified entity.
     *
     * @param id key to identify task
     *
     * @return map of sender uuid to the calculated spawner worth
     */
    public double calculateSpawnerWorth(int id) {
        return landOperationsHelper.calculateSpawnerWorth(id);
    }

    /**
     * Calculates container worth for a specified entity.
     *
     * @param id key to identify task
     *
     * @return map of sender uuid to the calculated container worth
     */
    public double calculateContainerWorth(int id) {
        return landOperationsHelper.calculateContainerWorth(id);
    }

    /**
     * Gets the map of worth for all blocks.
     *
     * @return map of block material to value
     */
    public LinkedHashMap<String, Double> getBlockWorth() {
        return this.landOperationsHelper.getBlockWorth();
    }

    /**
     * Gets the worth of a block.
     *
     * @param material material of block
     *
     * @return double representing its worth
     */
    public double getBlockWorth(String material) {
        return this.landOperationsHelper.getBlockWorth(material);
    }

    /**
     * Gets the map of worth for all spawners.
     *
     * @return map of spawner entity type to value
     */
    public LinkedHashMap<String, Double> getSpawnerWorth() {
        return this.landOperationsHelper.getSpawnerWorth();
    }

    /**
     * Gets the worth of a spawner.
     *
     * @param entityType entity type of spawner
     *
     * @return double representing its worth
     */
    public double getSpawnerWorth(String entityType) {
        return this.landOperationsHelper.getSpawnerWorth(entityType);
    }

    /**
     * Gets the map of worth for all container items.
     *
     * @return map of container item material to value
     */
    public LinkedHashMap<String, Double> getContainerWorth() {
        return this.landOperationsHelper.getContainerWorth();
    }

    /**
     * Gets the worth of a container item.
     *
     * @param material material of container item
     *
     * @return double representing its worth
     */
    public double getContainerWorth(String material) {
        return this.landOperationsHelper.getContainerWorth(material);
    }

    /**
     * Gets the claim info for an entity.
     *
     * @param name name of entity to get claim info for
     *
     * @return size 2 array with 1st element = number of claims and 2nd element = number of blocks
     */
    public Long[] getClaimsInfo(String name) {
        return this.landClaimPluginHandler.getClaimsInfo(name);
    }

    /**
     * Sets the state for operations to stop or continue.
     *
     * @param state state to set operations to
     */
    public void setStopOperations(boolean state) {
        landOperationsHelper.setStopOperations(state);
    }
}

