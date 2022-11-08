package tk.taverncraft.survivaltop.storage;

import tk.taverncraft.survivaltop.Main;

/**
 * StorageManager decides the storage helper to use.
 */
public class StorageManager {
    private Main main;
    private String storageType;
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
        storageType = main.getConfig().getString("storage-type", "none");
        if (storageType.equalsIgnoreCase("yaml")) {
            storageHelper = new YamlHelper(main);
        } else if (storageType.equalsIgnoreCase("mysql")) {
            storageHelper = new SqlHelper(main);
        } else {
            storageHelper = new NoneHelper();
        }
    }

    /**
     * Gets the storage helper.
     *
     * @return storage helper
     */
    public StorageHelper getStorageHelper() {
        return this.storageHelper;
    }

    /**
     * Gets the storage type.
     *
     * @return storage type
     */
    public String getStorageType() {
        return this.storageType;
    }
}
