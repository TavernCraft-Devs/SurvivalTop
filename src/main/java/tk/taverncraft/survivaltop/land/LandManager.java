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
    Main main;

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
        includeLand = main.getConfig().getBoolean("include-land", true);
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
    public double getClaimWorth(UUID uuid, double maxX, double minX, double maxY, double minY, double maxZ, double minZ,
                World world, ArrayList<BiFunction<UUID, Block, Double>> blockOperations) {
        double blocksWorth = 0;

        for (double i = minX; i < maxX; i++) {
            for (double j = minY; j < maxY; j++) {
                for (double k = minZ; k < maxZ; k++) {
                    Block block = world.getBlockAt((int) i, (int) j, (int) k);
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

    public HashMap<UUID, Double> calculateSpawnerWorthForAll() {
        HashMap<UUID, Double> tempSpawnerCache = new HashMap<>();
        for (Map.Entry<UUID, ArrayList<Block>> map : senderSpawnerListForAll.entrySet()) {
            ArrayList<Block> blocks = map.getValue();
            double value = landOperationsHelper.processSpawnerWorthForAll(blocks);
            tempSpawnerCache.put(map.getKey(), value);
        }
        return tempSpawnerCache;
    }

    public double calculateSpawnerWorthForIndividual(UUID uuid, boolean useGui) {
        ArrayList<Block> blocks = senderSpawnerListForIndividual.get(uuid);
        if (blocks == null) {
            return 0;
        }
        return landOperationsHelper.processSpawnerWorthForIndividual(blocks, uuid, useGui);
    }

    public HashMap<UUID, Double> calculateContainerWorthForAll() {
        HashMap<UUID, Double> tempContainerCache = new HashMap<>();
        for (Map.Entry<UUID, ArrayList<Block>> map : senderContainerListForAll.entrySet()) {
            ArrayList<Block> blocks = map.getValue();
            double value = landOperationsHelper.processContainerWorthForAll(blocks);
            tempContainerCache.put(map.getKey(), value);
        }
        return tempContainerCache;
    }

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

    public boolean getIncludeLand() {
        return this.includeLand;
    }

    public boolean getIncludeSpawners() {
        return this.includeSpawners;
    }

    public boolean getIncludeContainers() {
        return this.includeContainers;
    }

    public HashMap<String, Integer> getSenderBlockForGui(UUID uuid) {
        return this.senderBlockForGui.get(uuid);
    }

    public HashMap<String, Integer> getSenderSpawnerForGui(UUID uuid) {
        return this.senderSpawnerForGui.get(uuid);
    }

    public HashMap<String, Integer> getSenderContainerForGui(UUID uuid) {
        return this.senderContainerForGui.get(uuid);
    }

    public double getLand(UUID uuid, String name, ArrayList<BiFunction<UUID, Block, Double>> blockOperations) {
        return landClaimPluginHandler.getLandWorth(uuid, name, blockOperations);
    }

    public double getBlockWorth(String name) {
        return this.landOperationsHelper.getBlockWorth(name);
    }

    public double getSpawnerWorth(String name) {
        return this.landOperationsHelper.getSpawnerWorth(name);
    }

    public double getContainerWorth(String name) {
        return this.landOperationsHelper.getContainerWorth(name);
    }

    public void setAllOperations(ArrayList<BiFunction<UUID, Block, Double>> blockOperationsForAll,
            ArrayList<BiFunction<UUID, Block, Double>> blockOperationsForIndividual) {
        this.blockOperationsForAll = blockOperationsForAll;
        this.blockOperationsForIndividual = blockOperationsForIndividual;
    }

    public ArrayList<BiFunction<UUID, Block, Double>> getBlockOperationsForAll() {
        return this.blockOperationsForAll;
    }

    public ArrayList<BiFunction<UUID, Block, Double>> getBlockOperationsForIndividual() {
        return this.blockOperationsForIndividual;
    }
}

