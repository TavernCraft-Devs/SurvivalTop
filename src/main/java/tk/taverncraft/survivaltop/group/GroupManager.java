package tk.taverncraft.survivaltop.group;

import java.util.List;

import org.bukkit.OfflinePlayer;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.group.groups.FactionsUuidGroup;
import tk.taverncraft.survivaltop.group.groups.GroupHandler;
import tk.taverncraft.survivaltop.group.groups.KingdomsXGroup;
import tk.taverncraft.survivaltop.group.groups.McmmoPartyGroup;
import tk.taverncraft.survivaltop.group.groups.NoneGroup;
import tk.taverncraft.survivaltop.group.groups.PartiesGroup;
import tk.taverncraft.survivaltop.group.groups.TownyAdvancedNationGroup;
import tk.taverncraft.survivaltop.group.groups.TownyAdvancedTownGroup;

/**
 * GroupManager is responsible for all group operations.
 */
public class GroupManager {
    private Main main;

    // helper classes
    private GroupHandler groupHandler;

    /**
     * Constructor for GroupManager.
     *
     * @param main plugin class
     */
    public GroupManager(Main main) throws NullPointerException {
        this.main = main;
        initializeLandType();
    }

    /**
     * Initializes values for land type depending on which land plugin is used.
     */
    public void initializeLandType() throws NullPointerException {
        String groupType = main.getConfig().getString("group-type", "factionsuuid");
        if (groupType.equalsIgnoreCase("factionsuuid")) {
            groupHandler = new FactionsUuidGroup(main);
        } else if (groupType.equalsIgnoreCase("kingdomsx")) {
            groupHandler = new KingdomsXGroup(main);
        } else if (groupType.equalsIgnoreCase("mcmmoparty")) {
            groupHandler = new McmmoPartyGroup(main);
        } else if (groupType.equalsIgnoreCase("parties")) {
            groupHandler = new PartiesGroup(main);
        } else if (groupType.equalsIgnoreCase("townyadvancedtown")) {
            groupHandler = new TownyAdvancedTownGroup(main);
        } else if (groupType.equalsIgnoreCase("townyadvancednation")) {
            groupHandler = new TownyAdvancedNationGroup(main);
        } else {
            groupHandler = new NoneGroup();
        }
    }

    /**
     * Checks if a group is exist.
     *
     * @param name name of group to check for
     *
     * @return true if group exist, false otherwise
     */
    public boolean isValidGroup(String name) {
        return this.groupHandler.isValidGroup(name);
    }

    /**
     * Gets list of players from a group.
     *
     * @param name name of group to get players from
     *
     * @return list of players from given group
     */
    public List<OfflinePlayer> getPlayers(String name) {
        return this.groupHandler.getPlayers(name);
    }

    /**
     * Gets all groups.
     *
     * @return list of all groups
     */
    public List<String> getGroups() {
        return this.groupHandler.getGroups();
    }

    /**
     * Gets the group a player belongs to.
     *
     * @param playerName name of player to get group for
     *
     * @return group name of the player
     */
    public String getGroupOfPlayer(String playerName) {
        return this.groupHandler.getGroupOfPlayer(playerName);
    }

    /**
     * Gets the leader of a group.
     *
     * @param name name of group to get leader for
     *
     * @return name of group leader
     */
    public String getGroupLeader(String name) {
        return this.groupHandler.getGroupLeader(name);
    }
}

