package tk.taverncraft.survivaltop.group.groups;

import java.util.List;

import org.bukkit.OfflinePlayer;

/**
 * Interface for getting information from different group types.
 */
public interface GroupHandler {

    /**
     * Checks if a group exists.
     *
     * @param name name of group to check for
     *
     * @return true if group exist, false otherwise
     */
    boolean isValidGroup(String name);

    /**
     * Gets list of players from a group.
     *
     * @param name name of group to get players from
     *
     * @return list of players from given group
     */
    List<OfflinePlayer> getPlayers(String name);

    /**
     * Gets all groups.
     *
     * @return list of all groups
     */
    List<String> getGroups();

    /**
     * Gets the group a player belongs to.
     *
     * @param playerName name of player to get group for
     *
     * @return group name of the player
     */
    String getGroupOfPlayer(String playerName);

    /**
     * Gets the leader of a group.
     *
     * @param name name of group to get leader for
     *
     * @return name of group leader
     */
    String getGroupLeader(String name);
}
