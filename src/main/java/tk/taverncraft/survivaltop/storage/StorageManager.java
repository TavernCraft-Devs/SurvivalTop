package tk.taverncraft.survivaltop.storage;

import java.util.ArrayList;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.stats.cache.EntityLeaderboardCache;

/**
 * StorageManager decides the storage helper to use.
 */
public class StorageManager {
    private final Main main;
    private StorageHelper storageHelper;

    /**
     * Constructor for StorageManager.
     *
     * @param main plugin class
     */
    public StorageManager(Main main) {
        this.main = main;
        initializeValues();
    }

    /**
     * Initializes all values to default and set storage type.
     */
    public void initializeValues() {
        String storageType = main.getConfig().getString("storage-type", "none")
                .toLowerCase();
        if (storageType.equals("mysql")) {
            storageHelper = new SqlHelper(main);
        } else if (storageType.equals("yaml")) {
            storageHelper = new YamlHelper(main);
        }
    }

    /**
     * Calls the helper to save information to storage.
     *
     * @param entityLeaderboardCacheList list of entities to store
     */
    public void saveToStorage(ArrayList<EntityLeaderboardCache> entityLeaderboardCacheList) {
        // if null means no storage was initialized, so no need to save
        if (this.storageHelper == null) {
            return;
        }
        this.storageHelper.saveToStorage(entityLeaderboardCacheList);
    }
}
