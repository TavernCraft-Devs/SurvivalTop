package tk.taverncraft.survivaltop.utils;

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
        return "2.0.2";
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
                return main.getServerStatsManager().getEntityNameAtPosition(index);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                Bukkit.getConsoleSender().sendMessage(e.getMessage());
                return "None";
            }
        }

        if (params.startsWith("top_wealth_")) {
            String[] args = params.split("_", 3);
            try {
                int index = Integer.parseInt(args[2]) - 1;
                return main.getServerStatsManager().getEntityWealthAtPosition(index);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                return "0";
            }
        }

        if (params.startsWith("entity_position")) {
            String[] args = params.split("_", 3);
            try {
                String entityName;
                if (args.length == 2) {
                    entityName = player.getName();
                } else {
                    entityName = args[2];
                }
                return main.getServerStatsManager().getPositionOfEntity(entityName);
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                return "None";
            }
        }

        if (params.startsWith("entity_total_wealth")) {
            String[] args = params.split("_", 4);
            try {
                String entityName;
                if (args.length == 3) {
                    entityName = player.getName();
                } else {
                    entityName = args[3];
                }
                return main.getServerStatsManager().getEntityTotalWealth(entityName);
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                return "0";
            }
        }

        if (params.startsWith("entity_land_wealth")) {
            String[] args = params.split("_", 4);
            try {
                String entityName;
                if (args.length == 3) {
                    entityName = player.getName();
                } else {
                    entityName = args[3];
                }
                return main.getServerStatsManager().getEntityLandWealth(entityName);
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                return "0";
            }
        }

        if (params.startsWith("entity_bal_wealth")) {
            String[] args = params.split("_", 4);
            try {
                String entityName;
                if (args.length == 3) {
                    entityName = player.getName();
                } else {
                    entityName = args[3];
                }
                return main.getServerStatsManager().getEntityBalWealth(entityName);
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                return "0";
            }
        }

        return null; // Placeholder is unknown by the Expansion
    }
}

