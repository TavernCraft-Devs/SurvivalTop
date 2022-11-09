package tk.taverncraft.survivaltop.storage;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import tk.taverncraft.survivaltop.Main;

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
     * Updates the stats of an entity into file.
     *
     * @param uuid uuid of entity to update stats for
     * @param landWealth the amount of wealth calculated from land
     * @param balWealth the amount of wealth calculated from balance
     * @param invWealth the amount of wealth calculated from inventory
     */
    public void saveToStorage(UUID uuid, double landWealth, double balWealth, double invWealth) {
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
        entityConfig.set("land-wealth", landWealth);
        entityConfig.set("bal-wealth", balWealth);
        entityConfig.set("inv-wealth", invWealth);

        try {
            entityConfig.save(entityFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
