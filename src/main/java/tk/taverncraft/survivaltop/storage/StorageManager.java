package tk.taverncraft.survivaltop.storage;

import java.util.ArrayList;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.stats.EntityCache;

/**
 * StorageManager decides the storage helper to use.
 */
public class StorageManager {
    private Main main;
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
     * @param entityCacheList list of entities to store
     */
    public void saveToStorage(ArrayList<EntityCache> entityCacheList) {
        this.storageHelper.saveToStorage(entityCacheList);
    }
}
