package tk.taverncraft.survivaltop.group.groups;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;

/**
 * Empty group class that does nothing for cases where group is not required.
 */
public class NoneGroup implements GroupHandler {

    public NoneGroup() {}

    public List<OfflinePlayer> getPlayers(String name) {
        return new ArrayList<>();
    }

    public boolean isValidGroup(String name) {
        return false;
    }

    public List<String> getGroups() {
        return new ArrayList<>();
    }

    public String getGroupOfPlayer(String playerName) {
        return null;
    }

    public String getGroupLeader(String name) { return null; }
}
