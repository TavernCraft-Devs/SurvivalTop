package tk.taverncraft.survivaltop.land;

import java.util.UUID;
import java.util.HashMap;
import java.util.LinkedHashMap;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.land.claimplugins.*;
import tk.taverncraft.survivaltop.land.operations.LandOperationsHelper;

/**
 * LandManager is responsible for all land value calculations.
 */
public class LandManager {
    private Main main;

    // helper classes
    private LandOperationsHelper landOperationsHelper;
    private LandClaimPluginHandler landClaimPluginHandler;

    // boolean to determine what is included in land wealth
    private boolean includeSpawners;
    private boolean includeContainers;

    /**
     * Constructor for LandManager.
     *
     * @param main plugin class
     */
    public LandManager(Main main) throws NullPointerException {
        this.main = main;
        initializeCalculationType();
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
     * Initializes land type to calculate depending on config.
     */
    public void initializeCalculationType() {
        includeSpawners = main.getConfig().getBoolean("include-spawners", false);
        includeContainers = main.getConfig().getBoolean("include-containers", false);
    }

    /**
     * Initializes operations to perform for land calculation.
     */
    public void initializeLandOperations() {
        this.landOperationsHelper = new LandOperationsHelper(main);
    }

    /**
     * Calculates spawner worth for all entities.
     *
     * @return map of entities uuid to their spawner worth
     */
    public HashMap<UUID, Double> calculateSpawnerWorthForLeaderboard() {
        return landOperationsHelper.calculateSpawnerWorthForLeaderboard();
    }

    /**
     * Calculates spawner worth for a specified entity.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     *
     * @return map of sender uuid to the calculated spawner worth
     */
    public double calculateSpawnerWorthForStats(UUID uuid) {
        return landOperationsHelper.calculateSpawnerWorthForStats(uuid);
    }

    /**
     * Calculates container worth for all entities.
     *
     * @return map of entities uuid to their container worth
     */
    public HashMap<UUID, Double> calculateContainerWorthForLeaderboard() {
        return landOperationsHelper.calculateContainerWorthForLeaderboard();
    }

    /**
     * Calculates container worth for a specified entity.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     *
     * @return map of sender uuid to the calculated container worth
     */
    public double calculateContainerWorthForStats(UUID uuid) {
        return landOperationsHelper.calculateContainerWorthForStats(uuid);
    }

    /**
     * Resets all sender info list at end of update.
     */
    public void doCleanup() {
        landOperationsHelper.doLeaderboardCleanup();
    }

    /**
     * Resets a specific sender's info list after calculating stats.
     */
    public void doCleanup(UUID uuid) {
        landOperationsHelper.doStatsCleanup(uuid);
    }

    /**
     * Checks if spawner is included.
     *
     * @return true if included, false otherwise
     */
    public boolean getIncludeSpawners() {
        return this.includeSpawners;
    }

    /**
     * Checks if container is included.
     *
     * @return true if included, false otherwise
     */
    public boolean getIncludeContainers() {
        return this.includeContainers;
    }

    /**
     * Get the worth of a land.
     *
     * @param uuid uuid of sender if this is run through stats command; otherwise entities
     * @param name name of entity to get land worth for
     * @param isLeaderboardUpdate true if is a leaderboard update, false otherwise (i.e. stats)
     *
     * @return double representing its worth
     */
    public double getLandWorthForEntity(UUID uuid, String name, boolean isLeaderboardUpdate) {
        return landClaimPluginHandler.getLandWorth(uuid, name, isLeaderboardUpdate);
    }

    /**
     * Gets the map of worth for all blocks.
     *
     * @return map of block name to value
     */
    public LinkedHashMap<String, Double> getBlockWorth() {
        return this.landOperationsHelper.getBlockWorth();
    }

    /**
     * Get the worth of a block.
     *
     * @param name name of block
     *
     * @return double representing its worth
     */
    public double getBlockWorth(String name) {
        return this.landOperationsHelper.getBlockWorth(name);
    }

    /**
     * Gets the map of worth for all spawners.
     *
     * @return map of spawner name to value
     */
    public LinkedHashMap<String, Double> getSpawnerWorth() {
        return this.landOperationsHelper.getSpawnerWorth();
    }

    /**
     * Get the worth of a spawner.
     *
     * @param name name of spawner
     *
     * @return double representing its worth
     */
    public double getSpawnerWorth(String name) {
        return this.landOperationsHelper.getSpawnerWorth(name);
    }

    /**
     * Gets the map of worth for all container items.
     *
     * @return map of container item name to value
     */
    public LinkedHashMap<String, Double> getContainerWorth() {
        return this.landOperationsHelper.getContainerWorth();
    }

    /**
     * Get the worth of a container item.
     *
     * @param name name of container item
     *
     * @return double representing its worth
     */
    public double getContainerWorth(String name) {
        return this.landOperationsHelper.getContainerWorth(name);
    }
}

