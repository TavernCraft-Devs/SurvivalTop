package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.land.operations.LandOperationsHelper;

/**
 * Handles land wealth calculated using FactionsUuid plugin.
 */
public class FactionsUuidHandler implements LandClaimPluginHandler {
    private Main main;
    private LandOperationsHelper landOperationsHelper;

    /**
     * Constructor for FactionsUuidHandler.
     *
     * @param main plugin class
     * @param landOperationsHelper helper for land calculations
     */
    public FactionsUuidHandler(Main main, LandOperationsHelper landOperationsHelper) {
        this.main = main;
        this.landOperationsHelper = landOperationsHelper;
    }

    /**
     * Get the worth of a land.
     *
     * @param uuid uuid of sender if this is run through stats command; otherwise entities
     * @param name name of entity to get land worth for
     * @param isLeaderboardUpdate true if is a leaderboard update, false otherwise (i.e. stats)
     *
     * @return double representing its worth
     */
    public double getLandWorth(UUID uuid, String name, boolean isLeaderboardUpdate) {
        double wealth = 0;
        try {
            Set<FLocation> claims;
            if (this.main.groupIsEnabled()) {
                claims = getClaimsByGroup(name);
            } else {
                claims = getClaimsByPlayer(name);
            }
            for (FLocation claim : claims) {
                wealth += landOperationsHelper.getChunkWorth(uuid, claim.getChunk(),
                        claim.getWorld(), isLeaderboardUpdate);
            }
            return wealth;
        } catch (NoClassDefFoundError | NullPointerException e) {
            return wealth;
        }
    }

    /**
     * Get claims based on player.
     *
     * @param name name of player to get claims for
     *
     * @return List of claims of player
     */
    private Set<FLocation> getClaimsByPlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        FPlayer fPlayer = FPlayers.getInstance().getByOfflinePlayer(player);
        Faction faction = fPlayer.getFaction();
        return faction.getAllClaims();
    }

    /**
     * Get claims based on group.
     *
     * @param name name of group to get claims for
     *
     * @return List of claims of group
     */
    private Set<FLocation> getClaimsByGroup(String name) {
        // todo: minor cleanup here to streamline checks
        String groupType = this.main.getConfig().getString(
                "group-type", "factionsuuid").toLowerCase();
        if (groupType.equals("factionsuuid") || groupType.equals("saberfactions")) {
            Faction faction = Factions.getInstance().getByTag(name);
            return faction.getAllClaims();
        }

        List<OfflinePlayer> players = this.main.getGroupManager().getPlayers(name);
        Set<FLocation> claims = new HashSet<>();
        for (OfflinePlayer player : players) {
            FPlayer fPlayer = FPlayers.getInstance().getByOfflinePlayer(player);
            Faction faction = fPlayer.getFaction();
            claims.addAll(faction.getAllClaims());
        }
        return claims;
    }
}
