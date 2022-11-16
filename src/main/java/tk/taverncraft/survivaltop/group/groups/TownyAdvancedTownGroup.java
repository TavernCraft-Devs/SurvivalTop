package tk.taverncraft.survivaltop.group.groups;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

/**
 * Handles the group logic for Towny Advanced (Towns).
 */
public class TownyAdvancedTownGroup implements GroupHandler {
    private final TownyAPI api;

    /**
     * Constructor for TownyAdvancedTownGroup.
     */
    public TownyAdvancedTownGroup() {
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
        Town town = api.getTown(name);
        return town != null;
    }

    /**
     * Gets list of players from a group.
     *
     * @param name name of group to get players from
     *
     * @return list of players from given group
     */
    public List<OfflinePlayer> getPlayers(String name) {
        Town town = api.getTown(name);
        List<OfflinePlayer> players = new ArrayList<>();
        List<Resident> residents = town.getResidents();
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
        for (Town town : towns) {
            groups.add(town.getName());
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
            Town town = resident.getTown();
            if (town == null) {
                return null;
            }
            return town.getName();
        } catch (NotRegisteredException e) {
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
        Town town = api.getTown(name);
        if (town == null) {
            return null;
        }

        return town.getMayor().getName();
    }
}