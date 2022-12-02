package tk.taverncraft.survivaltop.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
     * Creates papi config file.
     */
    public void createPapiConfig() {
        FileConfiguration config = getConfig("papi.yml");
        main.setPapiConfig(config);
    }

    /**
     * Creates stats menu config file.
     */
    public void createStatsMenuConfig() {
        FileConfiguration config = getConfig("menu/stats.yml");
        main.setStatsMenuConfig(config);
    }

    /**
     * Creates info menu config file.
     */
    public void createInfoMenuConfig() {
        FileConfiguration config = getConfig("menu/info.yml");
        main.setInfoMenuConfig(config);
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
}
