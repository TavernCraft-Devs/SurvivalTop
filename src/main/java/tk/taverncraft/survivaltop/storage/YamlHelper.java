package tk.taverncraft.survivaltop.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.cache.EntityCache;

/**
 * YamlHelper is responsible for reading/writing from yml files.
 */
public class YamlHelper implements StorageHelper {
    private final Main main;

    /**
     * Constructor for YamlHelper.
     *
     * @param main plugin class
     */
    public YamlHelper(Main main) {
        this.main = main;
    }

    /**
     * Saves information to yaml file.
     *
     * @param EntityCacheList list of entities to store
     */
    public void saveToStorage(ArrayList<EntityCache> EntityCacheList) {
        int cacheSize = EntityCacheList.size();
        for (int i = 0; i < cacheSize; i++) {
            EntityCache eCache = EntityCacheList.get(i);
            saveToFile(eCache);
        }
    }

    /**
     * Saves individual entities to file.
     *
     * @param eCache entity to save
     */
    private void saveToFile(EntityCache eCache) {
        String entityName = eCache.getName();
        String entityType = "player";
        if (this.main.getOptions().groupIsEnabled()) {
            entityType = "group";
        }
        File entityFile = new File(this.main.getDataFolder() + "/entityData",
            entityName + ".yml");
        FileConfiguration entityConfig = new YamlConfiguration();
        if (!entityFile.exists()) {
            entityFile.getParentFile().mkdirs();
        } else {
            try {
                entityConfig.load(entityFile);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
        entityConfig.set("entity-name", entityName);
        entityConfig.set("entity-type", entityType);
        for (Map.Entry<String, Double> map : eCache.getWealthBreakdown().entrySet()) {
            entityConfig.set(map.getKey(), map.getValue());
        }

        try {
            entityConfig.save(entityFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
