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

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.block.Block;

import tk.taverncraft.survivaltop.Main;

/**
 * GriefPreventionHandler handles land wealth calculated using GriefPrevention plugin.
 */
public class GriefPreventionHandler implements LandClaimPluginHandler {
    Main main;

    /**
     * Constructor for GriefPreventionHandler.
     */
    public GriefPreventionHandler(Main main) {
        this.main = main;
    }

    /**
     * Gets land worth of an entity.
     *
     * @param name name of entity to get land worth for
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
     * Runs update with inclusion of search for spawners.
     *
     * @param l1 corner of claim
     * @param l2 corner of claim
     * @param world world to search in
     */
    public double getClaimWorth(UUID uuid, Location l1, Location l2, World world,
            ArrayList<BiFunction<UUID, Block, Double>> blockOperations) {
        double minX = Math.min(l1.getX(), l2.getX());
        double minY = this.main.getMinHeight();
        double minZ = Math.min(l1.getZ(), l2.getZ());
        double maxX = Math.max(l1.getX(), l2.getX()) + 1;
        double maxY = this.main.getMaxHeight();
        double maxZ = Math.max(l1.getZ(), l2.getZ()) + 1;
        return main.getLandManager().getClaimWorth(uuid, maxX, minX, maxY, minY, maxZ, minZ, world, blockOperations);
    }

    private Vector<Claim> getClaimsByPlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        return GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId()).getClaims();
    }

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
