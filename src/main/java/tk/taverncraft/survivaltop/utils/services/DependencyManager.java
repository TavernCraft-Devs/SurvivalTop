package tk.taverncraft.survivaltop.utils.services;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.logs.LogManager;

/**
 * DependencyManager handles the checking for dependencies.
 */
public class DependencyManager {
    private final Main main;

    // a map of configuration text to the plugin it uses
    private final HashMap<String, String> pluginMap = new HashMap<>() {{
        put("factionsuuid", "Factions");
        put("griefprevention", "GriefPrevention");
        put("residence", "Residence");
        put("ultimateclaims", "UltimateClaims");
        put("griefdefender", "GriefDefender");
        put("kingdomsx", "Kingdoms");
        put("crashclaim", "CrashClaim");
        put("redprotect", "RedProtect");
        put("townyadvanced", "Towny");
        put("townyadvancedtown", "Towny");
        put("townyadvancednation", "Towny");
        put("mcmmoparty", "mcMMO");
        put("parties", "Parties");
        put("saberfactions", "Factions");
    }};

    /**
     * Constructor for DependencyManager.
     *
     * @param main plugin class
     */
    public DependencyManager(Main main) {
        this.main = main;
    }

    /**
     * Checks if a specific dependency plugin is loaded.
     *
     * @param dependency plugin that is being depended on
     *
     * @return true if plugin is present and enabled, false otherwise
     */
    public boolean hasDependencyLoaded(String dependency) {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(dependency);
        return plugin != null && plugin.isEnabled();
    }

    /**
     * Checks if required dependencies are loaded.
     *
     * @return true if dependencies are loaded, false otherwise.
     */
    public boolean checkAllDependencies() {
        boolean balCheckPassed = checkBal();
        if (!balCheckPassed) {
            return false;
        }

        boolean landCheckPassed = checkLand();
        if (!landCheckPassed) {
            return false;
        }

        boolean groupCheckPassed = checkGroup();
        if (!groupCheckPassed) {
            return false;
        }

        boolean papiCheckPassed = checkPapi();
        return papiCheckPassed;
    }

    /**
     * Check if a dependency plugin is enabled.
     *
     * @param plugin plugin to check for
     *
     * @return true if plugin is enabled, false otherwise
     */
    private boolean isDependencyEnabled(String plugin) {
        if (Bukkit.getServer().getPluginManager().getPlugin(plugin) != null &&
            Bukkit.getServer().getPluginManager().getPlugin(plugin).isEnabled()) {
            return true;
        }
        LogManager.error("There appears to be a missing dependency: "
                + plugin + ". Have you installed it correctly?");
        return false;
    }

    /**
     * Check if vault is enabled.
     *
     * @return true if plugin is enabled, false otherwise
     */
    private boolean checkBal() {
        boolean enabled;
        if (main.getOptions().balIsIncluded()) {
            String depPlugin = "Vault";
            enabled = isDependencyEnabled(depPlugin);

            if (enabled) {
                LogManager.info("Successfully integrated with Vault!");
            } else {
                LogManager.error("Failed to integrate with Vault!");
                main.getOptions().disableBal();
                return false;
            }
        }
        return true;
    }

    /**
     * Check if land dependency plugin is enabled.
     *
     * @return true if plugin is enabled, false otherwise
     */
    private boolean checkLand() {
        boolean enabled;
        if (main.getOptions().landIsIncluded()) {
            String landType = main.getOptions().getLandType().toLowerCase();
            String depPlugin = pluginMap.get(landType);
            if (depPlugin == null) {
                LogManager.error("Failed to find a dependency for "
                    + landType + ", did you make a typo in the config?");
                main.getOptions().disableLand();
                return false;
            }
            enabled = isDependencyEnabled(depPlugin);

            if (enabled) {
                LogManager.info("Successfully integrated with "
                    + depPlugin + " for land type!");
            } else {
                LogManager.error("Failed to integrate with "
                    + depPlugin + " for land type!");
                main.getOptions().disableLand();
                return false;
            }
        }
        return true;
    }

    /**
     * Check if group dependency plugin is enabled.
     *
     * @return true if plugin is enabled, false otherwise
     */
    private boolean checkGroup() {
        boolean enabled;
        if (main.getOptions().groupIsEnabled()) {
            String groupType = main.getOptions().getGroupType().toLowerCase();
            String depPlugin = pluginMap.get(groupType);
            if (depPlugin == null) {
                LogManager.error("Failed to find a dependency for "
                    + groupType + ", did you make a typo in the config?");
                main.getOptions().disableGroup();
                return false;
            }
            enabled = isDependencyEnabled(depPlugin);

            if (enabled) {
                LogManager.info("Successfully integrated with "
                    + depPlugin + " for group type!");
            } else {
                LogManager.error("Failed to integrate with "
                    + depPlugin + " for group type!");
                main.getOptions().disableGroup();
                return false;
            }
        }
        return true;
    }

    /**
     * Check if papi dependency plugin is enabled.
     *
     * @return true if plugin is enabled, false otherwise
     */
    public boolean checkPapi() {
        boolean enabled;
        if (main.getOptions().papiIsIncluded()) {
            String depPlugin = "PlaceholderAPI";
            enabled = isDependencyEnabled(depPlugin);

            if (enabled) {
                LogManager.info("Successfully integrated with Papi!");
            } else {
                LogManager.error("Failed to integrate with Papi!");
                main.getOptions().disablePapi();
                return false;
            }
        }
        return true;
    }
}
