package tk.taverncraft.survivaltop.group.groups;

import com.alessiodp.parties.api.Parties;
import com.gmail.nossr50.api.PartyAPI;
import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.party.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import tk.taverncraft.survivaltop.Main;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class McmmoPartyGroup implements GroupHandler {
    private Main main;

    /**
     * Constructor for McmmoPartyGroup.
     */
    public McmmoPartyGroup(Main main) {
        if (main.isDependencyEnabled("mcMMO")) {
            this.main = main;
        }
    }

    public List<OfflinePlayer> getPlayers(String name) {
        Party party = PartyManager.getParty(name);
        List<OfflinePlayer> players = new ArrayList<>();
        LinkedHashMap<UUID, String> members = party.getMembers();
        for (Map.Entry<UUID, String> set : members.entrySet()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(set.getKey());
            players.add(player);
        }
        return players;
    }

    public boolean isValidGroup(String name) {
        if (name == null) {
            return false;
        }
        Party party = PartyManager.getParty(name);
        return party != null;
    }

    public List<String> getGroups() {
        List<String> partyNames = new ArrayList<>();
        List<Party> parties = PartyAPI.getParties();
        for (Party party : parties) {
            partyNames.add(party.getName());
        }
        return partyNames;
    }

    public String getGroupOfPlayer(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return PartyAPI.getPartyName(player.getPlayer());
    }
}
