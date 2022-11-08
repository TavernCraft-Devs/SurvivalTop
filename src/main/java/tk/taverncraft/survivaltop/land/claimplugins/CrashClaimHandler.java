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

import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;

import tk.taverncraft.survivaltop.Main;

/**
 * Handles land wealth calculated using CrashClaim plugin.
 */
public class CrashClaimHandler implements LandClaimPluginHandler {
    private Main main;

    /**
     * Constructor for CrashClaimHandler.
     *
     * @param main plugin class
     */
    public CrashClaimHandler(Main main) {
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
            ArrayList<Claim> claims;
            if (this.main.groupIsEnabled()) {
                claims = getClaimsByGroup(name);
            } else {
                claims = getClaimsByPlayer(name);
            }
            for (Claim claim : claims) {
                int maxX = claim.getMaxX();
                int maxZ = claim.getMaxZ();
                int minX = claim.getMinX();
                int minZ = claim.getMinZ();
                World world = Bukkit.getWorld(claim.getWorld());
                Location loc1 = new Location(world, maxX, 0, maxZ);
                Location loc2 = new Location(world, minX, 0, minZ);
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
    private ArrayList<Claim> getClaimsByPlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        return CrashClaim.getPlugin().getApi().getClaims(player.getPlayer());
    }

    /**
     * Get claims based on group.
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
