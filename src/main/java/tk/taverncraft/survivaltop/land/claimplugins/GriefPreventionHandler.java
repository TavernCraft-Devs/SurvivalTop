package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.List;
import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.land.processor.LandProcessor;
import tk.taverncraft.survivaltop.utils.types.ClaimInfo;

/**
 * Handles land wealth calculated using GriefPrevention plugin.
 */
public class GriefPreventionHandler implements LandClaimPluginHandler {
    private final Main main;
    private final LandProcessor landProcessor;

    /**
     * Constructor for GriefPreventionHandler.
     *
     * @param main plugin class
     * @param landProcessor helper for land calculations
     */
    public GriefPreventionHandler(Main main, LandProcessor landProcessor) {
        this.main = main;
        this.landProcessor = landProcessor;
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
        Vector<Claim> claims = getClaims(name);
        for (Claim claim : claims) {
            double maxY = main.getOptions().getMaxLandHeight();
            double minY = main.getOptions().getMinLandHeight();
            numBlocks += claim.getArea() * (maxY - minY);
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
            Vector<Claim> claims = getClaims(name);
            for (Claim claim : claims) {
                Location loc1 = claim.getGreaterBoundaryCorner();
                Location loc2 = claim.getLesserBoundaryCorner();
                World world = loc1.getWorld();
                processEntityClaim(id, loc1, loc2, world);
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
    private Vector<Claim> getClaims(String name) {
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
    private Vector<Claim> getClaimsByPlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        return GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId()).getClaims();
    }

    /**
     * Gets claims based on group.
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
