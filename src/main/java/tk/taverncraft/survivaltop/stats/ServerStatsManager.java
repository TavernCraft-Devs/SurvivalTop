package tk.taverncraft.survivaltop.stats;

import java.time.Instant;
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
import tk.taverncraft.survivaltop.stats.cache.EntityCache;
import tk.taverncraft.survivaltop.ui.LeaderboardGui;
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

    // leaderboard gui, for upcoming update
    private HashMap<UUID, LeaderboardGui> leaderboardGui = new HashMap<>();

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
     * again (if applicable).
     *
     * @param sender user executing the update
     */
    private void processSpawnersAndContainers(CommandSender sender) {
        new BukkitRunnable() {
            @Override
            public void run() {
                main.getLandManager().processSpawnerTypesForLeaderboard();
                main.getLandManager().processContainerItemsForLeaderboard();
                executePostUpdateActions(sender);
            }
        }.runTask(main);
    }

    /**
     * Performs update by individual players.
     */
    private void updateForPlayers() {
        boolean filterLastJoin = this.main.getConfig().getBoolean("filter-last-join", false);
        long lastJoinTime = this.main.getConfig().getLong("last-join-time", 2592000) * 1000;

        // code intentionally duplicated to keep the if condition outside loop to save check time

        // path for if last join filter is off or if last join time is set <= 0 (cannot filter)
        if (!filterLastJoin || lastJoinTime <= 0) {
            Arrays.stream(this.main.getServer().getOfflinePlayers()).forEach(offlinePlayer -> {
                calculateAndCacheEntities(offlinePlayer.getUniqueId(), offlinePlayer.getName());
            });
            return;
        }

        // path for if last join filter is on
        Instant instant = Instant.now();
        long currentTime = instant.getEpochSecond() * 1000;
        Arrays.stream(this.main.getServer().getOfflinePlayers()).forEach(offlinePlayer -> {
            if (currentTime - offlinePlayer.getLastPlayed() > lastJoinTime) {
                return;
            }
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
        int groupSize = groups.size();
        for (int i = 0; i < groupSize; i++) {
            String group = groups.get(i);
            UUID tempUuid = UUID.randomUUID();
            groupUuidToNameMap.put(tempUuid, group);
            groupNameToUuidMap.put(group, tempUuid);
            calculateAndCacheEntities(tempUuid, group);
        }
    }

    /**
     * Calculates the worth for each entity and caches them.
     *
     * @param uuid uuid of sender if this is run through stats command; otherwise entities
     * @param name name of entity to calculate for
     */
    private void calculateAndCacheEntities(UUID uuid, String name) {
        double entityBalWorth = 0;
        if (main.landIsIncluded()) {
            // land calculations are done async and will be retrieved later
            main.getLandManager().createHoldersForLeaderboard(uuid);
            main.getLandManager().processEntityLand(uuid, name, true);
        }
        if (main.balIsIncluded()) {
            entityBalWorth = main.getBalanceManager().getBalanceForEntity(name);
        }
        if (main.inventoryIsIncluded()) {
            main.getInventoryManager().createHolderForLeaderboard(uuid);
            main.getInventoryManager().getInventoryWorthForLeaderboard(uuid, name);
        }
        EntityCache eCache = new EntityCache(uuid, entityBalWorth);
        uuidToEntityCacheMap.put(uuid, eCache);
    }

    /**
     * Cleans up after values of entities are retrieved and updates the relevant caches. Also
     * updates spawner/container values if applicable.
     *
     * @param sender sender who initiated the leaderboard update
     */
    private void executePostUpdateActions(CommandSender sender) {
        new BukkitRunnable() {
            @Override
            public void run() {
                executePostUpdateBlocks(main.getLandManager().calculateBlockWorthForLeaderboard());
                executePostUpdateSpawners(main.getLandManager().calculateSpawnerWorthForLeaderboard());
                executePostUpdateContainers(main.getLandManager().calculateContainerWorthForLeaderboard());
                executePostUpdateInventories(main.getInventoryManager().calculateInventoryWorthForLeaderboard());
                HashMap<UUID, EntityCache> tempSortedCache = sortEntitiesByTotalWealth(uuidToEntityCacheMap);
                setUpEntityCache(tempSortedCache);
                main.getLeaderboardManager().completeLeaderboardUpdate(sender, tempSortedCache);
                main.getStorageManager().saveToStorage(entityCacheList);
            }
        }.runTaskLaterAsynchronously(main, 0);
    }

    /**
     * Updates the values of spawners after they are processed on the main thread.
     *
     * @param tempBlockCache spawner cache to update from
     */
    private void executePostUpdateBlocks(HashMap<UUID, Double> tempBlockCache) {
        for (Map.Entry<UUID, Double> map : tempBlockCache.entrySet()) {
            uuidToEntityCacheMap.get(map.getKey()).setBlockWealth(map.getValue());
        }
    }

    /**
     * Updates the values of spawners after they are processed on the main thread.
     *
     * @param tempSpawnerCache spawner cache to update from
     */
    private void executePostUpdateSpawners(HashMap<UUID, Double> tempSpawnerCache) {
        for (Map.Entry<UUID, Double> map : tempSpawnerCache.entrySet()) {
            uuidToEntityCacheMap.get(map.getKey()).setSpawnerWealth(map.getValue());
        }
    }

    /**
     * Updates the values of containers after they are processed on the main thread.
     *
     * @param tempContainerCache container cache to update from
     */
    private void executePostUpdateContainers(HashMap<UUID, Double> tempContainerCache) {
        for (Map.Entry<UUID, Double> map : tempContainerCache.entrySet()) {
            uuidToEntityCacheMap.get(map.getKey()).setContainerWealth(map.getValue());
        }
    }

    /**
     * Updates the values of inventories.
     *
     * @param tempInventoryCache inventory cache to update from
     */
    private void executePostUpdateInventories(HashMap<UUID, Double> tempInventoryCache) {
        for (Map.Entry<UUID, Double> map : tempInventoryCache.entrySet()) {
            uuidToEntityCacheMap.get(map.getKey()).setInventoryWealth(map.getValue());
        }
    }

    /**
     * Sorts entities by total wealth.
     *
     * @param hm hashmap of entity wealth to sort
     *
     * @return sorted total wealth hashmap
     */
    private HashMap<UUID, EntityCache> sortEntitiesByTotalWealth(ConcurrentHashMap<UUID,
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
    private void setUpEntityCache(HashMap<UUID, EntityCache> tempSortedCache) {
        this.entityPositionCache = new ConcurrentHashMap<>();
        int i = 0;
        for (UUID uuid : tempSortedCache.keySet()) {
            this.entityPositionCache.put(uuid, i);
            i++;
        }
        this.entityCacheList = new ArrayList<>(tempSortedCache.values());
    }

    /**
     * Gets the entity cache with given name.
     *
     * @param name name of entity to get cache for
     *
     * @return entity cache for the given name
     */
    public EntityCache getEntityCache(String name) {
        UUID uuid;
        if (main.groupIsEnabled()) {
            uuid = this.groupNameToUuidMap.get(name);
            if (uuid == null) {
                return null;
            }
        } else {
            OfflinePlayer player = Bukkit.getOfflinePlayer(name);
            if (player == null) {
                return null;
            }
            uuid = player.getUniqueId();
        }
        return this.uuidToEntityCacheMap.get(uuid);
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

