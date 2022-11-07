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
import org.bukkit.block.Block;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;

import tk.taverncraft.survivaltop.Main;

/**
 * FactionsUuidHandler handles land wealth calculated using FactionsUuid plugin.
 */
public class FactionsUuidHandler implements LandClaimPluginHandler {
    Main main;

    /**
     * Constructor for FactionsUuidHandler.
     */
    public FactionsUuidHandler(Main main) {
        if (main.getDependencyManager().isDependencyEnabled("Factions")) {
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
            Set<FLocation> claims;
            if (this.main.groupIsEnabled()) {
                claims = getClaimsByGroup(name);
            } else {
                claims = getClaimsByPlayer(name);
            }
            for (FLocation claim : claims) {
                Long chunkX = claim.getX();
                Long chunkZ = claim.getZ();
                World world = claim.getWorld();
                double loc1X = chunkX * 16;
                double loc1Z = chunkZ * 16;
                double loc2X = loc1X + 15;
                double loc2Z = loc1Z + 15;
                Location loc1 = new Location(world, loc1X, 0, loc1Z);
                Location loc2 = new Location(world, loc2X, 0, loc2Z);
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

    private Set<FLocation> getClaimsByPlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        FPlayer fPlayer = FPlayers.getInstance().getByOfflinePlayer(player);
        Faction faction = fPlayer.getFaction();
        return faction.getAllClaims();
    }

    private Set<FLocation> getClaimsByGroup(String name) {
        if (this.main.getConfig().getString("group-type").equalsIgnoreCase("factionsuuid")) {
            Faction faction = Factions.getInstance().getByTag(name);
            return faction.getAllClaims();
        }

        List<OfflinePlayer> players = this.main.getGroupManager().getPlayers(name);
        Set<FLocation> claims = new HashSet<>();
        for (OfflinePlayer player : players) {
            FPlayer fPlayer = FPlayers.getInstance().getByOfflinePlayer(player);
            Faction faction = fPlayer.getFaction();
            claims.addAll(faction.getAllClaims());
        }
        return claims;
    }
}
