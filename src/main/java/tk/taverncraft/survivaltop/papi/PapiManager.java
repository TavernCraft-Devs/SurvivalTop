package tk.taverncraft.survivaltop.papi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import tk.taverncraft.survivaltop.Main;

/**
 * An expansion class for PAPI.
 */
public class PapiManager extends PlaceholderExpansion {
    private final Main main;
    private HashMap<String, List<String>> categoriesToPlaceholdersMap;

    /**
     * Constructor for PapiManager.
     *
     * @param main plugin class
     */
    public PapiManager(Main main) {
        this.main = main;
        initializePlaceholders();
    }

    /**
     * Initializes all placeholders.
     */
    public void initializePlaceholders() {
        categoriesToPlaceholdersMap = new HashMap<>();
        for (String category: main.getPapiConfig().getConfigurationSection("").getKeys(false)) {
            List<String> placeholders = main.getPapiConfig().getStringList(category + ".placeholders");
            categoriesToPlaceholdersMap.put(category, placeholders);
        }
    }

    /**
     * Gets the placeholder value of an entity based on name.
     *
     * @param name name of entity to get placeholder value for
     *
     * @return placeholder value of entity
     */
    public HashMap<String, Double> getPlaceholderValForEntity(String name) {
        if (main.getOptions().groupIsEnabled()) {
            return getPlaceholderValByGroup(name);
        }
        return getPlaceholderValByPlayer(name);
    }

    /**
     * Gets the placeholder value from a player by name.
     *
     * @param name name of player to get placeholder value for
     *
     * @return placeholder value of player
     */
    public HashMap<String, Double> getPlaceholderValByPlayer(String name) {
        HashMap<String, Double> papiWealth = new HashMap<>();
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        for (Map.Entry<String, List<String>> map : categoriesToPlaceholdersMap.entrySet()) {
            String category = map.getKey();
            double value = 0;
            for (String placeholder : map.getValue()) {
                value += getParsedValue(player, placeholder, name);
            }
            papiWealth.put(category, value);
        }
        return papiWealth;
    }

    /**
     * Gets the placeholder value from a group by name.
     *
     * @param group name of group to get placeholder value for
     *
     * @return placeholder value of group
     */
    private HashMap<String, Double> getPlaceholderValByGroup(String group) {
        HashMap<String, Double> papiWealth = new HashMap<>();
        for (Map.Entry<String, List<String>> map : categoriesToPlaceholdersMap.entrySet()) {
            String category = map.getKey();
            double value = 0;
            if (main.getPapiConfig().getString(category + ".type", "GROUP").equalsIgnoreCase("GROUP")) {
                for (String placeholder : map.getValue()) {
                    value += getParsedValue(null, placeholder, group);
                }
            } else {
                List<OfflinePlayer> offlinePlayers = this.main.getGroupManager().getPlayers(group);
                for (OfflinePlayer offlinePlayer : offlinePlayers) {
                    for (String placeholder : map.getValue()) {
                        value += getParsedValue(offlinePlayer, placeholder, offlinePlayer.getName());
                    }
                }
            }
            papiWealth.put(category, value);
        }
        return papiWealth;
    }

    private double getParsedValue(OfflinePlayer player, String placeholder, String name) {
        String parsedName = placeholder.replaceAll("\\{name}", name);
        String strValue = PlaceholderAPI.setPlaceholders(player, parsedName);
        try {
            return Double.parseDouble(strValue);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public String getAuthor() {
        return main.getDescription().getAuthors().get(0);
    }

    @Override
    public String getIdentifier() {
        return main.getDescription().getPrefix();
    }

    @Override
    public String getVersion() {
        return main.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.startsWith("top_name_")) {
            String[] args = params.split("_", 3);
            try {
                int index = Integer.parseInt(args[2]) - 1;
                return main.getLeaderboardManager().getEntityNameAtPosition(index);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                return "None";
            }
        }

        if (params.startsWith("top_wealth_")) {
            String[] args = params.split("_", 3);
            try {
                int index = Integer.parseInt(args[2]) - 1;
                return main.getLeaderboardManager().getEntityWealthAtPosition(index);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                return "0";
            }
        }

        if (params.startsWith("entity_position")) {
            String[] args = params.split("_", 3);
            try {
                String name = getEntityName(args, player, 2);
                return name == null ? "None" : main.getLeaderboardManager().getPositionOfEntity(name);
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                return "None";
            }
        }

        if (params.startsWith("entity_bal_wealth")) {
            String[] args = params.split("_", 4);
            try {
                String name = getEntityName(args, player, 3);
                return name == null ? "0" : main.getLeaderboardManager().getEntityBalWealth(name);
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                return "0";
            }
        }

        if (params.startsWith("entity_inv_wealth")) {
            String[] args = params.split("_", 4);
            try {
                String name = getEntityName(args, player, 3);
                return name == null ? "0" : main.getLeaderboardManager().getEntityInvWealth(name);
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                return "0";
            }
        }

        if (params.startsWith("entity_land_wealth")) {
            String[] args = params.split("_", 4);
            try {
                String name = getEntityName(args, player, 3);
                return name == null ? "0" : main.getLeaderboardManager().getEntityLandWealth(name);
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                return "0";
            }
        }

        if (params.startsWith("entity_block_wealth")) {
            String[] args = params.split("_", 4);
            try {
                String name = getEntityName(args, player, 3);
                return name == null ? "0" : main.getLeaderboardManager().getEntityBlockWealth(name);
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                return "0";
            }
        }

        if (params.startsWith("entity_spawner_wealth")) {
            String[] args = params.split("_", 4);
            try {
                String name = getEntityName(args, player, 3);
                return name == null ? "0" : main.getLeaderboardManager().getEntitySpawnerWealth(name);
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                return "0";
            }
        }

        if (params.startsWith("entity_container_wealth")) {
            String[] args = params.split("_", 4);
            try {
                String name = getEntityName(args, player, 3);
                return name == null ? "0" : main.getLeaderboardManager().getEntityContainerWealth(name);
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                return "0";
            }
        }

        if (params.startsWith("entity_total_wealth")) {
            String[] args = params.split("_", 4);
            try {
                String name = getEntityName(args, player, 3);
                return name == null ? "0" : main.getLeaderboardManager().getEntityTotalWealth(name);
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                return "0";
            }
        }

        return null; // Placeholder is unknown by the Expansion
    }

    /**
     * Helper function for getting an entity's uuid.
     *
     * @param args args in papi placeholder
     * @param player player who sent the command
     * @param length length of input
     *
     * @return name of entity of interest
     */
    public String getEntityName(String[] args, OfflinePlayer player, int length) {
        String entityName;
        if (args.length == length) {
            entityName = player.getName();
            if (this.main.getOptions().groupIsEnabled()) {
                String group = this.main.getGroupManager().getGroupOfPlayer(entityName);
                return group.toUpperCase();
            }
        } else {
            entityName = args[length];
        }
        return entityName.toUpperCase();
    }
}

