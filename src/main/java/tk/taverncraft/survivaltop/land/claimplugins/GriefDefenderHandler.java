package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.flowpowered.math.vector.Vector3i;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.land.operations.LandOperationsHelper;

/**
 * Handles land wealth calculated using GriefDefender plugin.
 */
public class GriefDefenderHandler implements LandClaimPluginHandler {
    private final Main main;
    private final LandOperationsHelper landOperationsHelper;

    /**
     * Constructor for GriefDefenderHandler.
     *
     * @param main plugin class
     * @param landOperationsHelper helper for land calculations
     */
    public GriefDefenderHandler(Main main, LandOperationsHelper landOperationsHelper) {
        this.main = main;
        this.landOperationsHelper = landOperationsHelper;
    }

    /**
     * Gets the claim info for an entity.
     *
     * @param name name of entity to get claim info for
     *
     * @return size 2 array with 1st element = number of claims and 2nd element = number of blocks
     */
    public Long[] getClaimsInfo(String name) {
        long numBlocks = 0;
        List<Claim> claims = getClaims(name);
        for (Claim claim : claims) {
            Vector3i greaterBoundary = claim.getGreaterBoundaryCorner();
            Vector3i lesserBoundary = claim.getLesserBoundaryCorner();
            double minX = Math.min(greaterBoundary.getX(), lesserBoundary.getX());
            double minY = Math.min(greaterBoundary.getY(), lesserBoundary.getY());
            double minZ = Math.min(greaterBoundary.getZ(), lesserBoundary.getZ());
            double maxX = Math.max(greaterBoundary.getX(), lesserBoundary.getX()) + 1;
            double maxY = Math.max(greaterBoundary.getY(), lesserBoundary.getY()) + 1;
            double maxZ = Math.max(greaterBoundary.getZ(), lesserBoundary.getZ()) + 1;
            numBlocks += (maxX - minX) * (maxY - minY) * (maxZ - minZ);
        }
        return new Long[]{(long) claims.size(), numBlocks};
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
            List<Claim> claims = getClaims(name);
            for (Claim claim : claims) {
                Vector3i greaterBoundary = claim.getGreaterBoundaryCorner();
                Vector3i lesserBoundary = claim.getLesserBoundaryCorner();
                World world = Bukkit.getWorld(claim.getWorldUniqueId());
                Location loc1 = new Location(world, greaterBoundary.getX(),
                        greaterBoundary.getY(), greaterBoundary.getZ());
                Location loc2 = new Location(world, lesserBoundary.getX(),
                        lesserBoundary.getY(), lesserBoundary.getZ());
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
    private void processEntityClaim(UUID uuid, Location l1, Location l2, World world,
            boolean isLeaderboardUpdate) {
        double minX = Math.min(l1.getX(), l2.getX());
        double minY = Math.min(l1.getY(), l2.getY());
        double minZ = Math.min(l1.getZ(), l2.getZ());
        double maxX = Math.max(l1.getX(), l2.getX()) + 1;
        double maxY = Math.max(l1.getY(), l2.getY()) + 1;
        double maxZ = Math.max(l1.getZ(), l2.getZ()) + 1;
        landOperationsHelper.processEntityClaim(uuid, maxX, minX, maxY, minY, maxZ, minZ, world,
                isLeaderboardUpdate);
    }

    /**
     * Gets the claim for entity.
     *
     * @param name name of entity
     */
    private List<Claim> getClaims(String name) {
        if (this.main.getOptions().groupIsEnabled()) {
            return getClaimsByGroup(name);
        } else {
            return getClaimsByPlayer(name);
        }
    }

    /**
     * Gets claims based on player.
     *
     * @param name name of player to get claims for
     *
     * @return List of claims of player
     */
    private List<Claim> getClaimsByPlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        return GriefDefender.getCore().getAllPlayerClaims(player.getUniqueId());
    }

    /**
     * Gets claims based on group.
     *
     * @param name name of group to get claims for
     *
     * @return List of claims of group
     */
    private List<Claim> getClaimsByGroup(String name) {
        List<OfflinePlayer> players = this.main.getGroupManager().getPlayers(name);
        List<Claim> claims = new ArrayList<>();
        for (OfflinePlayer player : players) {
            List<Claim> tempClaims =
                GriefDefender.getCore().getAllPlayerClaims(player.getUniqueId());
            claims.addAll(tempClaims);
        }
        return claims;
    }
}
