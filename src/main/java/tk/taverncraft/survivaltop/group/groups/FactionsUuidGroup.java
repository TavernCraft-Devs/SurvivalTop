package tk.taverncraft.survivaltop.group.groups;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import tk.taverncraft.survivaltop.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FactionsUuidGroup implements GroupHandler {
    private Main main;
    private List<String> filteredGroups = new ArrayList<String>() {
        {
            add("§2wilderness");
            add("§6safezone");
            add("§4warzone");
        }
    };

    /**
     * Constructor for FactionsUuidGroup.
     */
    public FactionsUuidGroup(Main main) {
        if (main.isDependencyEnabled("Factions")) {
            this.main = main;
        }
    }

    public List<OfflinePlayer> getPlayers(String name) {
        Faction faction = Factions.getInstance().getByTag(name);
        List<OfflinePlayer> offlinePlayers = new ArrayList<>();
        Set<FPlayer> fPlayers = faction.getFPlayers();
        for (FPlayer fPlayer : fPlayers) {
            offlinePlayers.add(fPlayer.getOfflinePlayer());
        }
        return offlinePlayers;
    }

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

    public String getGroupOfPlayer(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        FPlayer fPlayer = FPlayers.getInstance().getByOfflinePlayer(player);
        Faction faction = fPlayer.getFaction();
        if (faction == null) {
            return null;
        }
        return faction.getTag();
    }

    private boolean isFilteredGroup(String name) {
        for (String group : filteredGroups) {
            if (group.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
