package tk.taverncraft.survivaltop.group.groups;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

public interface GroupHandler {
    List<OfflinePlayer> getPlayers(String name);
    boolean isValidGroup(String name);
    List<String> getGroups();
    String getGroupOfPlayer(String playerName);
}
