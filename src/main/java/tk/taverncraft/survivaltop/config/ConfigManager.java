package tk.taverncraft.survivaltop.config;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.messages.MessageManager;

/**
 * ConfigManager handles the loading of all configuration files.
 */
public class ConfigManager {
    private final Main main;

    // config
    private FileConfiguration config;
    private FileConfiguration blocksConfig;
    private FileConfiguration spawnersConfig;
    private FileConfiguration containersConfig;
    private FileConfiguration inventoriesConfig;
    private FileConfiguration papiConfig;
    private FileConfiguration statsMenuConfig;
    private FileConfiguration infoMenuConfig;
    private FileConfiguration signsConfig;

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
        createPapiConfig();
        createStatsMenuConfig();
        createInfoMenuConfig();
        createSignsConfig();
    }

    /**
     * Creates config file.
     */
    public void createConfig() {
        config = getConfig("config.yml");
    }

    /**
     * Creates message lang file.
     */
    public void createMessageFile() {
        String langFileName = main.getConfig().getString("lang-file");
        FileConfiguration langConfig = getConfig("lang/" + langFileName);
        MessageManager.setMessages(langConfig);
    }

    /**
     * Creates blocks config file.
     */
    public void createBlocksConfig() {
        blocksConfig = getConfig("calculations/blocks.yml");
    }

    /**
     * Creates spawners config file.
     */
    public void createSpawnersConfig() {
        spawnersConfig = getConfig("calculations/spawners.yml");
    }

    /**
     * Creates containers config file.
     */
    public void createContainersConfig() {
        containersConfig = getConfig("calculations/containers.yml");
    }

    /**
     * Creates inventories config file.
     */
    public void createInventoriesConfig() {
        inventoriesConfig = getConfig("calculations/inventories.yml");
    }

    /**
     * Creates papi config file.
     */
    public void createPapiConfig() {
        papiConfig = getConfig("calculations/papi.yml");
    }

    /**
     * Creates stats menu config file.
     */
    public void createStatsMenuConfig() {
        statsMenuConfig = getConfig("menu/stats.yml");
    }

    /**
     * Creates info menu config file.
     */
    public void createInfoMenuConfig() {
        infoMenuConfig = getConfig("menu/info.yml");
    }

    /**
     * Creates signs config file.
     */
    public void createSignsConfig() {
        signsConfig = getConfig("dat/signs.yml");
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

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getBlocksConfig() {
        return blocksConfig;
    }

    public FileConfiguration getSpawnersConfig() {
        return spawnersConfig;
    }

    public FileConfiguration getContainersConfig() {
        return containersConfig;
    }

    public FileConfiguration getInventoriesConfig() {
        return inventoriesConfig;
    }

    public FileConfiguration getPapiConfig() {
        return papiConfig;
    }

    public FileConfiguration getStatsMenuConfig() {
        return statsMenuConfig;
    }

    public FileConfiguration getInfoMenuConfig() {
        return infoMenuConfig;
    }


    public FileConfiguration getSignsConfig() {
        return signsConfig;
    }

    public void setConfig(FileConfiguration config) {
        this.config = config;
    }
}
