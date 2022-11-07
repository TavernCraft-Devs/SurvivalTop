package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;

import org.bukkit.block.Block;
import tk.taverncraft.survivaltop.Main;

/**
 * ResidenceHandler handles land wealth calculated using Residence plugin.
 */
public class ResidenceHandler implements LandClaimPluginHandler  {
    Main main;

    /**
     * Constructor for ResidenceHandler.
     */
    public ResidenceHandler(Main main) {
        if (main.isDependencyEnabled("Residence")) {
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
                    wealth += getClaimWorth(uuid, loc1, loc2, world, blockOperations);
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
        double minY = Math.min(l1.getY(), l2.getY());
        double minZ = Math.min(l1.getZ(), l2.getZ());
        double maxX = Math.max(l1.getX(), l2.getX()) + 1;
        double maxY = Math.max(l1.getY(), l2.getY()) + 1;
        double maxZ = Math.max(l1.getZ(), l2.getZ()) + 1;
        return main.getLandManager().getClaimWorth(uuid, maxX, minX, maxY, minY, maxZ, minZ, world, blockOperations);
    }

    private List<ClaimedResidence> getClaimsByPlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        return Residence.getInstance().getPlayerManager().getResidencePlayer(player.getUniqueId()).getResList();
    }

    private List<ClaimedResidence> getClaimsByGroup(String name) {
        List<OfflinePlayer> players = this.main.getGroupManager().getPlayers(name);
        List<ClaimedResidence> claims = new ArrayList<>();
        for (OfflinePlayer player : players) {
            List<ClaimedResidence> tempClaims =
                Residence.getInstance().getPlayerManager().getResidencePlayer(player.getUniqueId()).getResList();
            claims.addAll(tempClaims);
        }
        return claims;
    }
}
