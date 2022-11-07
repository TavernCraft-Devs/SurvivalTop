package tk.taverncraft.survivaltop.group.groups;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.player.KingdomPlayer;
import org.kingdoms.main.Kingdoms;
import tk.taverncraft.survivaltop.Main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KingdomsXGroup implements GroupHandler {
    private Main main;

    /**
     * Constructor for KingdomsXGroup.
     */
    public KingdomsXGroup(Main main) {
        this.main = main;
    }

    public List<OfflinePlayer> getPlayers(String name) {
        Kingdom kingdom = Kingdoms.get().getDataHandlers().getKingdomManager().getData(name);
        return kingdom.getPlayerMembers();
    }

    public boolean isValidGroup(String name) {
        if (name == null) {
            return false;
        }
        Kingdom kingdom = Kingdoms.get().getDataHandlers().getKingdomManager().getData(name);
        return kingdom != null;
    }

    public List<String> getGroups() {
        List<String> groups = new ArrayList<>();
        Collection<Kingdom> kingdoms = Kingdoms.get().getDataHandlers().getKingdomManager().getKingdoms();
        for (Kingdom kingdom : kingdoms) {
            groups.add(kingdom.getName());
        }
        return groups;
    }

    public String getGroupOfPlayer(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        KingdomPlayer kPlayer = KingdomPlayer.getKingdomPlayer(player.getUniqueId());
        Kingdom kingdom = kPlayer.getKingdom();
        if (kingdom == null) {
            return null;
        }
        return kingdom.getName();
    }

    public String getGroupLeader(String name) {
        Kingdom kingdom = Kingdoms.get().getDataHandlers().getKingdomManager().getData(name);
        if (kingdom == null) {
            return null;
        }
        return kingdom.getName();
    }
}