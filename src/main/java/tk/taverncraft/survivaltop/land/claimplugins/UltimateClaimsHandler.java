package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.region.ClaimCorners;
import com.songoda.ultimateclaims.claim.region.RegionCorners;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.land.processor.LandProcessor;
import tk.taverncraft.survivaltop.utils.types.ClaimInfo;

/**
 * Handles land wealth calculated using UltimateClaims plugin.
 */
public class UltimateClaimsHandler implements LandClaimPluginHandler  {
    private final Main main;
    private final LandProcessor landProcessor;
    private final UltimateClaims ultimateClaims;

    /**
     * Constructor for UltimateClaimsHandler.
     *
     * @param main plugin class
     * @param landProcessor helper for land calculations
     */
    public UltimateClaimsHandler(Main main, LandProcessor landProcessor) {
        this.main = main;
        this.landProcessor = landProcessor;
        this.ultimateClaims = (UltimateClaims) Bukkit.getServer().getPluginManager()
                .getPlugin("UltimateClaims");

    }

    /**
     * Gets the claim info for an entity.
     *
     * @param name name of entity to get claim info for
     *
     * @return size 2 array with 1st element = number of claims and 2nd element = number of blocks
     */
    public ClaimInfo getClaimsInfo(String name) {
        long numBlocks = 0;
        List<Claim> claims = getClaims(name);
        for (Claim claim : claims) {
            double maxY = main.getOptions().getMaxLandHeight();
            double minY = main.getOptions().getMinLandHeight();
            numBlocks += claim.getClaimSize() * (maxY - minY);
        }
        return new ClaimInfo(claims.size(), numBlocks);
    }

    /**
     * Processes the worth of a land.
     *
     * @param name name of entity to get land worth for
     * @param id key to identify task
     */
    public void processEntityLand(String name, int id) {
        try {
            OfflinePlayer player = Bukkit.getOfflinePlayer(name);
            List<Claim> claims = getClaims(name);
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
                        processEntityClaim(id, loc1, loc2, world);
                    }
                }
            }
        } catch (NoClassDefFoundError | NullPointerException ignored) {
        }
    }

    /**
     * Processes the worth of a claim identified between 2 locations.
     *
     * @param id key to identify task
     * @param l1 location 1
     * @param l2 location 2
     * @param world world that the claim is in
     */
    public void processEntityClaim(int id, Location l1, Location l2, World world) {
        double minX = Math.min(l1.getX(), l2.getX());
        double minY = main.getOptions().getMinLandHeight();
        double minZ = Math.min(l1.getZ(), l2.getZ());
        double maxX = Math.max(l1.getX(), l2.getX()) + 1;
        double maxY = main.getOptions().getMaxLandHeight();
        double maxZ = Math.max(l1.getZ(), l2.getZ()) + 1;
        landProcessor.processEntityClaim(id, maxX, minX, maxY, minY, maxZ, minZ, world);
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
        return ultimateClaims.getClaimManager().getClaims(player);
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
                ultimateClaims.getClaimManager().getClaims(player);
            claims.addAll(tempClaims);
        }
        return claims;
    }
}

