package tk.taverncraft.survivaltop.utils;

import org.bukkit.Bukkit;
import tk.taverncraft.survivaltop.Main;

import java.util.HashMap;

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
    }};

    public DependencyManager(Main main) {
        this.main = main;
    }

    public boolean hasDependenciesLoaded() {
        boolean landCheckPassed;
        if (main.getConfig().getBoolean("include-land")) {
            String landType = main.getConfig().getString("land-type", "griefprevention").toLowerCase();
            String depPlugin = pluginMap.get(landType);
            if (depPlugin == null) {
                Bukkit.getLogger().severe("Failed to find a dependency for " + landType + ", did" +
                    " you make a typo in the config?");
                return false;
            }
            landCheckPassed = isDependencyEnabled(depPlugin);

            if (landCheckPassed) {
                Bukkit.getLogger().info("Successfully integrated with: " + depPlugin + " for land" +
                    " type!");
            } else {
                Bukkit.getLogger().severe("Failed to integrate with: " + depPlugin + " for land " +
                    "type!");
                return false;
            }
        }

        boolean groupCheckPassed;
        if (main.getConfig().getBoolean("enable-group")) {
            String groupType = main.getConfig().getString("group-type", "factionsuuid").toLowerCase();
            String depPlugin = pluginMap.get(groupType);
            if (depPlugin == null) {
                Bukkit.getLogger().severe("Failed to find a dependency for " + groupType + ", did" +
                    " you make a typo in the config?");
                return false;
            }
            groupCheckPassed = isDependencyEnabled(depPlugin);

            if (groupCheckPassed) {
                Bukkit.getLogger().info("Successfully integrated with: " + depPlugin + " for " +
                    "group type!");
            } else {
                Bukkit.getLogger().severe("Failed to integrate with: " + depPlugin + " for group " +
                    "type!");
                return false;
            }
        }

        return true;
    }

    public boolean isDependencyEnabled(String plugin) {
        if (Bukkit.getServer().getPluginManager().getPlugin(plugin) != null &&
            Bukkit.getServer().getPluginManager().getPlugin(plugin).isEnabled()) {
            return true;
        }
        Bukkit.getLogger().severe("There appears to be a missing dependency: " + plugin + ". Have" +
            " you installed it correctly?");
        return false;
    }
}
