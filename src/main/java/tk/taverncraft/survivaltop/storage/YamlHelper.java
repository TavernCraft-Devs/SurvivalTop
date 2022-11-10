package tk.taverncraft.survivaltop.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.stats.cache.EntityCache;

/**
 * YamlHelper is responsible for reading/writing from yml files.
 */
public class YamlHelper implements StorageHelper {
    private Main main;

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
     * @param entityCacheList list of entities to store
     */
    public void saveToStorage(ArrayList<EntityCache> entityCacheList) {
        for (EntityCache eCache : entityCacheList) {
            saveToFile(eCache);
        }
    }

    /**
     * Saves individual entities to file.
     *
     * @param eCache entity to save
     */
    private void saveToFile(EntityCache eCache) {
        UUID uuid = eCache.getUuid();
        String entityFileName;
        String entityName = "None";
        String entityType = "player";
        if (this.main.groupIsEnabled()) {
            entityFileName = this.main.getServerStatsManager().getGroupUuidToNameMap().get(uuid);
            entityName = entityFileName;
            entityType = "group";
        } else {
            entityFileName = uuid.toString();
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            if (player != null) {
                entityName = player.getName();
            }
        }
        File entityFile = new File(this.main.getDataFolder() + "/entityData",
            entityFileName + ".yml");
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
        entityConfig.set("bal-wealth", eCache.getBalWealth());
        entityConfig.set("land-wealth", eCache.getLandWealth());
        entityConfig.set("block-wealth", eCache.getBlockWealth());
        entityConfig.set("spawner-wealth", eCache.getSpawnerWealth());
        entityConfig.set("container-wealth", eCache.getContainerWealth());
        entityConfig.set("inv-wealth", eCache.getInventoryWealth());
        entityConfig.set("total-wealth", eCache.getTotalWealth());

        try {
            entityConfig.save(entityFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
