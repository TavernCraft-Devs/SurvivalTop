package tk.taverncraft.survivaltop.group.groups;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.player.KingdomPlayer;
import org.kingdoms.main.Kingdoms;

/**
 * Handles the group logic for KingdomsX.
 */
public class KingdomsXGroup implements GroupHandler {

    /**
     * Constructor for KingdomsXGroup.
     */
    public KingdomsXGroup() {}

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
        Kingdom kingdom = Kingdoms.get().getDataHandlers().getKingdomManager().getData(name);
        return kingdom != null;
    }

    /**
     * Gets list of players from a group.
     *
     * @param name name of group to get players from
     *
     * @return list of players from given group
     */
    public List<OfflinePlayer> getPlayers(String name) {
        Kingdom kingdom = Kingdoms.get().getDataHandlers().getKingdomManager().getData(name);
        return kingdom.getPlayerMembers();
    }

    /**
     * Gets all groups.
     *
     * @return list of all groups
     */
    public List<String> getGroups() {
        List<String> groups = new ArrayList<>();
        Collection<Kingdom> kingdoms = Kingdoms.get().getDataHandlers().getKingdomManager()
                .getKingdoms();
        for (Kingdom kingdom : kingdoms) {
            groups.add(kingdom.getName());
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
        KingdomPlayer kPlayer = KingdomPlayer.getKingdomPlayer(player.getUniqueId());
        Kingdom kingdom = kPlayer.getKingdom();
        if (kingdom == null) {
            return null;
        }
        return kingdom.getName();
    }

    /**
     * Gets the leader of a group.
     *
     * @param name name of group to get leader for
     *
     * @return name of group leader
     */
    public String getGroupLeader(String name) {
        Kingdom kingdom = Kingdoms.get().getDataHandlers().getKingdomManager().getData(name);
        if (kingdom == null) {
            return null;
        }
        return kingdom.getName();
    }
}