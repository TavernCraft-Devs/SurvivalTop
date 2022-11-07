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

public class CrashClaimHandler implements LandClaimPluginHandler {
    Main main;

    /**
     * Constructor for CrashClaimHandler.
     */
    public CrashClaimHandler(Main main) {
        if (main.getDependencyManager().isDependencyEnabled("CrashClaim")) {
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

    private double getClaimWorth(UUID uuid, Location l1, Location l2, World world,
                                 ArrayList<BiFunction<UUID, Block, Double>> blockOperations) {
        double minX = Math.min(l1.getX(), l2.getX());
        double minY = this.main.getMinHeight();
        double minZ = Math.min(l1.getZ(), l2.getZ());
        double maxX = Math.max(l1.getX(), l2.getX()) + 1;
        double maxY = this.main.getMaxHeight();
        double maxZ = Math.max(l1.getZ(), l2.getZ()) + 1;
        return main.getLandManager().getClaimWorth(uuid, maxX, minX, maxY, minY, maxZ, minZ, world, blockOperations);
    }

    private ArrayList<Claim> getClaimsByPlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        return CrashClaim.getPlugin().getApi().getClaims(player.getPlayer());
    }

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
