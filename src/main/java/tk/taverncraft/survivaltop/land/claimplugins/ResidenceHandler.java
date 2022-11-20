package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.land.operations.LandOperationsHelper;

/**
 * Handles land wealth calculated using Residence plugin.
 */
public class ResidenceHandler implements LandClaimPluginHandler  {
    private final Main main;
    private final LandOperationsHelper landOperationsHelper;

    /**
     * Constructor for ResidenceHandler.
     *
     * @param main plugin class
     * @param landOperationsHelper helper for land calculations
     */
    public ResidenceHandler(Main main, LandOperationsHelper landOperationsHelper) {
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
        List<ClaimedResidence> claims = getClaims(name);
        for (ClaimedResidence claim : claims) {
            numBlocks += claim.getTotalSize();
        }
        return new Long[]{(long) claims.size(), numBlocks};
    }

    /**
     * Processes the worth of a land.
     *
     * @param name name of entity to get land worth for
     * @param id key to identify task
     * @param isLeaderboardUpdate true if is a leaderboard update, false otherwise (i.e. stats)
     */
    public void processEntityLand(String name, int id, boolean isLeaderboardUpdate) {
        try {
            List<ClaimedResidence> claims = getClaims(name);
            for (ClaimedResidence claim : claims) {
                CuboidArea[] areas = claim.getAreaArray();
                for (CuboidArea area : areas) {
                    Location loc1 = area.getHighLocation();
                    Location loc2 = area.getLowLocation();
                    World world = area.getWorld();
                    processEntityClaim(id, loc1, loc2, world, isLeaderboardUpdate);
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
     * @param isLeaderboardUpdate true if is a leaderboard update, false otherwise (i.e. stats)
     */
    public void processEntityClaim(int id, Location l1, Location l2, World world,
            boolean isLeaderboardUpdate) {
        double minX = Math.min(l1.getX(), l2.getX());
        double minY = Math.min(l1.getY(), l2.getY());
        double minZ = Math.min(l1.getZ(), l2.getZ());
        double maxX = Math.max(l1.getX(), l2.getX()) + 1;
        double maxY = Math.max(l1.getY(), l2.getY()) + 1;
        double maxZ = Math.max(l1.getZ(), l2.getZ()) + 1;
        landOperationsHelper.processEntityClaim(id, maxX, minX, maxY, minY, maxZ, minZ, world,
                isLeaderboardUpdate);
    }

    /**
     * Gets the claim for entity.
     *
     * @param name name of entity
     */
    private List<ClaimedResidence> getClaims(String name) {
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
    private List<ClaimedResidence> getClaimsByPlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        return Residence.getInstance().getPlayerManager().getResidencePlayer(
                player.getUniqueId()).getResList();
    }

    /**
     * Gets claims based on group.
     *
     * @param name name of group to get claims for
     *
     * @return List of claims of group
     */
    private List<ClaimedResidence> getClaimsByGroup(String name) {
        List<OfflinePlayer> players = this.main.getGroupManager().getPlayers(name);
        List<ClaimedResidence> claims = new ArrayList<>();
        for (OfflinePlayer player : players) {
            List<ClaimedResidence> tempClaims =
                    Residence.getInstance().getPlayerManager().getResidencePlayer(
                            player.getUniqueId()).getResList();
            claims.addAll(tempClaims);
        }
        return claims;
    }
}
