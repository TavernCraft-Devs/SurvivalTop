package tk.taverncraft.survivaltop.storage;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import tk.taverncraft.survivaltop.Main;

public class StorageManager {
    Main main;
    private String storageType;
    StorageHelper storageHelper;

    public StorageManager(Main main) {
        this.main = main;
        initializeValues();
    }

    public void initializeValues() {
        storageType = main.getConfig().getString("storage-type", "none");
        if (storageType.equalsIgnoreCase("yaml")) {
            storageHelper = new YamlHelper(main);
        } else if (storageType.equalsIgnoreCase("mysql")) {
            storageHelper = new SqlHelper(main);
        } else {
            storageHelper = new NoneHelper(main);
        }
    }

    public StorageHelper getStorageHelper() {
        return this.storageHelper;
    }

    public String getStorageType() {
        return this.storageType;
    }

    /**
     * Get entity data and create if not exist.
     *
     * @param uuid uuid of entity to get data for
     */
    public FileConfiguration getEntityData(UUID uuid) {
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
        File entityFile = new File(main.getDataFolder() + "/entityData", entityFileName + ".yml");
        FileConfiguration entityConfig = new YamlConfiguration();
        if (!entityFile.exists()) {
            entityFile.getParentFile().mkdirs();
            entityConfig.set("entity-name", entityName);
            entityConfig.set("entity-type", entityType);
            entityConfig.set("land-wealth", 0);
            entityConfig.set("bal-wealth", 0);
            try {
                entityConfig.save(entityFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            entityConfig.load(entityFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return entityConfig;
    }
}
