package tk.taverncraft.survivaltop.land;

import java.util.UUID;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

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
     * Initializes land operations helper.
     */
    public void initializeLandOperations() {
        this.landOperationsHelper = new LandOperationsHelper(main);
    }

    /**
     * Get the worth of a land.
     *
     * @param uuid uuid of sender if this is run through stats command; otherwise entities
     * @param name name of entity to get land worth for
     * @param isLeaderboardUpdate true if is a leaderboard update, false otherwise (i.e. stats)
     */
    public void processEntityLand(UUID uuid, String name, boolean isLeaderboardUpdate) {
        landClaimPluginHandler.processEntityLand(uuid, name, isLeaderboardUpdate);
    }

    /**
     * Creates holders for leaderboard.
     *
     * @param uuid uuid of each entity
     */
    public void createHoldersForLeaderboard(UUID uuid) {
        landOperationsHelper.createHoldersForLeaderboard(uuid);
    }

    /**
     * Creates holders for stats.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     */
    public void createHoldersForStats(UUID uuid) {
        landOperationsHelper.createHoldersForStats(uuid);
    }

    /**
     * Gets the blocks to show sender in GUI.
     *
     * @return hashmap of block material to its worth
     */
    public HashMap<Material, Integer> getBlocksForGuiStats(UUID uuid) {
        return landOperationsHelper.getBlocksForGuiStats(uuid);
    }

    /**
     * Gets the spawners to show sender in GUI.
     *
     * @return hashmap of spawner entity type to its worth
     */
    public HashMap<EntityType, Integer> getSpawnersForGuiStats(UUID uuid) {
        return landOperationsHelper.getSpawnersForGuiStats(uuid);
    }

    /**
     * Gets the container items to show sender in GUI.
     *
     * @return hashmap of container item material to its worth
     */
    public HashMap<Material, Integer> getContainersForGuiStats(UUID uuid) {
        return landOperationsHelper.getContainersForGuiStats(uuid);
    }

    /**
     * Processes spawner types on the main thread for leaderboard.
     */
    public void processSpawnerTypesForLeaderboard() {
        landOperationsHelper.processSpawnerTypesForLeaderboard();
    }

    /**
     * Processes spawner types on the main thread for stats.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     */
    public void processSpawnerTypesForStats(UUID uuid) {
        landOperationsHelper.processSpawnerTypesForStats(uuid);
    }

    /**
     * Processes container items on the main thread for leaderboard.
     */
    public void processContainerItemsForLeaderboard() {
        landOperationsHelper.processContainerItemsForLeaderboard();
    }

    /**
     * Processes container items on the main thread for stats.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     */
    public void processContainerItemsForStats(UUID uuid) {
        landOperationsHelper.processContainerItemsForStats(uuid);
    }

    /**
     * Calculates block worth for all entities.
     *
     * @return map of entities uuid to their block worth
     */
    public HashMap<UUID, Double> calculateBlockWorthForLeaderboard() {
        return landOperationsHelper.calculateBlockWorthForLeaderboard();
    }

    /**
     * Calculates block worth for a specified entity.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     *
     * @return map of sender uuid to the calculated block worth
     */
    public double calculateBlockWorthForStats(UUID uuid) {
        return landOperationsHelper.calculateBlockWorthForStats(uuid);
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
     * Gets the map of worth for all blocks.
     *
     * @return map of block material to value
     */
    public LinkedHashMap<Material, Double> getBlockWorth() {
        return this.landOperationsHelper.getBlockWorth();
    }

    /**
     * Get the worth of a block.
     *
     * @param material material of block
     *
     * @return double representing its worth
     */
    public double getBlockWorth(Material material) {
        return this.landOperationsHelper.getBlockWorth(material);
    }

    /**
     * Gets the map of worth for all spawners.
     *
     * @return map of spawner entity type to value
     */
    public LinkedHashMap<EntityType, Double> getSpawnerWorth() {
        return this.landOperationsHelper.getSpawnerWorth();
    }

    /**
     * Get the worth of a spawner.
     *
     * @param entityType entity type of spawner
     *
     * @return double representing its worth
     */
    public double getSpawnerWorth(EntityType entityType) {
        return this.landOperationsHelper.getSpawnerWorth(entityType);
    }

    /**
     * Gets the map of worth for all container items.
     *
     * @return map of container item material to value
     */
    public LinkedHashMap<Material, Double> getContainerWorth() {
        return this.landOperationsHelper.getContainerWorth();
    }

    /**
     * Get the worth of a container item.
     *
     * @param material material of container item
     *
     * @return double representing its worth
     */
    public double getContainerWorth(Material material) {
        return this.landOperationsHelper.getContainerWorth(material);
    }
}

