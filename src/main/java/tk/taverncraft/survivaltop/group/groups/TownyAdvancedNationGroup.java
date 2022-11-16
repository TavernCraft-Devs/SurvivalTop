package tk.taverncraft.survivaltop.group.groups;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

/**
 * Handles the group logic for Towny Advanced (Nations).
 */
public class TownyAdvancedNationGroup implements GroupHandler {
    private final TownyAPI api;

    /**
     * Constructor for TownyAdvancedNationGroup.
     */
    public TownyAdvancedNationGroup() {
        this.api = TownyAPI.getInstance();
    }

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
        Nation nation = api.getNation(name);
        return nation != null;
    }

    /**
     * Gets list of players from a group.
     *
     * @param name name of group to get players from
     *
     * @return list of players from given group
     */
    public List<OfflinePlayer> getPlayers(String name) {
        Nation nation = api.getNation(name);
        List<OfflinePlayer> players = new ArrayList<>();
        List<Resident> residents = nation.getResidents();
        for (Resident resident : residents) {
            players.add(resident.getPlayer());
        }
        return players;
    }

    /**
     * Gets all groups.
     *
     * @return list of all groups
     */
    public List<String> getGroups() {
        List<String> groups = new ArrayList<>();
        List<Town> towns = api.getTowns();
        Set<String> nationNames = new HashSet<>();
        for (Town town : towns) {
            try {
                nationNames.add(town.getNation().getName());
            } catch (NotRegisteredException ignored) {
            }
        }

        List<Nation> nations = api.getNations(nationNames.toArray(new String[0]));
        for (Nation nation : nations) {
            groups.add(nation.getName());
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
        Resident resident = api.getResident(player.getUniqueId());
        try {
            Nation nation = resident.getNation();
            if (nation == null) {
                return null;
            }
            return nation.getName();
        } catch (TownyException e) {
            return null;
        }
    }

    /**
     * Gets the leader of a group.
     *
     * @param name name of group to get leader for
     *
     * @return name of group leader
     */
    public String getGroupLeader(String name) {
        Nation nation = api.getNation(name);
        if (nation == null) {
            return null;
        }

        return nation.getKing().getName();
    }
}