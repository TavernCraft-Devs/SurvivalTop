package tk.taverncraft.survivaltop.stats;

import java.util.UUID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.utils.MessageManager;

/**
 * ServerStatsManager handles all logic for updating of server-wide stats.
 */
public class ServerStatsManager {
    private Main main;

    // cache values used for leaderboard/papi
    private ConcurrentHashMap<UUID, Integer> entityPositionCache;
    private ConcurrentHashMap<UUID, EntityCache> uuidToEntityCacheMap;
    private ArrayList<EntityCache> entityCacheList;

    // temp hashmaps with pseudo uuids used only when entity is set to group
    private HashMap<UUID, String> groupUuidToNameMap;
    private HashMap<String, UUID> groupNameToUuidMap;

    /**
     * Constructor for ServerStatsManager.
     *
     * @param main plugin class
     */
    public ServerStatsManager(Main main) throws NullPointerException {
        this.main = main;
        initializeValues();
    }

    /**
     * Initializes all values to default.
     */
    public void initializeValues() throws NullPointerException {
        entityPositionCache = new ConcurrentHashMap<>();
        uuidToEntityCacheMap = new ConcurrentHashMap<>();
        entityCacheList = new ArrayList<>();
        groupUuidToNameMap = new HashMap<>();
        groupNameToUuidMap = new HashMap<>();
    }

    /**
     * Updates the entire leaderboard wealth. May take a while to run if player-base is large.
     *
     * @param sender user executing the update
     */
    public void updateWealthStats(CommandSender sender) {
        try {
            main.getLandManager().resetSenderLists();
            MessageManager.sendMessage(sender, "update-started");
            if (this.main.groupIsEnabled()) {
                updateForGroups();
            } else {
                updateForPlayers();
            }
            processSpawnersAndContainers(sender);
        } catch (Exception e) {
            Bukkit.getLogger().info(e.getMessage());
            this.main.getLeaderboardManager().stopExistingTasks();
        }
    }

    /**
     * Processes spawners and containers on main thread before doing post updates on async thread
     * again.
     *
     * @param sender user executing the update
     */
    private void processSpawnersAndContainers(CommandSender sender) {
        new BukkitRunnable() {
            @Override
            public void run() {
                HashMap<UUID, Double> tempSpawnerCache =
                    main.getLandManager().calculateSpawnerWorthForAll();
                HashMap<UUID, Double> tempContainerCache =
                    main.getLandManager().calculateContainerWorthForAll();
                executePostUpdateActions(tempSpawnerCache, tempContainerCache, sender);
            }
        }.runTask(main);
    }

    /**
     * Performs update by individual players.
     */
    private void updateForPlayers() {
        Arrays.stream(this.main.getServer().getOfflinePlayers()).forEach(offlinePlayer -> {
            calculateAndCacheEntities(offlinePlayer.getUniqueId(), offlinePlayer.getName());
        });
    }

    /**
     * Performs update by groups.
     */
    private void updateForGroups() {
        // reset cache on each update since unlike players, group uuids are temporary
        this.uuidToEntityCacheMap = new ConcurrentHashMap<>();
        this.groupUuidToNameMap = new HashMap<>();
        this.groupNameToUuidMap = new HashMap<>();

        List<String> groups = this.main.getGroupManager().getGroups();
        for (String group : groups) {
            UUID tempUuid = UUID.randomUUID();
            groupUuidToNameMap.put(tempUuid, group);
            groupNameToUuidMap.put(group, tempUuid);
            calculateAndCacheEntities(tempUuid, group);
        }
    }

    /**
     * Calculates the worth for each entity and caches them.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     * @param name name of entity to calculate for
     */
    private void calculateAndCacheEntities(UUID uuid, String name) {
        double entityBlockWorth = 0;
        double entityBalWorth = 0;
        double entityInvWorth = 0;
        if (main.landIsIncluded()) {
            entityBlockWorth = main.getLandManager().getLand(uuid, name,
                    main.getLandManager().getBlockOperationsForAll());
        }
        if (main.balIsIncluded()) {
            entityBalWorth = main.getBalanceManager().getBalanceForEntity(name);
        }
        if (main.inventoryIsIncluded()) {
            entityInvWorth = main.getInventoryManager().getEntityInventoryWorth(uuid, name);
        }
        EntityCache eCache = new EntityCache(uuid, entityBalWorth, entityBlockWorth,
                entityInvWorth, 0, 0);
        uuidToEntityCacheMap.put(uuid, eCache);
    }

    /**
     * Cleans up after values of entities are retrieved and updates the relevant caches. Also
     * updates spawner/container values if applicable.
     *
     * @param tempSpawnerCache map from uuid to spawner value
     * @param tempContainerCache map from uuid to container value
     * @param sender sender who initiated the leaderboard update
     */
    private void executePostUpdateActions(HashMap<UUID, Double> tempSpawnerCache, HashMap<UUID,
        Double> tempContainerCache, CommandSender sender) {
        new BukkitRunnable() {
            @Override
            public void run() {
                executePostUpdateSpawners(tempSpawnerCache);
                executePostUpdateContainers(tempContainerCache);
                HashMap<UUID, EntityCache> tempSortedCache =
                        sortTotalWealthCache(uuidToEntityCacheMap);
                sortWealthCacheKeyValue(tempSortedCache);
                main.getLandManager().resetSenderLists();
                main.getLeaderboardManager().completeLeaderboardUpdate(sender, tempSortedCache);
                main.getStorageManager().saveToStorage(entityCacheList);
            }
        }.runTaskLaterAsynchronously(main, 0);
    }

    /**
     * Updates the values of spawners after they are processed on the main thread.
     *
     * @param tempSpawnerCache spawner cache to update from
     */
    private void executePostUpdateSpawners(HashMap<UUID, Double> tempSpawnerCache) {
        for (Map.Entry<UUID, Double> map : tempSpawnerCache.entrySet()) {
            UUID uuid = map.getKey();
            double value = map.getValue();
            EntityCache eCache = uuidToEntityCacheMap.get(uuid);
            EntityCache updatedCache = eCache.setSpawnerWealth(value);
            uuidToEntityCacheMap.put(uuid, updatedCache);
        }
    }

    /**
     * Updates the values of containers after they are processed on the main thread.
     *
     * @param tempContainerCache container cache to update from
     */
    private void executePostUpdateContainers(HashMap<UUID, Double> tempContainerCache) {
        for (Map.Entry<UUID, Double> map : tempContainerCache.entrySet()) {
            UUID uuid = map.getKey();
            double value = map.getValue();
            EntityCache eCache = uuidToEntityCacheMap.get(uuid);
            EntityCache updatedCache = eCache.setContainerWealth(value);
            uuidToEntityCacheMap.put(uuid, updatedCache);
        }
    }

    /**
     * Sorts entities by total wealth.
     *
     * @param hm hashmap of entity wealth to sort
     *
     * @return sorted total wealth hashmap
     */
    private HashMap<UUID, EntityCache> sortTotalWealthCache(ConcurrentHashMap<UUID,
            EntityCache> hm) {
        List<Map.Entry<UUID, EntityCache> > list =
                new LinkedList<>(hm.entrySet());

        list.sort((o1, o2) -> (o2.getValue().getTotalWealth())
                .compareTo(o1.getValue().getTotalWealth()));

        HashMap<UUID, EntityCache> temp = new LinkedHashMap<>();
        for (Map.Entry<UUID, EntityCache> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    /**
     * Set entity position and entity cache list for easy PAPI access.
     *
     * @param tempSortedCache hashmap to use to generate cache for
     */
    private void sortWealthCacheKeyValue(HashMap<UUID, EntityCache> tempSortedCache) {
        this.entityPositionCache = new ConcurrentHashMap<>();
        int i = 0;
        for (UUID uuid : tempSortedCache.keySet()) {
            this.entityPositionCache.put(uuid, i);
            i++;
        }
        this.entityCacheList = new ArrayList<>(tempSortedCache.values());
    }

    /**
     * Gets the name of an entity at given position.
     *
     * @param index position to get entity name at
     *
     * @return name of entity at specified position
     */
    public String getEntityNameAtPosition(int index) {
        EntityCache eCache = this.entityCacheList.get(index);
        UUID uuid = eCache.getUuid();

        if (uuid == null) {
            return "None";
        }

        if (this.main.groupIsEnabled()) {
            return this.groupUuidToNameMap.get(uuid);
        } else {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            return player.getName();
        }
    }

    /**
     * Gets the wealth of an entity at given position.
     *
     * @param index position to get entity wealth at
     *
     * @return wealth of entity at specified position
     */
    public String getEntityWealthAtPosition(int index) {
        EntityCache eCache = this.entityCacheList.get(index);
        Double value = eCache.getTotalWealth();

        if (value != null) {
            return String.format("%.02f", value);
        } else {
            return "None";
        }
    }

    /**
     * Gets the position of an entity with given name.
     *
     * @param uuid uuid of entity to get position for
     *
     * @return position of given entity
     */
    public String getPositionOfEntity(UUID uuid) {
        Integer position = this.entityPositionCache.get(uuid);
        if (position != null) {
            position = position + 1; // index 0
            return String.format("%d", position);
        } else {
            return "None";
        }
    }

    /**
     * Gets the balance wealth of an entity with given name.
     *
     * @param uuid uuid of entity to get balance wealth for
     *
     * @return balance wealth of given entity
     */
    public String getEntityBalWealth(UUID uuid) {
        EntityCache eCache = uuidToEntityCacheMap.get(uuid);
        return String.format("%.02f", eCache.getBalWealth());
    }

    /**
     * Gets the land wealth of an entity with given name.
     *
     * @param uuid uuid of entity to get land wealth for
     *
     * @return land wealth of given entity
     */
    public String getEntityLandWealth(UUID uuid) {
        EntityCache eCache = uuidToEntityCacheMap.get(uuid);
        return String.format("%.02f", eCache.getLandWealth());
    }

    /**
     * Gets the block wealth of an entity with given name.
     *
     * @param uuid uuid of entity to get block wealth for
     *
     * @return block wealth of given entity
     */
    public String getEntityBlockWealth(UUID uuid) {
        EntityCache eCache = uuidToEntityCacheMap.get(uuid);
        return String.format("%.02f", eCache.getBlockWealth());
    }

    /**
     * Gets the spawner wealth of an entity with given name.
     *
     * @param uuid uuid of entity to get spawner wealth for
     *
     * @return spawner wealth of given entity
     */
    public String getEntitySpawnerWealth(UUID uuid) {
        EntityCache eCache = uuidToEntityCacheMap.get(uuid);
        return String.format("%.02f", eCache.getSpawnerWealth());
    }

    /**
     * Gets the container wealth of an entity with given name.
     *
     * @param uuid uuid of entity to get container wealth for
     *
     * @return container wealth of given entity
     */
    public String getEntityContainerWealth(UUID uuid) {
        EntityCache eCache = uuidToEntityCacheMap.get(uuid);
        return String.format("%.02f", eCache.getContainerWealth());
    }

    /**
     * Gets the inventory wealth of an entity with given name.
     *
     * @param uuid uuid of entity to get inventory wealth for
     *
     * @return inventory wealth of given entity
     */
    public String getEntityInvWealth(UUID uuid) {
        EntityCache eCache = uuidToEntityCacheMap.get(uuid);
        return String.format("%.02f", eCache.getInventoryWealth());
    }

    /**
     * Gets the total wealth of an entity with given name.
     *
     * @param uuid uuid of entity to get total wealth for
     *
     * @return total wealth of given entity
     */
    public String getEntityTotalWealth(UUID uuid) {
        EntityCache eCache = uuidToEntityCacheMap.get(uuid);
        return String.format("%.02f", eCache.getTotalWealth());
    }

    /**
     * Returns a map of temporary uuid to name for groups.
     *
     * @return map of temporary uuid to group names
     */
    public HashMap<UUID, String> getGroupUuidToNameMap() {
        return this.groupUuidToNameMap;
    }

    /**
     * Returns a map of name to temporary uuid for groups.
     *
     * @return map of group names to temporary uuid
     */
    public HashMap<String, UUID> getGroupNameToUuidMap() {
        return this.groupNameToUuidMap;
    }
}

