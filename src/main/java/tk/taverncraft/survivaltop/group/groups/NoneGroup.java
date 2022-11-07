package tk.taverncraft.survivaltop.group.groups;

import org.bukkit.OfflinePlayer;
import tk.taverncraft.survivaltop.Main;

import java.util.ArrayList;
import java.util.List;

public class NoneGroup implements GroupHandler {
    private Main main;

    /**
     * Constructor for NoneGroup.
     */
    public NoneGroup(Main main) {
        this.main = main;
    }

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
