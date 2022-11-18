package tk.taverncraft.survivaltop.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.logs.LogFile;
import tk.taverncraft.survivaltop.messages.MessageManager;

/**
 * ConfigManager handles the loading of all configuration files.
 */
public class ConfigManager {
    private final Main main;

    /**
     * Constructor for ConfigManager.
     *
     * @param main plugin class
     */
    public ConfigManager(Main main) {
        this.main = main;
    }

    /**
     * Creates all configuration files from the plugin.
     */
    public void createConfigs() {
        createConfig();
        createMessageFile();
        createBlocksConfig();
        createSpawnersConfig();
        createContainersConfig();
        createInventoriesConfig();
        createSignsConfig();
    }

    /**
     * Creates config file.
     */
    public void createConfig() {
        FileConfiguration config = getConfig("config.yml");
        main.setConfig(config);
    }

    /**
     * Creates blocks config file.
     */
    public void createBlocksConfig() {
        FileConfiguration config = getConfig("blocks.yml");
        main.setBlocksConfig(config);
    }

    /**
     * Creates spawners config file.
     */
    public void createSpawnersConfig() {
        FileConfiguration config = getConfig("spawners.yml");
        main.setSpawnersConfig(config);
    }

    /**
     * Creates containers config file.
     */
    public void createContainersConfig() {
        FileConfiguration config = getConfig("containers.yml");
        main.setContainersConfig(config);
    }

    /**
     * Creates inventories config file.
     */
    public void createInventoriesConfig() {
        FileConfiguration config = getConfig("inventories.yml");
        main.setInventoriesConfig(config);
    }

    /**
     * Creates signs config file.
     */
    public void createSignsConfig() {
        FileConfiguration config = getConfig("signs.yml");
        main.setSignsConfig(config);
    }

    /**
     * Creates message file.
     */
    public void createMessageFile() {
        String langFileName = main.getConfig().getString("lang-file");

        // default language
        if (langFileName == null) {
            langFileName = "en.yml";
        }

        File langFile = new File(main.getDataFolder() + "/lang", langFileName);
        FileConfiguration lang = new YamlConfiguration();
        if (!langFile.exists()) {
            langFile.getParentFile().mkdirs();
            try {
                Path dest = Paths.get(main.getDataFolder() + "/lang/" + langFileName);
                Files.copy(main.getResource(langFileName), dest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            lang.load(langFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        MessageManager.setMessages(lang);
    }

    /**
     * Gets the configuration file with given name.
     *
     * @param configName name of config file
     *
     * @return file configuration for config
     */
    private FileConfiguration getConfig(String configName) {
        File configFile = new File(main.getDataFolder(), configName);
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            main.saveResource(configName, false);
        }

        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        return config;
    }

    /**
     * Dumps details into a log file, triggered by the dump command.
     *
     * @param logFile log file containing log details from plugin
     */
    public void dumpToLogFile(LogFile logFile) {
        String fileName = "dump-" + Instant.now().getEpochSecond() + ".yml";
        File configFile = new File(main.getDataFolder() + "/dumps", fileName);
        FileConfiguration config = new YamlConfiguration();
        configFile.getParentFile().mkdirs();

        // logs from plugin
        config.set("minecraft-version", logFile.getMinecraftVersion());
        config.set("survivalTop-version", logFile.getSurvivalTopVersion());
        config.set("world-size", logFile.getWorldSize());
        config.set("num-entities", logFile.getNumEntities());
        config.set("num-claims", logFile.getNumClaims());
        config.set("num-blocks", logFile.getNumBlocks());
        config.set("leaderboard-update-start-time", logFile.getLeaderboardUpdateStartTime());
        config.set("last-update-duration", logFile.getLastUpdateDuration());
        config.set("estimated-block-processing-rate", logFile.getEstimatedBlockProcessingRate());

        // config options
        config.set("use-gui-stats", main.getOptions().isUseGuiStats());
        config.set("use-realtime-stats", main.getOptions().isUseRealTimeStats());
        config.set("filter-last-join", main.getOptions().filterLastJoin());
        config.set("filter-player-time", main.getOptions().filterPlayerTime());
        config.set("enable-group", main.getOptions().groupIsEnabled());
        config.set("group-type", main.getOptions().getGroupType());
        config.set("include-bal", main.getOptions().balIsIncluded());
        config.set("include-land", main.getOptions().landIsIncluded());
        config.set("land-type", main.getOptions().getLandType());
        config.set("include-spawners", main.getOptions().spawnerIsIncluded());
        config.set("include-containers", main.getOptions().containerIsIncluded());
        config.set("max-land-height", main.getOptions().getMaxLandHeight());
        config.set("min-land-height", main.getOptions().getMinLandHeight());
        config.set("include-inventory", main.getOptions().inventoryIsIncluded());
        config.set("update-interval", main.getOptions().getUpdateInterval());
        config.set("update-on-start", main.getOptions().updateOnStart());
        config.set("minimum-wealth", main.getOptions().getMinimumWealth());
        config.set("storage-type", main.getOptions().getStorageType());
        config.set("last-load-time", main.getOptions().getLastLoadTime());

        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
