package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.region.ClaimCorners;
import com.songoda.ultimateclaims.claim.region.RegionCorners;

import org.bukkit.block.Block;
import tk.taverncraft.survivaltop.Main;

import static org.bukkit.Bukkit.getServer;

/**
 * UltimateClaimsHandler handles land wealth calculated using UltimateClaims plugin.
 */
public class UltimateClaimsHandler implements LandClaimPluginHandler  {
    Main main;
    UltimateClaims ultimateClaims;

    /**
     * Constructor for UltimateClaimsHandler.
     */
    public UltimateClaimsHandler(Main main) {
        if (main.isDependencyEnabled("UltimateClaims")) {
            this.main = main;
            this.ultimateClaims = (UltimateClaims) getServer().getPluginManager().getPlugin("UltimateClaims");
        }
    }

    /**
     * Gets land worth of an entity.
     *
     * @param uuid uuid of the sender
     * @param name name of entity to get land worth for
     */
    public double getLandWorth(UUID uuid, String name,
            ArrayList<BiFunction<UUID, Block, Double>> blockOperations) {
        double wealth = 0;
        try {
            OfflinePlayer player = Bukkit.getOfflinePlayer(name);
            List<Claim> claims;
            if (this.main.groupIsEnabled()) {
                claims = getClaimsByGroup(name);
            } else {
                claims = getClaimsByPlayer(name);
            }
            for (Claim claim : claims) {
                if (!claim.getOwner().getUniqueId().equals(player.getUniqueId())) {
                    continue;
                }
                List<RegionCorners> regionCorners = claim.getCorners();
                for (RegionCorners regionCorner : regionCorners) {
                    Set<ClaimCorners> claimCorners = regionCorner.getClaimCorners();
                    for (ClaimCorners claimCorner : claimCorners) {
                        double x1 = claimCorner.x[0];
                        double x2 = claimCorner.x[1];
                        double z1 = claimCorner.z[0];
                        double z2 = claimCorner.z[1];
                        World world = Bukkit.getWorld(claim.getClaimedChunks().get(0).getWorld());
                        Location loc1 = new Location(world, x1, 0, z1);
                        Location loc2 = new Location(world, x2, 0, z2);
                        wealth += getClaimWorth(uuid, loc1, loc2, world, blockOperations);
                    }
                }
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

    private List<Claim> getClaimsByPlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        return ultimateClaims.getClaimManager().getClaims(player);
    }

    private List<Claim> getClaimsByGroup(String name) {
        List<OfflinePlayer> players = this.main.getGroupManager().getPlayers(name);
        List<Claim> claims = new ArrayList<>();
        for (OfflinePlayer player : players) {
            List<Claim> tempClaims =
                ultimateClaims.getClaimManager().getClaims(player);
            claims.addAll(tempClaims);
        }
        return claims;
    }
}

