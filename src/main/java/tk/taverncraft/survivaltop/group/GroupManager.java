package tk.taverncraft.survivaltop.group;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.group.groups.FactionsUuidGroup;
import tk.taverncraft.survivaltop.group.groups.GroupHandler;
import tk.taverncraft.survivaltop.group.groups.KingdomsXGroup;
import tk.taverncraft.survivaltop.group.groups.McmmoPartyGroup;
import tk.taverncraft.survivaltop.group.groups.PartiesGroup;
import tk.taverncraft.survivaltop.group.groups.TownyAdvancedNationGroup;
import tk.taverncraft.survivaltop.group.groups.TownyAdvancedTownGroup;

/**
 * GroupManager is responsible for all group operations.
 */
public class GroupManager {
    private final Main main;

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
        String groupType = main.getOptions().getGroupType().toLowerCase();
        switch (groupType) {
        case "factionsuuid":
        case "saberfactions":
            groupHandler = new FactionsUuidGroup();
            return;
        case "kingdomsx":
            groupHandler = new KingdomsXGroup();
            return;
        case "mcmmoparty":
            groupHandler = new McmmoPartyGroup();
            return;
        case "parties":
            groupHandler = new PartiesGroup();
            return;
        case "townyadvancedtown":
            groupHandler = new TownyAdvancedTownGroup();
            return;
        case "townyadvancednation":
            groupHandler = new TownyAdvancedNationGroup();
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
        if (this.groupHandler == null) {
            return false;
        }
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
        if (this.groupHandler == null) {
            return new ArrayList<>();
        }
        return this.groupHandler.getPlayers(name);
    }

    /**
     * Gets all groups.
     *
     * @return list of all groups
     */
    public List<String> getGroups() {
        if (this.groupHandler == null) {
            return new ArrayList<>();
        }
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
        if (this.groupHandler == null) {
            return null;
        }
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
        if (this.groupHandler == null) {
            return null;
        }
        return this.groupHandler.getGroupLeader(name);
    }
}

