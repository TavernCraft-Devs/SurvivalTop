package tk.taverncraft.survivaltop.utils.services;

import java.util.UUID;

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
        return "2.2.2";
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
                UUID uuid = getEntityUuid(args, player, 2);
                return uuid == null ? "None" : main.getServerStatsManager().getPositionOfEntity(uuid);
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                return "None";
            }
        }

        if (params.startsWith("entity_bal_wealth")) {
            String[] args = params.split("_", 4);
            try {
                UUID uuid = getEntityUuid(args, player, 3);
                return uuid == null ? "0" : main.getServerStatsManager().getEntityBalWealth(uuid);
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                return "0";
            }
        }

        if (params.startsWith("entity_inv_wealth")) {
            String[] args = params.split("_", 4);
            try {
                UUID uuid = getEntityUuid(args, player, 3);
                return uuid == null ? "0" : main.getServerStatsManager().getEntityInvWealth(uuid);
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                return "0";
            }
        }

        if (params.startsWith("entity_land_wealth")) {
            String[] args = params.split("_", 4);
            try {
                UUID uuid = getEntityUuid(args, player, 3);
                return uuid == null ? "0" : main.getServerStatsManager().getEntityLandWealth(uuid);
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                return "0";
            }
        }

        if (params.startsWith("entity_block_wealth")) {
            String[] args = params.split("_", 4);
            try {
                UUID uuid = getEntityUuid(args, player, 3);
                return uuid == null ? "0" : main.getServerStatsManager().getEntityBlockWealth(uuid);
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                return "0";
            }
        }

        if (params.startsWith("entity_spawner_wealth")) {
            String[] args = params.split("_", 4);
            try {
                UUID uuid = getEntityUuid(args, player, 3);
                return uuid == null ? "0" : main.getServerStatsManager().getEntitySpawnerWealth(uuid);
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                return "0";
            }
        }

        if (params.startsWith("entity_container_wealth")) {
            String[] args = params.split("_", 4);
            try {
                UUID uuid = getEntityUuid(args, player, 3);
                return uuid == null ? "0" : main.getServerStatsManager().getEntityContainerWealth(uuid);
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                return "0";
            }
        }

        if (params.startsWith("entity_total_wealth")) {
            String[] args = params.split("_", 4);
            try {
                UUID uuid = getEntityUuid(args, player, 3);
                return uuid == null ? "0" : main.getServerStatsManager().getEntityTotalWealth(uuid);
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
     * @return uuid of entity of interest
     */
    public UUID getEntityUuid(String[] args, OfflinePlayer player, int length) {
        String entityName;
        if (args.length == length) {
            entityName = player.getName();
            if (this.main.getOptions().groupIsEnabled()) {
                String group = this.main.getGroupManager().getGroupOfPlayer(entityName);
                return this.main.getServerStatsManager().getGroupNameToUuidMap().get(group);
            }
        } else {
            entityName = args[length];
            if (this.main.getOptions().groupIsEnabled()) {
                return this.main.getServerStatsManager().getGroupNameToUuidMap().get(entityName);
            }
        }

        assert entityName != null;
        OfflinePlayer target = Bukkit.getOfflinePlayer(entityName);
        return target.getUniqueId();
    }
}

