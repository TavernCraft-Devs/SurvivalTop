package tk.taverncraft.survivaltop.group.groups;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;

/**
 * Handles the group logic for FactionsUUID.
 */
public class FactionsUuidGroup implements GroupHandler {
    private final int FILTER_SIZE = 3;
    private final List<String> filteredGroups = new ArrayList<>() {
        {
            add("§2wilderness");
            add("§6safezone");
            add("§4warzone");
        }
    };

    /**
     * Constructor for FactionsUuidGroup.
     */
    public FactionsUuidGroup() {}

    /**
     * Checks if a group exists.
     *
     * @param name name of group to check for
     *
     * @return true if group exist, false otherwise
     */
    public boolean isValidGroup(String name) {
        if (name == null) {
            return false;
        }
        if (isFilteredGroup(name)) {
            return false;
        }
        Faction faction = Factions.getInstance().getByTag(name);
        return faction != null;
    }

    /**
     * Gets list of players from a group.
     *
     * @param name name of group to get players from
     *
     * @return list of players from given group
     */
    public List<OfflinePlayer> getPlayers(String name) {
        Faction faction = Factions.getInstance().getByTag(name);
        List<OfflinePlayer> offlinePlayers = new ArrayList<>();
        Set<FPlayer> fPlayers = faction.getFPlayers();
        for (FPlayer fPlayer : fPlayers) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(fPlayer.getId()));
            offlinePlayers.add(player);
        }
        return offlinePlayers;
    }

    /**
     * Gets all groups.
     *
     * @return list of all groups
     */
    public List<String> getGroups() {
        List<String> groups = new ArrayList<>();
        List<Faction> factions = Factions.getInstance().getAllFactions();
        for (Faction faction : factions) {
            String group = faction.getTag();
            if (isFilteredGroup(group)) {
                continue;
            }
            groups.add(faction.getTag());
        }
        return groups;
    }

    /**
     * Gets the group a player belongs to.
     *
     * @param playerName name of player to get group for
     *
     * @return group name of the player
     */
    public String getGroupOfPlayer(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        FPlayer fPlayer = FPlayers.getInstance().getByOfflinePlayer(player);
        Faction faction = fPlayer.getFaction();
        if (faction == null) {
            return null;
        }
        return faction.getTag();
    }

    /**
     * Gets the leader of a group.
     *
     * @param name name of group to get leader for
     *
     * @return name of group leader
     */
    public String getGroupLeader(String name) {
        Faction faction = Factions.getInstance().getByTag(name);
        if (faction == null) {
            return null;
        }
        return faction.getTag();
    }

    /**
     * Checks if a group should be filtered. Unique to factions that has wilderness, safezone and
     * warzone that needs to be filtered out.
     *
     * @param name name of group to check
     *
     * @return true if group needs to be filtered, false otherwise
     */
    private boolean isFilteredGroup(String name) {
        for (int i = 0; i < FILTER_SIZE; i++) {
            if (filteredGroups.get(i).equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
