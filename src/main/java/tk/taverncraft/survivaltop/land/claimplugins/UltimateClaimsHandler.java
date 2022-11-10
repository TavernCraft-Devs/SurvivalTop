package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.region.ClaimCorners;
import com.songoda.ultimateclaims.claim.region.RegionCorners;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.land.operations.LandOperationsHelper;

import static org.bukkit.Bukkit.getServer;

/**
 * Handles land wealth calculated using UltimateClaims plugin.
 */
public class UltimateClaimsHandler implements LandClaimPluginHandler  {
    private Main main;
    private LandOperationsHelper landOperationsHelper;
    private UltimateClaims ultimateClaims;

    /**
     * Constructor for UltimateClaimsHandler.
     *
     * @param main plugin class
     * @param landOperationsHelper helper for land calculations
     */
    public UltimateClaimsHandler(Main main, LandOperationsHelper landOperationsHelper) {
        this.main = main;
        this.landOperationsHelper = landOperationsHelper;
        this.ultimateClaims = (UltimateClaims) getServer().getPluginManager().getPlugin("UltimateClaims");

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
            OfflinePlayer player = Bukkit.getOfflinePlayer(name);
            List<Claim> claims;
            if (this.main.groupIsEnabled()) {
                claims = getClaimsByGroup(name);
            } else {
                claims = getClaimsByPlayer(name);
            }
            for (Claim claim : claims) {
                if (!claim.getOwner().getUniqueId().equals(player.getUniqueId())) {
                    continue;
                }
                List<RegionCorners> regionCorners = claim.getCorners();
                for (RegionCorners regionCorner : regionCorners) {
                    Set<ClaimCorners> claimCorners = regionCorner.getClaimCorners();
                    for (ClaimCorners claimCorner : claimCorners) {
                        double x1 = claimCorner.x[0];
                        double x2 = claimCorner.x[1];
                        double z1 = claimCorner.z[0];
                        double z2 = claimCorner.z[1];
                        World world = Bukkit.getWorld(claim.getClaimedChunks().get(0).getWorld());
                        Location loc1 = new Location(world, x1, 0, z1);
                        Location loc2 = new Location(world, x2, 0, z2);
                        wealth += getClaimWorth(uuid, loc1, loc2, world, isLeaderboardUpdate);
                    }
                }
            }
            return wealth;
        } catch (NoClassDefFoundError | NullPointerException e) {
            return wealth;
        }
    }

    /**
     * Gets the worth of a claim identified between 2 locations.
     *
     * @param uuid uuid of sender if this is run through stats command; otherwise entities
     * @param l1 location 1
     * @param l2 location 2
     * @param world world that the claim is in
     * @param isLeaderboardUpdate true if is a leaderboard update, false otherwise (i.e. stats)
     *
     * @return double representing claim worth
     */
    public double getClaimWorth(UUID uuid, Location l1, Location l2, World world,
            boolean isLeaderboardUpdate) {
        double minX = Math.min(l1.getX(), l2.getX());
        double minY = this.main.getMinHeight();
        double minZ = Math.min(l1.getZ(), l2.getZ());
        double maxX = Math.max(l1.getX(), l2.getX()) + 1;
        double maxY = this.main.getMaxHeight();
        double maxZ = Math.max(l1.getZ(), l2.getZ()) + 1;
        return landOperationsHelper.getClaimWorth(uuid, maxX, minX, maxY, minY, maxZ, minZ, world,
                isLeaderboardUpdate);
    }

    /**
     * Get claims based on player.
     *
     * @param name name of player to get claims for
     *
     * @return List of claims of player
     */
    private List<Claim> getClaimsByPlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        return ultimateClaims.getClaimManager().getClaims(player);
    }

    /**
     * Get claims based on group.
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
                ultimateClaims.getClaimManager().getClaims(player);
            claims.addAll(tempClaims);
        }
        return claims;
    }
}

