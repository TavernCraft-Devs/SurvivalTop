package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.List;
import java.util.UUID;
import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.land.operations.LandOperationsHelper;

/**
 * Handles land wealth calculated using GriefPrevention plugin.
 */
public class GriefPreventionHandler implements LandClaimPluginHandler {
    private final Main main;
    private final LandOperationsHelper landOperationsHelper;

    /**
     * Constructor for GriefPreventionHandler.
     *
     * @param main plugin class
     * @param landOperationsHelper helper for land calculations
     */
    public GriefPreventionHandler(Main main, LandOperationsHelper landOperationsHelper) {
        this.main = main;
        this.landOperationsHelper = landOperationsHelper;
    }

    /**
     * Processes the worth of a land.
     *
     * @param uuid uuid of sender if this is run through stats command; otherwise entities
     * @param name name of entity to get land worth for
     * @param isLeaderboardUpdate true if is a leaderboard update, false otherwise (i.e. stats)
     */
    public void processEntityLand(UUID uuid, String name, boolean isLeaderboardUpdate) {
        try {
            Vector<Claim> claims;
            if (this.main.groupIsEnabled()) {
                claims = getClaimsByGroup(name);
            } else {
                claims = getClaimsByPlayer(name);
            }
            for (Claim claim : claims) {
                Location loc1 = claim.getGreaterBoundaryCorner();
                Location loc2 = claim.getLesserBoundaryCorner();
                World world = loc1.getWorld();
                processEntityClaim(uuid, loc1, loc2, world, isLeaderboardUpdate);
            }
        } catch (NoClassDefFoundError | NullPointerException ignored) {
        }
    }

    /**
     * Processes the worth of a claim identified between 2 locations.
     *
     * @param uuid uuid of sender if this is run through stats command; otherwise entities
     * @param l1 location 1
     * @param l2 location 2
     * @param world world that the claim is in
     * @param isLeaderboardUpdate true if is a leaderboard update, false otherwise (i.e. stats)
     */
    public void processEntityClaim(UUID uuid, Location l1, Location l2, World world,
            boolean isLeaderboardUpdate) {
        double minX = Math.min(l1.getX(), l2.getX());
        double minY = this.main.getMinHeight();
        double minZ = Math.min(l1.getZ(), l2.getZ());
        double maxX = Math.max(l1.getX(), l2.getX()) + 1;
        double maxY = this.main.getMaxHeight();
        double maxZ = Math.max(l1.getZ(), l2.getZ()) + 1;
        landOperationsHelper.processEntityClaim(uuid, maxX, minX, maxY, minY, maxZ, minZ, world,
                isLeaderboardUpdate);
    }

    /**
     * Get claims based on player.
     *
     * @param name name of player to get claims for
     *
     * @return List of claims of player
     */
    private Vector<Claim> getClaimsByPlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        return GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId()).getClaims();
    }

    /**
     * Get claims based on group.
     *
     * @param name name of group to get claims for
     *
     * @return List of claims of group
     */
    private Vector<Claim> getClaimsByGroup(String name) {
        List<OfflinePlayer> players = this.main.getGroupManager().getPlayers(name);
        Vector<Claim> claims = new Vector<>();
        for (OfflinePlayer player : players) {
            Vector<Claim> tempClaims =
                GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId()).getClaims();
            claims.addAll(tempClaims);
        }
        return claims;
    }
}
