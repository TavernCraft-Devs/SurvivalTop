package tk.taverncraft.survivaltop.group.groups;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.gmail.nossr50.api.PartyAPI;
import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.party.PartyManager;

/**
 * Handles the group logic for mcMMO Party.
 */
public class McmmoPartyGroup implements GroupHandler {

    /**
     * Constructor for McmmoPartyGroup.
     */
    public McmmoPartyGroup() {}

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
        Party party = PartyManager.getParty(name);
        return party != null;
    }

    /**
     * Gets list of players from a group.
     *
     * @param name name of group to get players from
     *
     * @return list of players from given group
     */
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

    /**
     * Gets all groups.
     *
     * @return list of all groups
     */
    public List<String> getGroups() {
        List<String> partyNames = new ArrayList<>();
        List<Party> parties = PartyAPI.getParties();
        for (Party party : parties) {
            partyNames.add(party.getName());
        }
        return partyNames;
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
        return PartyAPI.getPartyName(player.getPlayer());
    }

    /**
     * Gets the leader of a group.
     *
     * @param name name of group to get leader for
     *
     * @return name of group leader
     */
    public String getGroupLeader(String name) {
        return PartyAPI.getPartyLeader(name);
    }
}
