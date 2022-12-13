package tk.taverncraft.survivaltop.land;

import java.util.HashMap;
import java.util.LinkedHashMap;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.land.claimplugins.*;
import tk.taverncraft.survivaltop.land.processor.LandProcessor;
import tk.taverncraft.survivaltop.utils.types.ClaimInfo;
import tk.taverncraft.survivaltop.utils.types.MutableInt;

/**
 * LandManager is responsible for all land value calculations.
 */
public class LandManager {
    private final Main main;

    // helper classes
    private LandProcessor landProcessor;
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
        String landType = main.getOptions().getLandType().toLowerCase();

        switch (landType) {
        case "residence":
            landClaimPluginHandler = new ResidenceHandler(main, landProcessor);
            return;
        case "ultimateclaims":
            landClaimPluginHandler = new UltimateClaimsHandler(main, landProcessor);
            return;
        case "griefdefender":
            landClaimPluginHandler = new GriefDefenderHandler(main, landProcessor);
            return;
        case "kingdomsx":
            landClaimPluginHandler = new KingdomsXHandler(main, landProcessor);
            return;
        case "redprotect":
            landClaimPluginHandler = new RedProtectHandler(main, landProcessor);
            return;
        case "crashclaim":
            landClaimPluginHandler = new CrashClaimHandler(main, landProcessor);
            return;
        case "factionsuuid":
        case "saberfactions":
            landClaimPluginHandler = new FactionsUuidHandler(main, landProcessor);
            return;
        case "townyadvanced":
            landClaimPluginHandler = new TownyAdvancedHandler(main, landProcessor);
            return;
        default:
            landClaimPluginHandler = new GriefPreventionHandler(main, landProcessor);
        }
    }

    /**
     * Initializes land operations helper.
     */
    public void initializeLandOperations() {
        this.landProcessor = new LandProcessor(main);
    }

    /**
     * Cleans up holders after stats update.
     *
     * @param id key to identify task
     */
    public void doCleanUp(int id) {
        landProcessor.doCleanUp(id);
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
        landProcessor.createHolder(id);
    }

    /**
     * Gets the blocks to show sender in GUI.
     *
     * @param id key to identify task
     *
     * @return hashmap of block material to its worth
     */
    public HashMap<String, MutableInt> getBlocksForGui(int id) {
        return landProcessor.getBlocksForGui(id);
    }

    /**
     * Gets the spawners to show sender in GUI.
     *
     * @param id key to identify task
     *
     * @return hashmap of spawner entity type to its worth
     */
    public HashMap<String, MutableInt> getSpawnersForGui(int id) {
        return landProcessor.getSpawnersForGui(id);
    }

    /**
     * Gets the container items to show sender in GUI.
     *
     * @param id key to identify task
     *
     * @return hashmap of container item material to its worth
     */
    public HashMap<String, MutableInt> getContainersForGui(int id) {
        return landProcessor.getContainersForGui(id);
    }

    /**
     * Processes spawner types on the main thread for stats.
     *
     * @param id key to identify task
     */
    public void processSpawnerTypes(int id) {
        landProcessor.processSpawnerTypes(id);
    }

    /**
     * Processes container items on the main thread for stats.
     *
     * @param id key to identify task
     */
    public void processContainerItems(int id) {
        landProcessor.processContainerItems(id);
    }

    /**
     * Calculates block worth for a specified entity.
     *
     * @param id key to identify task
     *
     * @return map of sender uuid to the calculated block worth
     */
    public double calculateBlockWorth(int id) {
        return landProcessor.calculateBlockWorth(id);
    }

    /**
     * Calculates spawner worth for a specified entity.
     *
     * @param id key to identify task
     *
     * @return map of sender uuid to the calculated spawner worth
     */
    public double calculateSpawnerWorth(int id) {
        return landProcessor.calculateSpawnerWorth(id);
    }

    /**
     * Calculates container worth for a specified entity.
     *
     * @param id key to identify task
     *
     * @return map of sender uuid to the calculated container worth
     */
    public double calculateContainerWorth(int id) {
        return landProcessor.calculateContainerWorth(id);
    }

    /**
     * Gets the map of worth for all blocks.
     *
     * @return map of block material to value
     */
    public LinkedHashMap<String, Double> getBlockWorth() {
        return this.landProcessor.getBlockWorth();
    }

    /**
     * Gets the worth of a block.
     *
     * @param material material of block
     *
     * @return double representing its worth
     */
    public double getBlockWorth(String material) {
        return this.landProcessor.getBlockWorth(material);
    }

    /**
     * Gets the map of worth for all spawners.
     *
     * @return map of spawner entity type to value
     */
    public LinkedHashMap<String, Double> getSpawnerWorth() {
        return this.landProcessor.getSpawnerWorth();
    }

    /**
     * Gets the worth of a spawner.
     *
     * @param entityType entity type of spawner
     *
     * @return double representing its worth
     */
    public double getSpawnerWorth(String entityType) {
        return this.landProcessor.getSpawnerWorth(entityType);
    }

    /**
     * Gets the map of worth for all container items.
     *
     * @return map of container item material to value
     */
    public LinkedHashMap<String, Double> getContainerWorth() {
        return this.landProcessor.getContainerWorth();
    }

    /**
     * Gets the worth of a container item.
     *
     * @param material material of container item
     *
     * @return double representing its worth
     */
    public double getContainerWorth(String material) {
        return this.landProcessor.getContainerWorth(material);
    }

    /**
     * Gets the claim info for an entity.
     *
     * @param name name of entity to get claim info for
     *
     * @return size 2 array with 1st element = number of claims and 2nd element = number of blocks
     */
    public ClaimInfo getClaimsInfo(String name) {
        return this.landClaimPluginHandler.getClaimsInfo(name);
    }

    /**
     * Sets the state for operations to stop or continue.
     *
     * @param state state to set operations to
     */
    public void setStopOperations(boolean state) {
        landProcessor.setStopOperations(state);
    }
}

