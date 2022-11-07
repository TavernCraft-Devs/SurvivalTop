package tk.taverncraft.survivaltop.group.groups;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import tk.taverncraft.survivaltop.Main;

import java.util.ArrayList;
import java.util.List;

public class TownyAdvancedTownGroup implements GroupHandler {
    private Main main;
    private TownyAPI api;

    /**
     * Constructor for TownyAdvancedTownGroup.
     */
    public TownyAdvancedTownGroup(Main main) {
        this.main = main;
        this.api = TownyAPI.getInstance();
    }

    public List<OfflinePlayer> getPlayers(String name) {
        Town town = api.getTown(name);
        List<OfflinePlayer> players = new ArrayList<>();
        List<Resident> residents = town.getResidents();
        for (Resident resident : residents) {
            players.add(resident.getPlayer());
        }
        return players;
    }

    public boolean isValidGroup(String name) {
        if (name == null) {
            return false;
        }
        Town town = api.getTown(name);
        return town != null;
    }

    public List<String> getGroups() {
        List<String> groups = new ArrayList<>();
        List<Town> towns = api.getTowns();
        for (Town town : towns) {
            groups.add(town.getName());
        }
        return groups;
    }

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

    public String getGroupLeader(String name) {
        Town town = api.getTown(name);
        if (town == null) {
            return null;
        }

        return town.getMayor().getName();
    }
}