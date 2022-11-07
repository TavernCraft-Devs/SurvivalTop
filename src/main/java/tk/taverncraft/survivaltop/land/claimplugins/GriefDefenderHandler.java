package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

import com.flowpowered.math.vector.Vector3i;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;

import org.bukkit.block.Block;
import tk.taverncraft.survivaltop.Main;

public class GriefDefenderHandler implements LandClaimPluginHandler {
    Main main;

    /**
     * Constructor for GriefDefenderHandler.
     */
    public GriefDefenderHandler(Main main) {
        if (main.isDependencyEnabled("GriefDefender")) {
            this.main = main;
        }
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

    private double getClaimWorth(UUID uuid, Location l1, Location l2, World world,
            ArrayList<BiFunction<UUID, Block, Double>> blockOperations) {
        double minX = Math.min(l1.getX(), l2.getX());
        double minY = Math.min(l1.getY(), l2.getY());
        double minZ = Math.min(l1.getZ(), l2.getZ());
        double maxX = Math.max(l1.getX(), l2.getX()) + 1;
        double maxY = Math.max(l1.getY(), l2.getY()) + 1;
        double maxZ = Math.max(l1.getZ(), l2.getZ()) + 1;
        return main.getLandManager().getClaimWorth(uuid, maxX, minX, maxY, minY, maxZ, minZ, world, blockOperations);
    }

    private List<Claim> getClaimsByPlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        return GriefDefender.getCore().getAllPlayerClaims(player.getUniqueId());
    }

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
