package tk.taverncraft.survivaltop.utils;

import java.util.HashMap;

import org.bukkit.Bukkit;

import tk.taverncraft.survivaltop.Main;

/**
 * DependencyManager handles the checking for dependencies.
 */
public class DependencyManager {
    private Main main;

    // a map of configuration text to the plugin it uses
    private HashMap<String, String> pluginMap = new HashMap<String, String>() {{
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
        return groupCheckPassed;
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
        Bukkit.getLogger().severe("[SurvivalTop] There appears to be a missing dependency: "
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
        if (main.balIsIncluded()) {
            String depPlugin = "Vault";
            if (depPlugin == null) {
                Bukkit.getLogger().severe("[SurvivalTop] Failed to find Vault plugin even " +
                    "though balance is included!");
                main.disableBal();
                return false;
            }
            enabled = isDependencyEnabled(depPlugin);

            if (enabled) {
                Bukkit.getLogger().info("[SurvivalTop] Successfully integrated with Vault!");
            } else {
                Bukkit.getLogger().severe("[SurvivalTop] Failed to integrate with Vault!");
                main.disableBal();
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
        if (main.landIsIncluded()) {
            String landType = main.getConfig().getString("land-type",
                "griefprevention").toLowerCase();
            String depPlugin = pluginMap.get(landType);
            if (depPlugin == null) {
                Bukkit.getLogger().severe("[SurvivalTop] Failed to find a dependency for "
                    + landType + ", did you make a typo in the config?");
                main.disableLand();
                return false;
            }
            enabled = isDependencyEnabled(depPlugin);

            if (enabled) {
                Bukkit.getLogger().info("[SurvivalTop] Successfully integrated with: "
                    + depPlugin + " for land type!");
            } else {
                Bukkit.getLogger().severe("[SurvivalTop] Failed to integrate with: "
                    + depPlugin + " for land type!");
                main.disableLand();
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
        if (main.getConfig().getBoolean("enable-group")) {
            String groupType = main.getConfig().getString("group-type",
                "factionsuuid").toLowerCase();
            String depPlugin = pluginMap.get(groupType);
            if (depPlugin == null) {
                Bukkit.getLogger().severe("[SurvivalTop] Failed to find a dependency for "
                    + groupType + ", did you make a typo in the config?");
                main.disableGroup();
                return false;
            }
            enabled = isDependencyEnabled(depPlugin);

            if (enabled) {
                Bukkit.getLogger().info("[SurvivalTop] Successfully integrated with: "
                    + depPlugin + " for group type!");
            } else {
                Bukkit.getLogger().severe("[SurvivalTop] Failed to integrate with: "
                    + depPlugin + " for group type!");
                main.disableGroup();
                return false;
            }
        }
        return true;
    }
}
