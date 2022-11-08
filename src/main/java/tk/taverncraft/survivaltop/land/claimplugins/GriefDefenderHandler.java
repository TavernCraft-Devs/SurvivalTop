package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.flowpowered.math.vector.Vector3i;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;

import tk.taverncraft.survivaltop.Main;

/**
 * Handles land wealth calculated using GriefDefender plugin.
 */
public class GriefDefenderHandler implements LandClaimPluginHandler {
    private Main main;

    /**
     * Constructor for GriefDefenderHandler.
     *
     * @param main plugin class
     */
    public GriefDefenderHandler(Main main) {
        this.main = main;
    }

    /**
     * Get the worth of a land.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     * @param name name of entity to get land worth for
     * @param blockOperations operations to perform
     *
     * @return double representing its worth
     */
    public double getLandWorth(UUID uuid, String name,
            ArrayList<BiFunction<UUID, Block, Double>> blockOperations) {
        double wealth = 0;
        try {
            List<Claim> claims;
            if (this.main.groupIsEnabled()) {
                claims = getClaimsByGroup(name);
            } else {
                claims = getClaimsByPlayer(name);
            }
            for (Claim claim : claims) {
                Vector3i greaterBoundary = claim.getGreaterBoundaryCorner();
                Vector3i lesserBoundary = claim.getLesserBoundaryCorner();
                World world = Bukkit.getWorld(claim.getWorldUniqueId());
                Location loc1 = new Location(world, greaterBoundary.getX(),
                        greaterBoundary.getY(), greaterBoundary.getZ());
                Location loc2 = new Location(world, lesserBoundary.getX(),
                        lesserBoundary.getY(), lesserBoundary.getZ());
                wealth += getClaimWorth(uuid, loc1, loc2, world, blockOperations);
            }
            return wealth;
        } catch (NoClassDefFoundError | NullPointerException e) {
            return wealth;
        }
    }

    /**
     * Gets the worth of a claim identified between 2 locations.
     *
     * @param uuid uuid of sender, not to be confused with the entity itself!
     * @param l1 location 1
     * @param l2 location 2
     * @param world world that the claim is in
     * @param blockOperations operations to perform
     *
     * @return double representing claim worth
     */
    private double getClaimWorth(UUID uuid, Location l1, Location l2, World world,
            ArrayList<BiFunction<UUID, Block, Double>> blockOperations) {
        double minX = Math.min(l1.getX(), l2.getX());
        double minY = Math.min(l1.getY(), l2.getY());
        double minZ = Math.min(l1.getZ(), l2.getZ());
        double maxX = Math.max(l1.getX(), l2.getX()) + 1;
        double maxY = Math.max(l1.getY(), l2.getY()) + 1;
        double maxZ = Math.max(l1.getZ(), l2.getZ()) + 1;
        return main.getLandManager().getClaimWorth(uuid, maxX, minX, maxY, minY, maxZ, minZ,
                world, blockOperations);
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
        return GriefDefender.getCore().getAllPlayerClaims(player.getUniqueId());
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
                GriefDefender.getCore().getAllPlayerClaims(player.getUniqueId());
            claims.addAll(tempClaims);
        }
        return claims;
    }
}
