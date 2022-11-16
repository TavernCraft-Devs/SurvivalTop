package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.land.operations.LandOperationsHelper;

/**
 * Handles land wealth calculated using Towny Advanced plugin.
 */
public class TownyAdvancedHandler implements LandClaimPluginHandler {
    private final Main main;
    private final LandOperationsHelper landOperationsHelper;
    private final TownyAPI api;

    /**
     * Constructor for TownyAdvancedHandler.
     *
     * @param main plugin class
     * @param landOperationsHelper helper for land calculations
     */
    public TownyAdvancedHandler(Main main, LandOperationsHelper landOperationsHelper) {
        this.main = main;
        this.landOperationsHelper = landOperationsHelper;
        this.api = TownyAPI.getInstance();
    }

    /**
     * Gets the claim info for an entity.
     *
     * @param name name of entity to get claim info for
     *
     * @return size 2 array with 1st element = number of claims and 2nd element = number of blocks
     */
    public Long[] getClaimsInfo(String name) {
        long numBlocks = 0;
        Collection<TownBlock> claims = getClaims(name);
        int townSize = this.main.getConfig().getInt("town-block-size", 16);
        for (TownBlock claim : claims) {
            double minX = claim.getX() * townSize;
            double minY = this.main.getOptions().getMinLandHeight();
            double minZ = claim.getZ() * townSize;
            double maxX = minX + townSize;
            double maxY = this.main.getOptions().getMaxLandHeight();
            double maxZ = minZ + townSize;
            numBlocks += (maxX - minX) * (maxZ - minZ) * (maxY - minY);
        }
        return new Long[]{(long) claims.size(), numBlocks};
    }

    /**
     * Processes the worth of a land.
     *
     * @param uuid uuid of sender if this is run through stats command; otherwise entities
     * @param name name of entity to get land worth for
     * @param isLeaderboardUpdate true if is a leaderboard update, false otherwise (i.e. stats)
     */
    public void processEntityLand(UUID uuid, String name, boolean isLeaderboardUpdate) {
        try {
            Collection<TownBlock> claims = getClaims(name);
            int townSize = this.main.getConfig().getInt("town-block-size", 16);
            for (TownBlock claim : claims) {
                World world = claim.getWorldCoord().getBukkitWorld();
                double minX = claim.getX() * townSize;
                double minZ = claim.getZ() * townSize;
                double maxX = minX + townSize;
                double maxZ = minZ + townSize;
                processEntityClaim(uuid, maxX, maxZ, minX, minZ, world, isLeaderboardUpdate);
            }
        } catch (NoClassDefFoundError | NullPointerException ignored) {
        }
    }

    /**
     * Processes the worth of a claim identified between 2 locations.
     *
     * @param uuid uuid of sender if this is run through stats command; otherwise entities
     * @param maxX max value of x
     * @param maxZ max value of z
     * @param minX min value of x
     * @param minZ min value of z
     * @param world world that the claim is in
     * @param isLeaderboardUpdate true if is a leaderboard update, false otherwise (i.e. stats)
     */
    public void processEntityClaim(UUID uuid, double maxX, double maxZ, double minX, double minZ,
            World world, boolean isLeaderboardUpdate) {
        double minY = this.main.getOptions().getMinLandHeight();
        double maxY = this.main.getOptions().getMaxLandHeight();
        landOperationsHelper.processEntityClaim(uuid, maxX, minX, maxY, minY, maxZ, minZ, world,
                isLeaderboardUpdate);
    }

    /**
     * Gets the claim for entity.
     *
     * @param name name of entity
     */
    private Collection<TownBlock> getClaims(String name) {
        if (this.main.getOptions().groupIsEnabled()) {
            return getClaimsByGroup(name);
        } else {
            return getClaimsByPlayer(name);
        }
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
            } catch (NotRegisteredException ignored) {
            }
        }
        return claims;
    }
}
