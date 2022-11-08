package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;

import tk.taverncraft.survivaltop.Main;

/**
 * Handles land wealth calculated using Towny Advanced plugin.
 */
public class TownyAdvancedHandler implements LandClaimPluginHandler {
    private Main main;
    private TownyAPI api;

    /**
     * Constructor for TownyAdvancedHandler.
     *
     * @param main plugin class
     */
    public TownyAdvancedHandler(Main main) {
        this.main = main;
        this.api = TownyAPI.getInstance();
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
            Collection<TownBlock> claims;
            if (this.main.groupIsEnabled()) {
                claims = getClaimsByGroup(name);
            } else {
                claims = getClaimsByPlayer(name);
            }
            int townSize = this.main.getConfig().getInt("town-block-size", 16);
            for (TownBlock claim : claims) {
                World world = claim.getWorldCoord().getBukkitWorld();
                int x = claim.getX() * townSize;
                int z = claim.getZ() * townSize;
                Location loc1 = new Location(world, x, 0, z);
                Location loc2 = new Location(world, x + 15, 0, z + 15);
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
    private Collection<TownBlock> getClaimsByPlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        Resident resident = api.getResident(player.getUniqueId());
        // assume made that if group is not enabled, player land comes from towns and not nations
        try {
            return resident.getTown().getTownBlocks();
        } catch (NotRegisteredException e) {
            return new HashSet<>();
        }
    }

    /**
     * Get claims based on group.
     *
     * @param name name of group to get claims for
     *
     * @return List of claims of group
     */
    private Collection<TownBlock> getClaimsByGroup(String name) {
        if (this.main.getConfig().getString("group-type").equalsIgnoreCase(
                "townyadvancedtown")) {
            Town town = api.getTown(name);
            return town.getTownBlocks();
        } else if (this.main.getConfig().getString("group-type").equalsIgnoreCase(
                "townyadvancednation")) {
            Nation nation = api.getNation(name);
            return nation.getTownBlocks();
        }

        List<OfflinePlayer> players = this.main.getGroupManager().getPlayers(name);
        Collection<TownBlock> claims = new HashSet<>();
        for (OfflinePlayer player : players) {
            Resident resident = api.getResident(player.getUniqueId());
            try {
                claims.addAll(resident.getTown().getTownBlocks());
            } catch (NotRegisteredException e) {
                continue;
            }
        }
        return claims;
    }
}
