package tk.taverncraft.survivaltop.group.groups;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;

/**
 * Handles the group logic for Parties.
 */
public class PartiesGroup implements GroupHandler {
    PartiesAPI api;

    /**
     * Constructor for PartiesGroup.
     */
    public PartiesGroup() {
        this.api = Parties.getApi();
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
        Party party = api.getParty(name);
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
        Party party = api.getParty(name);
        List<OfflinePlayer> players = new ArrayList<>();
        Set<UUID> members = party.getMembers();
        for (UUID uuid : members) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
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
        List<Party> parties = api.getPartiesListByMembers(Integer.MAX_VALUE, 0);
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
        Party party = api.getPartyOfPlayer(player.getUniqueId());
        if (party == null) {
            return null;
        }
        return party.getName();
    }

    /**
     * Gets the leader of a group.
     *
     * @param name name of group to get leader for
     *
     * @return name of group leader
     */
    public String getGroupLeader(String name) {
        Party party = api.getParty(name);
        if (party == null) {
            return null;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(party.getLeader());
        return player.getName();
    }
}
