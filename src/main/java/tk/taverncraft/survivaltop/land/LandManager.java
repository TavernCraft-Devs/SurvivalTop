package tk.taverncraft.survivaltop.land;

import java.util.*;
import java.util.function.BiFunction;

import org.bukkit.*;
import org.bukkit.block.Block;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.land.claimplugins.*;

/**
 * LandManager is responsible for all land value calculations.
 */
public class LandManager {
    private Main main;

    // helper classes
    private LandOperationsHelper landOperationsHelper;
    private LandClaimPluginHandler landClaimPluginHandler;

    // for postprocessing on main thread
    // note that uuid below is uuid of the sender
    private HashMap<UUID, ArrayList<Block>> senderSpawnerListForAll = new HashMap<>();
    private HashMap<UUID, ArrayList<Block>> senderContainerListForAll = new HashMap<>();
    private HashMap<UUID, ArrayList<Block>> senderSpawnerListForIndividual = new HashMap<>();
    private HashMap<UUID, ArrayList<Block>> senderContainerListForIndividual = new HashMap<>();

    // for tracking blocks, spawners and containers to be used in gui
    // note that uuid below is uuid of the sender
    private HashMap<UUID, HashMap<String, Integer>> senderBlockForGui = new HashMap<>();
    private HashMap<UUID, HashMap<String, Integer>> senderSpawnerForGui = new HashMap<>();
    private HashMap<UUID, HashMap<String, Integer>> senderContainerForGui = new HashMap<>();

    // boolean to determine what is included in land wealth
    private boolean includeLand;
    private boolean includeSpawners;
    private boolean includeContainers;

    // list of operations to perform for calculating land wealth
    ArrayList<BiFunction<UUID, Block, Double>> blockOperationsForAll = new ArrayList<>();
    ArrayList<BiFunction<UUID, Block, Double>> blockOperationsForIndividual = new ArrayList<>();

    /**
     * Constructor for LandManager.
     *
     * @param main plugin class
     */
    public LandManager(Main main) throws NullPointerException {
        this.main = main;
        initializeLandType();
        initializeCalculationType();
        initializeLandOperations();
    }

    /**
     * Initializes values for land type depending on which land plugin is used.
     */
    public void initializeLandType() throws NullPointerException {
        String landType = main.getConfig().getString("land-type", "griefprevention");
        if (landType.equalsIgnoreCase("residence")) {
            landClaimPluginHandler = new ResidenceHandler(main);
        } else if (landType.equalsIgnoreCase("ultimateclaims")) {
            landClaimPluginHandler = new UltimateClaimsHandler(main);
        } else if (landType.equalsIgnoreCase("griefdefender")) {
            landClaimPluginHandler = new GriefDefenderHandler(main);
        } else if (landType.equalsIgnoreCase("kingdomsx")) {
            landClaimPluginHandler = new KingdomsXHandler(main);
        } else if (landType.equalsIgnoreCase("redprotect")) {
            landClaimPluginHandler = new RedProtectHandler(main);
        } else if (landType.equalsIgnoreCase("crashclaim")) {
            landClaimPluginHandler = new CrashClaimHandler(main);
        } else if (landType.equalsIgnoreCase("factionsuuid")) {
            landClaimPluginHandler = new FactionsUuidHandler(main);
        } else if (landType.equalsIgnoreCase("townyadvanced")) {
            landClaimPluginHandler = new TownyAdvancedHandler(main);
        } else {
            landClaimPluginHandler = new GriefPreventionHandler(main);
        }
    }

    /**
     * Initializes land type to calculate depending on config.
     */
    public void initializeCalculationType() {
        senderSpawnerListForAll = new HashMap<>();
        senderContainerListForAll = new HashMap<>();
        senderSpawnerListForIndividual = new HashMap<>();
        senderContainerListForIndividual = new HashMap<>();
        senderBlockForGui = new HashMap<>();
        senderSpawnerForGui = new HashMap<>();
        senderContainerForGui = new HashMap<>();
        includeLand = main.landIsIncluded();
        includeSpawners = main.getConfig().getBoolean("include-spawners", false);
        includeContainers = main.getConfig().getBoolean("include-containers", false);
    }

    /**
     * Initializes operations to perform for land calculation.
     */
    public void initializeLandOperations() {
        this.landOperationsHelper = new LandOperationsHelper(main, this);
    }

    /**
     * Runs update with inclusion of search for spawners.
     *
     * @param maxX max x coordinate
     * @param minX min x coordinate
     * @param maxY max y coordinate
     * @param minY min y coordinate
     * @param maxZ max z coordinate
     * @param minZ min z coordinate
     * @param world world to search in
     * @param blockOperations block operations to perform
     */
    public double getClaimWorth(UUID uuid, double maxX, double minX, double maxY, double minY,
            double maxZ, double minZ, World world,
            ArrayList<BiFunction<UUID, Block, Double>> blockOperations) {
        double blocksWorth = 0;

        for (int i = (int) minX; i < maxX; i++) {
            for (int j = (int) minY; j < maxY; j++) {
                for (int k = (int) minZ; k < maxZ; k++) {
                    Block block = world.getBlockAt(i, j, k);
                    for (BiFunction<UUID, Block, Double> f : blockOperations) {
                        double blockWorth = ((Number) f.apply(uuid, block)).doubleValue();
                        blocksWorth += blockWorth;
                    }
                }
            }
        }
        return blocksWorth;
    }

    /**
     * Sets spawner to be processed on main thread later (for leaderboard update).
     *
     * @param uuid sender to link spawner to
     * @param block spawner block to check for
     */
    public void setSenderSpawnerForAll(UUID uuid, Block block) {
        senderSpawnerListForAll.computeIfAbsent(uuid, k -> new ArrayList<>());
        senderSpawnerListForAll.get(uuid).add(block);
    }

    /**
     * Sets container to be processed on main thread later (for leaderboard update).
     *
     * @param uuid sender to link spawner to
     * @param block container block to check for
     */
    public void setSenderContainerForAll(UUID uuid, Block block) {
        senderContainerListForAll.computeIfAbsent(uuid, k -> new ArrayList<>());
        senderContainerListForAll.get(uuid).add(block);
    }

    /**
     * Sets spawner to be processed on main thread later (for individual entity stats).
     *
     * @param uuid sender to link spawner to
     * @param block spawner block to check for
     */
    public void setSenderSpawnerForIndividual(UUID uuid, Block block) {
        senderSpawnerListForIndividual.computeIfAbsent(uuid, k -> new ArrayList<>());
        senderSpawnerListForIndividual.get(uuid).add(block);
    }

    /**
     * Sets container to be processed on main thread later (for individual entity stats).
     *
     * @param uuid sender to link spawner to
     * @param block container block to check for
     */
    public void setSenderContainerForIndividual(UUID uuid, Block block) {
        senderContainerListForIndividual.computeIfAbsent(uuid, k -> new ArrayList<>());
        senderContainerListForIndividual.get(uuid).add(block);
    }

    /**
     * Sets block count and value to be used in gui.
     *
     * @param uuid sender to link block to
     * @param block block to check for
     */
    public void setSenderBlockForGui(UUID uuid, Block block) {
        senderBlockForGui.computeIfAbsent(uuid, k -> new HashMap<>());
        String name = block.getType().toString();
        Integer currentCount = senderBlockForGui.get(uuid).get(name);
        if (currentCount == null) {
            currentCount = 0;
        }
        senderBlockForGui.get(uuid).put(name, currentCount + 1);
    }

    /**
     * Sets spawner count and value to be used in gui.
     *
     * @param uuid sender  to link spawner to
     * @param mobName spawner name to check for
     */
    public void setSenderSpawnerForGui(UUID uuid, String mobName) {
        senderSpawnerForGui.computeIfAbsent(uuid, k -> new HashMap<>());
        Integer currentCount = senderSpawnerForGui.get(uuid).get(mobName);
        if (currentCount == null) {
            currentCount = 0;
        }
        senderSpawnerForGui.get(uuid).put(mobName, currentCount + 1);
    }

    /**
     * Sets container count and value to be used in gui.
     *
     * @param uuid sender to link container to
     * @param itemName item name to check for
     */
    public void setSenderContainerForGui(UUID uuid, String itemName, int amount) {
        senderContainerForGui.computeIfAbsent(uuid, k -> new HashMap<>());
        Integer currentCount = senderContainerForGui.get(uuid).get(itemName);
        if (currentCount == null) {
            currentCount = 0;
        }
        senderContainerForGui.get(uuid).put(itemName, currentCount + amount);
    }

    /**
     * Calculates spawner worth for all entities.
     *
     * @return map of entities uuid to their spawner worth
     */
    public HashMap<UUID, Double> calculateSpawnerWorthForAll() {
        HashMap<UUID, Double> tempSpawnerCache = new HashMap<>();
        for (Map.Entry<UUID, ArrayList<Block>> map : senderSpawnerListForAll.entrySet()) {
            ArrayList<Block> blocks = map.getValue();
            double value = landOperationsHelper.processSpawnerWorthForAll(blocks);
            tempSpawnerCache.put(map.getKey(), value);
        }
        return tempSpawnerCache;
    }

    /**
     * Calculates spawner worth for a specified entity.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     * @param useGui whether a gui is used
     *
     * @return map of sender uuid to the calculated spawner worth
     */
    public double calculateSpawnerWorthForIndividual(UUID uuid, boolean useGui) {
        ArrayList<Block> blocks = senderSpawnerListForIndividual.get(uuid);
        if (blocks == null) {
            return 0;
        }
        return landOperationsHelper.processSpawnerWorthForIndividual(blocks, uuid, useGui);
    }

    /**
     * Calculates container worth for all entities.
     *
     * @return map of entities uuid to their container worth
     */
    public HashMap<UUID, Double> calculateContainerWorthForAll() {
        HashMap<UUID, Double> tempContainerCache = new HashMap<>();
        for (Map.Entry<UUID, ArrayList<Block>> map : senderContainerListForAll.entrySet()) {
            ArrayList<Block> blocks = map.getValue();
            double value = landOperationsHelper.processContainerWorthForAll(blocks);
            tempContainerCache.put(map.getKey(), value);
        }
        return tempContainerCache;
    }

    /**
     * Calculates container worth for a specified entity.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     * @param useGui whether a gui is used
     *
     * @return map of sender uuid to the calculated container worth
     */
    public double calculateContainerWorthForIndividual(UUID uuid, boolean useGui) {
        ArrayList<Block> blocks = senderContainerListForIndividual.get(uuid);
        if (blocks == null) {
            return 0;
        }
        return landOperationsHelper.processContainerWorthForIndividual(blocks, uuid, useGui);
    }

    /**
     * Resets all sender info list at end of update.
     */
    public void resetSenderLists() {
        senderSpawnerListForAll = new HashMap<>();
        senderContainerListForAll = new HashMap<>();
    }

    /**
     * Resets a specific sender's info list after calculating stats.
     */
    public void resetSenderLists(UUID uuid) {
        senderSpawnerListForIndividual.remove(uuid);
        senderContainerListForIndividual.remove(uuid);
        senderBlockForGui.remove(uuid);
        senderSpawnerForGui.remove(uuid);
        senderContainerForGui.remove(uuid);
    }

    /**
     * Checks if land is included.
     *
     * @return true if included, false otherwise
     */
    public boolean getIncludeLand() {
        return this.includeLand;
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
     * Gets the blocks to show sender in GUI.
     *
     * @return hashmap of block name to its worth
     */
    public HashMap<String, Integer> getSenderBlockForGui(UUID uuid) {
        return this.senderBlockForGui.get(uuid);
    }

    /**
     * Gets the spawners to show sender in GUI.
     *
     * @return hashmap of spawner name to its worth
     */
    public HashMap<String, Integer> getSenderSpawnerForGui(UUID uuid) {
        return this.senderSpawnerForGui.get(uuid);
    }

    /**
     * Gets the container items to show sender in GUI.
     *
     * @return hashmap of container item name to its worth
     */
    public HashMap<String, Integer> getSenderContainerForGui(UUID uuid) {
        return this.senderContainerForGui.get(uuid);
    }

    /**
     * Get the worth of a land.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     * @param name name of entity to get land worth for
     * @param blockOperations operations to perform
     *
     * @return double representing its worth
     */
    public double getLand(UUID uuid, String name,
            ArrayList<BiFunction<UUID, Block, Double>> blockOperations) {
        return landClaimPluginHandler.getLandWorth(uuid, name, blockOperations);
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

    /**
     * Setter for all block operations.
     *
     * @param blockOperationsForAll block operations to set for all entities
     * @param blockOperationsForIndividual block operations to set for an individual entity.
     */
    public void setAllOperations(ArrayList<BiFunction<UUID, Block, Double>> blockOperationsForAll,
            ArrayList<BiFunction<UUID, Block, Double>> blockOperationsForIndividual) {
        this.blockOperationsForAll = blockOperationsForAll;
        this.blockOperationsForIndividual = blockOperationsForIndividual;
    }

    /**
     * Operations to perform on all entities (during leaderboard update).
     *
     * @return list of operations to perform
     */
    public ArrayList<BiFunction<UUID, Block, Double>> getBlockOperationsForAll() {
        return this.blockOperationsForAll;
    }

    /**
     * Operations to perform on a single entity (during individual stats checking).
     *
     * @return list of operations to perform
     */
    public ArrayList<BiFunction<UUID, Block, Double>> getBlockOperationsForIndividual() {
        return this.blockOperationsForIndividual;
    }
}

