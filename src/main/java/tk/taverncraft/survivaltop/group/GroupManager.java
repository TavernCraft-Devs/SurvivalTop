package tk.taverncraft.survivaltop.group;

import java.util.*;

import org.bukkit.OfflinePlayer;
import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.group.groups.FactionsUuidGroup;
import tk.taverncraft.survivaltop.group.groups.GroupHandler;
import tk.taverncraft.survivaltop.group.groups.KingdomsXGroup;
import tk.taverncraft.survivaltop.group.groups.McmmoPartyGroup;
import tk.taverncraft.survivaltop.group.groups.NoneGroup;
import tk.taverncraft.survivaltop.group.groups.PartiesGroup;
import tk.taverncraft.survivaltop.land.claimplugins.*;

/**
 * GroupManager is responsible for all group operations.
 */
public class GroupManager {
    Main main;

    // helper classes
    private GroupHandler groupHandler;

    /**
     * Constructor for GroupManager.
     */
    public GroupManager(Main main) throws NullPointerException {
        this.main = main;
        initializeLandType();
    }

    /**
     * Initializes values for land type depending on which land plugin is used.
     */
    public void initializeLandType() throws NullPointerException {
        String groupType = main.getConfig().getString("group-type", "None");
        if (groupType.equalsIgnoreCase("factionsuuid")) {
            groupHandler = new FactionsUuidGroup(main);
        } else if (groupType.equalsIgnoreCase("kingdomsx")) {
            groupHandler = new KingdomsXGroup(main);
        } else if (groupType.equalsIgnoreCase("mcmmoparty")) {
            groupHandler = new McmmoPartyGroup(main);
        } else if (groupType.equalsIgnoreCase("parties")) {
            groupHandler = new PartiesGroup(main);
        } else {
            groupHandler = new NoneGroup(main);
        }
    }

    public boolean isValidGroup(String name) {
        return this.groupHandler.isValidGroup(name);
    }

    public List<OfflinePlayer> getPlayers(String name) {
        return this.groupHandler.getPlayers(name);
    }

    public List<String> getGroups() {
        return this.groupHandler.getGroups();
    }

    public String getGroupOfPlayer(String playerName) {
        return this.groupHandler.getGroupOfPlayer(playerName);
    }
}

