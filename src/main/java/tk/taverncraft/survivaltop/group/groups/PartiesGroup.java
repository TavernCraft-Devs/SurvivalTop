package tk.taverncraft.survivaltop.group.groups;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import tk.taverncraft.survivaltop.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PartiesGroup implements GroupHandler {
    private Main main;
    PartiesAPI api;

    /**
     * Constructor for PartiesGroup.
     */
    public PartiesGroup(Main main) {
        if (main.isDependencyEnabled("Parties")) {
            this.main = main;
            this.api = Parties.getApi();
        }
    }

    public List<OfflinePlayer> getPlayers(String name) {
        Party party = api.getParty(name);
        List<OfflinePlayer> players = new ArrayList<>();
        Set<UUID> members = party.getMembers();
        for (UUID uuid : members) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            players.add(player);
        }
        return players;
    }

    public boolean isValidGroup(String name) {
        Party party = api.getParty(name);
        return party != null;
    }

    public List<String> getGroups() {
        List<String> partyNames = new ArrayList<>();
        List<Party> parties = api.getPartiesListByMembers(Integer.MAX_VALUE, 0);
        for (Party party : parties) {
            partyNames.add(party.getName());
        }
        return partyNames;
    }

    public String getGroupOfPlayer(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        Party party = api.getPartyOfPlayer(player.getUniqueId());
        if (party == null) {
            return null;
        }
        return party.getName();
    }
}
