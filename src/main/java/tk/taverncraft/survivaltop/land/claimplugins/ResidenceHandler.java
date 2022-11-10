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
    private Main main;
    private LandOperationsHelper landOperationsHelper;

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
            List<ClaimedResidence> claims;
            if (this.main.groupIsEnabled()) {
                claims = getClaimsByGroup(name);
            } else {
                claims = getClaimsByPlayer(name);
            }
            for (ClaimedResidence claim : claims) {
                CuboidArea[] areas = claim.getAreaArray();
                for (CuboidArea area : areas) {
                    Location loc1 = area.getHighLocation();
                    Location loc2 = area.getLowLocation();
                    World world = area.getWorld();
                    wealth += getClaimWorth(uuid, loc1, loc2, world, isLeaderboardUpdate);
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
        double minY = Math.min(l1.getY(), l2.getY());
        double minZ = Math.min(l1.getZ(), l2.getZ());
        double maxX = Math.max(l1.getX(), l2.getX()) + 1;
        double maxY = Math.max(l1.getY(), l2.getY()) + 1;
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
    private List<ClaimedResidence> getClaimsByPlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        return Residence.getInstance().getPlayerManager().getResidencePlayer(
                player.getUniqueId()).getResList();
    }

    /**
     * Get claims based on group.
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
