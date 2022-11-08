package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.function.BiFunction;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import tk.taverncraft.survivaltop.Main;

/**
 * Handles land wealth calculated using GriefPrevention plugin.
 */
public class GriefPreventionHandler implements LandClaimPluginHandler {
    private Main main;

    /**
     * Constructor for GriefPreventionHandler.
     *
     * @param main plugin class
     */
    public GriefPreventionHandler(Main main) {
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
    public double getClaimWorth(UUID uuid, Location l1, Location l2, World world,
            ArrayList<BiFunction<UUID, Block, Double>> blockOperations) {
        double minX = Math.min(l1.getX(), l2.getX());
        double minY = this.main.getMinHeight();
        double minZ = Math.min(l1.getZ(), l2.getZ());
        double maxX = Math.max(l1.getX(), l2.getX()) + 1;
        double maxY = this.main.getMaxHeight();
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
