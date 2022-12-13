package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.land.processor.LandProcessor;
import tk.taverncraft.survivaltop.utils.types.ClaimInfo;

/**
 * Handles land wealth calculated using RedProtect plugin.
 */
public class RedProtectHandler implements LandClaimPluginHandler  {
    private final Main main;
    private final LandProcessor landProcessor;

    /**
     * Constructor for RedProtectHandler.
     *
     * @param main plugin class
     * @param landProcessor helper for land calculations
     */
    public RedProtectHandler(Main main, LandProcessor landProcessor) {
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
        Set<Region> claims = getClaims(name);
        for (Region claim : claims) {
            Location loc1 = claim.getMaxLocation();
            Location loc2 = claim.getMinLocation();
            double minY = Math.min(loc1.getY(), loc2.getY());
            double maxY = Math.min(loc1.getY(), loc2.getY());
            numBlocks += claim.getArea() * (maxY - minY);
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
            Set<Region> claims = getClaims(name);
            for (Region claim : claims) {
                World world = Bukkit.getWorld(claim.getWorld());
                Location loc1 = claim.getMaxLocation();
                Location loc2 = claim.getMinLocation();
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
    public void processEntityClaim(int id, Location l1, Location l2, World world) {
        double minX = Math.min(l1.getX(), l2.getX());
        double minY = Math.min(l1.getY(), l2.getY());
        double minZ = Math.min(l1.getZ(), l2.getZ());
        double maxX = Math.max(l1.getX(), l2.getX()) + 1;
        double maxY = Math.max(l1.getY(), l2.getY()) + 1;
        double maxZ = Math.max(l1.getZ(), l2.getZ()) + 1;
        landProcessor.processEntityClaim(id, maxX, minX, maxY, minY, maxZ, minZ, world);
    }

    /**
     * Gets the claim for entity.
     *
     * @param name name of entity
     */
    private Set<Region> getClaims(String name) {
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
    private Set<Region> getClaimsByPlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        return RedProtect.get().getRegionManager().getMemberRegions(
                player.getUniqueId().toString());
    }

    /**
     * Gets claims based on group.
     *
     * @param name name of group to get claims for
     *
     * @return List of claims of group
     */
    private Set<Region> getClaimsByGroup(String name) {
        List<OfflinePlayer> players = this.main.getGroupManager().getPlayers(name);
        Set<Region> claims = new HashSet<>();
        for (OfflinePlayer player : players) {
            Set<Region> tempClaims =
                    RedProtect.get().getRegionManager().getMemberRegions(
                            player.getUniqueId().toString());
            claims.addAll(tempClaims);
        }
        return claims;
    }
}
