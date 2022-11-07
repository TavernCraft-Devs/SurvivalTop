package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import tk.taverncraft.survivaltop.Main;

public class RedProtectHandler implements LandClaimPluginHandler  {

    Main main;

    /**
     * Constructor for RedProtectHandler.
     */
    public RedProtectHandler(Main main) {
        if (main.getDependencyManager().isDependencyEnabled("RedProtect")) {
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
            Set<Region> claims;
            if (this.main.groupIsEnabled()) {
                claims = getClaimsByGroup(name);
            } else {
                claims = getClaimsByPlayer(name);
            }
            for (Region claim : claims) {
                World world = Bukkit.getWorld(claim.getWorld());
                Location loc1 = claim.getMaxLocation();
                Location loc2 = claim.getMinLocation();
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
        double minY = Math.min(l1.getY(), l2.getY());
        double minZ = Math.min(l1.getZ(), l2.getZ());
        double maxX = Math.max(l1.getX(), l2.getX()) + 1;
        double maxY = Math.max(l1.getY(), l2.getY()) + 1;
        double maxZ = Math.max(l1.getZ(), l2.getZ()) + 1;
        return main.getLandManager().getClaimWorth(uuid, maxX, minX, maxY, minY, maxZ, minZ, world, blockOperations);
    }

    private Set<Region> getClaimsByPlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        return RedProtect.get().getRegionManager().getMemberRegions(player.getUniqueId().toString());
    }

    private Set<Region> getClaimsByGroup(String name) {
        List<OfflinePlayer> players = this.main.getGroupManager().getPlayers(name);
        Set<Region> claims = new HashSet<>();
        for (OfflinePlayer player : players) {
            Set<Region> tempClaims =
                RedProtect.get().getRegionManager().getMemberRegions(player.getUniqueId().toString());
            claims.addAll(tempClaims);
        }
        return claims;
    }
}
