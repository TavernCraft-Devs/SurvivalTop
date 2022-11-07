package tk.taverncraft.survivaltop.stats;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import org.kingdoms.utils.caffeine.checkerframework.checker.units.qual.C;
import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.storage.SqlHelper;
import tk.taverncraft.survivaltop.utils.MessageManager;

/**
 * ServerStatsManager handles all logic for updating of server-wide stats.
 */
public class ServerStatsManager {
    Main main;

    // cache values used for leaderboard/papi
    private ConcurrentHashMap<UUID, Double> entityTotalWealthCache;
    private ConcurrentHashMap<UUID, Double> entityLandWealthCache;
    private ConcurrentHashMap<UUID, Double> entityBalWealthCache;
    private HashMap<UUID, Integer> entityPositionCache;
    private ArrayList<UUID> entityTotalWealthKeys;
    private ArrayList<Double> entityTotalWealthValues;

    // temp hashmaps with pseudo uuids used only when entity is set to group
    private HashMap<UUID, String> groupUuidToNameMap;
    private HashMap<String, UUID> groupNameToUuidMap;

    /**
     * Constructor for ServerStatsManager.
     */
    public ServerStatsManager(Main main) throws NullPointerException {
        this.main = main;
        initializeValues();
    }

    /**
     * Initializes all values to default.
     */
    public void initializeValues() throws NullPointerException {
        entityTotalWealthCache = new ConcurrentHashMap<>();
        entityLandWealthCache = new ConcurrentHashMap<>();
        entityBalWealthCache = new ConcurrentHashMap<>();
        entityPositionCache = new HashMap<>();
        entityTotalWealthKeys = new ArrayList<>();
        entityTotalWealthValues = new ArrayList<>();
        groupUuidToNameMap = new HashMap<>();
        groupNameToUuidMap = new HashMap<>();
    }

    /**
     * Updates the entire leaderboard. May take a while to run if player-base is large.
     *
     * @param sender user executing the update
     */
    public void updateWealthStats(CommandSender sender) {
        try {
            boolean includeLandInWealth = main.getConfig().getBoolean("include-land", false);
            boolean includeBalInWealth = main.getConfig().getBoolean("include-bal", false);
            main.getLandManager().resetSenderLists();
            MessageManager.sendMessage(sender, "update-started");
            if (this.main.groupIsEnabled()) {
                updateForGroups(includeLandInWealth, includeBalInWealth);
            } else {
                updateForPlayers(includeLandInWealth, includeBalInWealth);
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    HashMap<UUID, Double> tempSpawnerCache = main.getLandManager().calculateSpawnerWorthForAll();
                    HashMap<UUID, Double> tempContainerCache = main.getLandManager().calculateContainerWorthForAll();
                    postUpdateProcessing(tempSpawnerCache, tempContainerCache, sender);
                }
            }.runTask(main);
        } catch (Exception e) {
            Bukkit.getLogger().info(e.getMessage());
            this.main.getLeaderboardManager().cancelAllTasks();
        }
    }

    private void updateForPlayers(boolean includeLandInWealth, boolean includeBalInWealth) {
        Arrays.stream(this.main.getServer().getOfflinePlayers()).forEach(offlinePlayer -> {
            double entityLandWorth = 0;
            double entityBalWorth = 0;
            try {
                if (includeLandInWealth) {
                    entityLandWorth = main.getLandManager().getLand(offlinePlayer.getUniqueId(),
                        offlinePlayer.getName(),
                        main.getLandManager().getBlockOperationsForAll());
                }
                if (includeBalInWealth) {
                    entityBalWorth = Main.getEconomy().getBalance(offlinePlayer);
                }
            } catch (Exception | NoClassDefFoundError e) {
                // vault might throw an error here related to null user, remove when resolved
            }
            UUID uuid = offlinePlayer.getUniqueId();
            entityLandWealthCache.put(uuid, entityLandWorth);
            entityBalWealthCache.put(uuid, entityBalWorth);

            entityTotalWealthCache.put(uuid, entityLandWorth + entityBalWorth);
            main.getStorageManager().getStorageHelper().saveToStorage(uuid, entityLandWorth, entityBalWorth);
        });
    }

    private void updateForGroups(boolean includeLandInWealth, boolean includeBalInWealth) {
        // reset cache on each update since unlike players, group uuids are temporary
        for (Map.Entry<UUID, String> set : groupUuidToNameMap.entrySet()) {
            this.entityTotalWealthCache.remove(set.getKey());
            this.entityBalWealthCache.remove(set.getKey());
            this.entityLandWealthCache.remove(set.getKey());
        }
        this.groupUuidToNameMap = new HashMap<>();
        this.groupNameToUuidMap = new HashMap<>();

        List<String> groups = this.main.getGroupManager().getGroups();
        for (String group : groups) {
            UUID tempUuid = UUID.randomUUID();
            groupUuidToNameMap.put(tempUuid, group);
            groupNameToUuidMap.put(group, tempUuid);
            double entityLandWorth = 0;
            double entityBalWorth = 0;
            try {
                if (includeLandInWealth) {
                    entityLandWorth = main.getLandManager().getLand(tempUuid,
                        group,
                        main.getLandManager().getBlockOperationsForAll());
                }
                if (includeBalInWealth) {
                    try {
                        double totalBalance = 0;
                        List<OfflinePlayer> offlinePlayers = this.main.getGroupManager().getPlayers(group);
                        for (OfflinePlayer offlinePlayer : offlinePlayers) {
                            totalBalance += Main.getEconomy().getBalance(offlinePlayer);
                        }
                        entityBalWorth = totalBalance;
                    } catch (NoClassDefFoundError | NullPointerException e) {
                        entityBalWorth = 0;
                    }
                }
            } catch (Exception | NoClassDefFoundError e) {
                // vault might throw an error here related to null user, remove when resolved
            }
            entityLandWealthCache.put(tempUuid, entityLandWorth);
            entityBalWealthCache.put(tempUuid, entityBalWorth);

            entityTotalWealthCache.put(tempUuid, entityLandWorth + entityBalWorth);
            main.getStorageManager().getStorageHelper().saveToStorage(tempUuid, entityLandWorth, entityBalWorth);
        }
    }

    /**
     * Cleans up after values of entities are retrieved and updates the relevant caches. Also
     * updates spawner values if applicable.
     */
    private void postUpdateProcessing(HashMap<UUID, Double> tempSpawnerCache, HashMap<UUID,
        Double> tempContainerCache, CommandSender sender) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, Double> map : tempSpawnerCache.entrySet()) {
                    UUID uuid = map.getKey();
                    double value = map.getValue();
                    double newValue = value + entityLandWealthCache.get(uuid);
                    double balValue = entityBalWealthCache.get(uuid);
                    double totalValue = newValue + balValue;
                    entityLandWealthCache.put(uuid, newValue);
                    entityTotalWealthCache.put(uuid, totalValue);
                    main.getStorageManager().getStorageHelper().saveToStorage(uuid, newValue, balValue);
                }

                for (Map.Entry<UUID, Double> map : tempContainerCache.entrySet()) {
                    UUID uuid = map.getKey();
                    double value = map.getValue();
                    double newValue = value + entityLandWealthCache.get(uuid);
                    double balValue = entityBalWealthCache.get(uuid);
                    double totalValue = newValue + balValue;
                    entityLandWealthCache.put(uuid, newValue);
                    entityTotalWealthCache.put(uuid, totalValue);
                    main.getStorageManager().getStorageHelper().saveToStorage(uuid, newValue, balValue);
                }

                if (main.getStorageManager().getStorageType().equalsIgnoreCase("mysql")) {
                    new SqlHelper(main).insertIntoDatabase();
                }

                HashMap<UUID, Double> tempSortedCache = sortTotalWealthCache(entityTotalWealthCache);
                sortWealthCacheKeyValue(tempSortedCache);
                main.getLandManager().resetSenderLists();
                main.getLeaderboardManager().completeLeaderboardUpdate(sender, tempSortedCache);
            }
        }.runTaskLaterAsynchronously(main, 0);
    }

    /**
     * Sorts entities by total wealth.
     *
     * @param hm hashmap of entity wealth to sort
     */
    private HashMap<UUID, Double> sortTotalWealthCache(ConcurrentHashMap<UUID, Double> hm) {
        List<Map.Entry<UUID, Double> > list =
                new LinkedList<>(hm.entrySet());

        list.sort((o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));

        HashMap<UUID, Double> temp = new LinkedHashMap<>();
        for (Map.Entry<UUID, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    /**
     * Sorts leaderboard array list for easy papi access.
     */
    private void sortWealthCacheKeyValue(HashMap<UUID, Double> tempSortedCache) {
        Set<UUID> keySet = tempSortedCache.keySet();
        this.entityPositionCache = new HashMap<>();
        int i = 0;
        for (Map.Entry<UUID, Double> set : tempSortedCache.entrySet()) {
            this.entityPositionCache.put(set.getKey(), i);
            i++;
        }
        this.entityTotalWealthKeys = new ArrayList<>(keySet);
        Collection<Double> values = tempSortedCache.values();
        this.entityTotalWealthValues = new ArrayList<>(values);
    }

    public String getEntityNameAtPosition(int index) {
        UUID uuid = this.entityTotalWealthKeys.get(index);

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

    public String getEntityWealthAtPosition(int index) {
        Double value = this.entityTotalWealthValues.get(index);
        if (value != null) {
            return String.format("%.02f", value);
        } else {
            return "None";
        }
    }

    public String getPositionOfEntity(String entityName) {
        UUID uuid = this.groupNameToUuidMap.get(entityName);
        Integer position = this.entityPositionCache.get(uuid);
        if (position != null) {
            position = position + 1; // index 0
            return String.format("%d", position);
        } else {
            return "None";
        }
    }

    public String getEntityTotalWealth(String entityName) {
        if (this.main.groupIsEnabled()) {
            UUID uuid = this.groupNameToUuidMap.get(entityName);
            return String.format("%.02f", entityTotalWealthCache.get(uuid));
        } else {
            OfflinePlayer player = Bukkit.getOfflinePlayer(entityName);
            return String.format("%.02f", entityTotalWealthCache.get(player.getUniqueId()));
        }
    }

    public String getEntityLandWealth(String entityName) {
        if (this.main.groupIsEnabled()) {
            UUID uuid = this.groupNameToUuidMap.get(entityName);
            return String.format("%.02f", entityLandWealthCache.get(uuid));
        } else {
            OfflinePlayer player = Bukkit.getOfflinePlayer(entityName);
            return String.format("%.02f", entityLandWealthCache.get(player.getUniqueId()));
        }
    }

    public String getEntityBalWealth(String entityName) {
        if (this.main.groupIsEnabled()) {
            UUID uuid = this.groupNameToUuidMap.get(entityName);
            return String.format("%.02f", entityBalWealthCache.get(uuid));
        } else {
            OfflinePlayer player = Bukkit.getOfflinePlayer(entityName);
            return String.format("%.02f", entityBalWealthCache.get(player.getUniqueId()));
        }
    }

    public HashMap<UUID, String> getGroupUuidToNameMap() {
        return this.groupUuidToNameMap;
    }
}

