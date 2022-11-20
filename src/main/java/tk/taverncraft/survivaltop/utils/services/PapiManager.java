package tk.taverncraft.survivaltop.utils.services;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import tk.taverncraft.survivaltop.Main;

/**
 * An expansion class for PAPI.
 */
public class PapiManager extends PlaceholderExpansion {

    private final Main main;

    /**
     * Constructor for PapiManager.
     *
     * @param main plugin class
     */
    public PapiManager(Main main) {
        this.main = main;
    }

    @Override
    public String getAuthor() {
        return "tjtanjin - FrozenFever";
    }

    @Override
    public String getIdentifier() {
        return "survtop";
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

