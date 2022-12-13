package tk.taverncraft.survivaltop.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tk.taverncraft.survivaltop.Main;

public class CacheManager {
    private final Main main;

    // stats cache
    private ConcurrentHashMap<String, EntityCache> statsCacheMap;

    // leaderboard cache
    private ConcurrentHashMap<String, EntityCache> leaderboardCacheMap;

    // maps below are used for papi
    private ConcurrentHashMap<String, EntityCache> entityCacheMapCopy;

    // cached positions
    private ConcurrentHashMap<String, Integer> positionCacheMap;
    private ArrayList<EntityCache> entityCacheList;

    public CacheManager(Main main) {
        this.main = main;
        initializeValues();
    }

    public void initializeValues() {
        statsCacheMap = new ConcurrentHashMap<>();
        leaderboardCacheMap = new ConcurrentHashMap<>();
        positionCacheMap = new ConcurrentHashMap<>();
        entityCacheMapCopy = new ConcurrentHashMap<>();
        entityCacheList = new ArrayList<>();
    }

    public void saveToStatsCache(String name, EntityCache eCache) {
        statsCacheMap.put(name, eCache);
    }

    public void saveToLeaderboardCache(String name, EntityCache eCache) {
        leaderboardCacheMap.put(name, eCache);
    }

    public EntityCache getCacheAtPosition(int position) {
        return entityCacheList.get(position);
    }

    public void processLeaderboardCache() {
        setUpEntityCache();
        entityCacheMapCopy = leaderboardCacheMap;
        main.getStorageManager().saveToStorage(entityCacheList);
    }
    
    public ArrayList<EntityCache> getEntityCacheList() {
        return entityCacheList;
    }

    /**
     * Sorts entities by total wealth and filters for total leaderboard position limit shown
     * if applicable.
     *
     * @return sorted total wealth hashmap
     */
    private HashMap<String, EntityCache> getSortedCache() {
        List<Map.Entry<String, EntityCache> > list =
            new LinkedList<>(leaderboardCacheMap.entrySet());

        list.sort((o1, o2) -> (o2.getValue().getTotalWealth())
            .compareTo(o1.getValue().getTotalWealth()));

        HashMap<String, EntityCache> temp = new LinkedHashMap<>();
        int totalLeaderboardPositions = main.getOptions().getTotalLeaderboardPositions();
        if (totalLeaderboardPositions >= 0) {
            for (Map.Entry<String, EntityCache> aa : list) {
                if (totalLeaderboardPositions == 0) {
                    break;
                }
                temp.put(aa.getKey(), aa.getValue());
                totalLeaderboardPositions--;
            }
        } else {
            for (Map.Entry<String, EntityCache> aa : list) {
                temp.put(aa.getKey(), aa.getValue());
            }
        }
        return temp;
    }

    /**
     * Sets entity position and entity cache list for easy papi access.
     */
    private void setUpEntityCache() {
        HashMap<String, EntityCache> tempSortedCache = getSortedCache();
        this.positionCacheMap = new ConcurrentHashMap<>();
        int i = 0;
        for (String nameKey : tempSortedCache.keySet()) {
            this.positionCacheMap.put(nameKey, i);
            i++;
        }
        this.entityCacheList = new ArrayList<>(tempSortedCache.values());
    }

    /**
     * Gets the latest valid cache.
     *
     * @param name name of entity
     *
     * @return latest cache for entity or null if none are found
     */
    public EntityCache getLatestCache(String name) {
        EntityCache statsCache = statsCacheMap.get(name);
        EntityCache leaderboardCache = leaderboardCacheMap.get(name);

        if (statsCache.isExpired(main.getOptions().getCacheDuration())) {
            statsCache = null;
        }

        if (leaderboardCache.isExpired(main.getOptions().getCacheDuration())) {
            leaderboardCache = null;
        }

        if (statsCache != null && leaderboardCache != null) {
            if (statsCache.getCacheTime() > leaderboardCache.getCacheTime()) {
                return statsCache;
            } else {
                return leaderboardCache;
            }
        }

        if (statsCache != null) {
            return statsCache;
        }

        return leaderboardCache;
    }

    // functions below are called by the papi manager to retrieve leaderboard values

    /**
     * Gets the name of an entity at given position.
     *
     * @param index position to get entity name at
     *
     * @return name of entity at specified position
     */
    public String getEntityNameAtPosition(int index) {
        EntityCache eCache = this.entityCacheList.get(index);
        String name = eCache.getName();

        if (name == null) {
            return "None";
        }

        return name;
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
     * @param name of entity
     *
     * @return position of given entity
     */
    public String getPositionOfEntity(String name) {
        Integer position = this.positionCacheMap.get(name);
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
     * @param name of entity
     *
     * @return balance wealth of given entity
     */
    public String getEntityBalWealth(String name) {
        EntityCache eCache = entityCacheMapCopy.get(name);
        return String.format("%.02f", eCache.getBalWealth());
    }

    /**
     * Gets the land wealth of an entity with given name.
     *
     * @param name of entity
     *
     * @return land wealth of given entity
     */
    public String getEntityLandWealth(String name) {
        EntityCache eCache = entityCacheMapCopy.get(name);
        return String.format("%.02f", eCache.getLandWealth());
    }

    /**
     * Gets the block wealth of an entity with given name.
     *
     * @param name of entity
     *
     * @return block wealth of given entity
     */
    public String getEntityBlockWealth(String name) {
        EntityCache eCache = entityCacheMapCopy.get(name);
        return String.format("%.02f", eCache.getBlockWealth());
    }

    /**
     * Gets the spawner wealth of an entity with given name.
     *
     * @param name of entity
     *
     * @return spawner wealth of given entity
     */
    public String getEntitySpawnerWealth(String name) {
        EntityCache eCache = entityCacheMapCopy.get(name);
        return String.format("%.02f", eCache.getSpawnerWealth());
    }

    /**
     * Gets the container wealth of an entity with given name.
     *
     * @param name of entity
     *
     * @return container wealth of given entity
     */
    public String getEntityContainerWealth(String name) {
        EntityCache eCache = entityCacheMapCopy.get(name);
        return String.format("%.02f", eCache.getContainerWealth());
    }

    /**
     * Gets the inventory wealth of an entity with given name.
     *
     * @param name name of entity
     *
     * @return inventory wealth of given entity
     */
    public String getEntityInvWealth(String name) {
        EntityCache eCache = entityCacheMapCopy.get(name);
        return String.format("%.02f", eCache.getInventoryWealth());
    }

    /**
     * Gets the total wealth of an entity with given name.
     *
     * @param name of entity
     *
     * @return total wealth of given entity
     */
    public String getEntityTotalWealth(String name) {
        EntityCache eCache = entityCacheMapCopy.get(name);
        return String.format("%.02f", eCache.getTotalWealth());
    }
}
