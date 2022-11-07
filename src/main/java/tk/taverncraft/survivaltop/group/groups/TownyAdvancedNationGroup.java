package tk.taverncraft.survivaltop.group.groups;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import tk.taverncraft.survivaltop.Main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TownyAdvancedNationGroup implements GroupHandler {
    private Main main;
    private TownyAPI api;

    /**
     * Constructor for TownyAdvancedNationGroup.
     */
    public TownyAdvancedNationGroup(Main main) {
        if (main.isDependencyEnabled("Towny")) {
            this.main = main;
            this.api = TownyAPI.getInstance();
        }
    }

    public List<OfflinePlayer> getPlayers(String name) {
        Nation nation = api.getNation(name);
        List<OfflinePlayer> players = new ArrayList<>();
        List<Resident> residents = nation.getResidents();
        for (Resident resident : residents) {
            players.add(resident.getPlayer());
        }
        return players;
    }

    public boolean isValidGroup(String name) {
        if (name == null) {
            return false;
        }
        Nation nation = api.getNation(name);
        return nation != null;
    }

    public List<String> getGroups() {
        List<String> groups = new ArrayList<>();
        List<Town> towns = api.getTowns();
        Set<String> nationNames = new HashSet<>();
        for (Town town : towns) {
            try {
                nationNames.add(town.getNation().getName());
            } catch (NotRegisteredException e) {
                continue;
            }
        }

        List<Nation> nations = api.getNations(nationNames.toArray(new String[0]));
        for (Nation nation : nations) {
            groups.add(nation.getName());
        }
        return groups;
    }

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
}