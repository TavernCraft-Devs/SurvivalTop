package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.land.operations.LandOperationsHelper;

/**
 * Handles land wealth calculated using CrashClaim plugin.
 */
public class CrashClaimHandler implements LandClaimPluginHandler {
    private final Main main;
    private final LandOperationsHelper landOperationsHelper;

    /**
     * Constructor for CrashClaimHandler.
     *
     * @param main plugin class
     * @param landOperationsHelper helper for land calculations
     */
    public CrashClaimHandler(Main main, LandOperationsHelper landOperationsHelper) {
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
            ArrayList<Claim> claims = getClaims(name);
            for (Claim claim : claims) {
                int maxX = claim.getMaxX();
                int maxZ = claim.getMaxZ();
                int minX = claim.getMinX();
                int minZ = claim.getMinZ();
                World world = Bukkit.getWorld(claim.getWorld());
                Location loc1 = new Location(world, maxX, 0, maxZ);
                Location loc2 = new Location(world, minX, 0, minZ);
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
        double minY = main.getOptions().getMinLandHeight();
        double minZ = Math.min(l1.getZ(), l2.getZ());
        double maxX = Math.max(l1.getX(), l2.getX()) + 1;
        double maxY = main.getOptions().getMaxLandHeight();
        double maxZ = Math.max(l1.getZ(), l2.getZ()) + 1;
        landOperationsHelper.processEntityClaim(uuid, maxX, minX, maxY, minY, maxZ, minZ, world,
                isLeaderboardUpdate);
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
