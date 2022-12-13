package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.land.processor.LandProcessor;
import tk.taverncraft.survivaltop.utils.types.ClaimInfo;

/**
 * Handles land wealth calculated using CrashClaim plugin.
 */
public class CrashClaimHandler implements LandClaimPluginHandler {
    private final Main main;
    private final LandProcessor landProcessor;

    /**
     * Constructor for CrashClaimHandler.
     *
     * @param main plugin class
     * @param landProcessor helper for land calculations
     */
    public CrashClaimHandler(Main main, LandProcessor landProcessor) {
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
        ArrayList<Claim> claims = getClaims(name);
        for (Claim claim : claims) {
            double minX = Math.min(claim.getMaxX(), claim.getMinX());
            double minY = main.getOptions().getMinLandHeight();
            double minZ = Math.min(claim.getMaxZ(), claim.getMinZ());
            double maxX = Math.max(claim.getMaxX(), claim.getMinX()) + 1;
            double maxY = main.getOptions().getMaxLandHeight();
            double maxZ = Math.max(claim.getMaxZ(), claim.getMinZ()) + 1;
            numBlocks += (maxX - minX) * (maxY - minY) * (maxZ - minZ);
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
            ArrayList<Claim> claims = getClaims(name);
            for (Claim claim : claims) {
                int maxX = claim.getMaxX();
                int maxZ = claim.getMaxZ();
                int minX = claim.getMinX();
                int minZ = claim.getMinZ();
                World world = Bukkit.getWorld(claim.getWorld());
                Location loc1 = new Location(world, maxX, 0, maxZ);
                Location loc2 = new Location(world, minX, 0, minZ);
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
    private void processEntityClaim(int id, Location l1, Location l2, World world) {
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
    private ArrayList<Claim> getClaims(String name) {
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
    private ArrayList<Claim> getClaimsByPlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        return CrashClaim.getPlugin().getApi().getClaims(player.getPlayer());
    }

    /**
     * Gets claims based on group.
     *
     * @param name name of group to get claims for
     *
     * @return List of claims of group
     */
    private ArrayList<Claim> getClaimsByGroup(String name) {
        List<OfflinePlayer> players = this.main.getGroupManager().getPlayers(name);
        ArrayList<Claim> claims = new ArrayList<>();
        for (OfflinePlayer player : players) {
            ArrayList<Claim> tempClaims =
                CrashClaim.getPlugin().getApi().getClaims(player.getPlayer());
            claims.addAll(tempClaims);
        }
        return claims;
    }
}
