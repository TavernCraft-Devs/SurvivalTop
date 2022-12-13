package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

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
import tk.taverncraft.survivaltop.land.processor.LandProcessor;
import tk.taverncraft.survivaltop.utils.types.ClaimInfo;

/**
 * Handles land wealth calculated using Towny Advanced plugin.
 */
public class TownyAdvancedHandler implements LandClaimPluginHandler {
    private final Main main;
    private final LandProcessor landProcessor;
    private final TownyAPI api;

    /**
     * Constructor for TownyAdvancedHandler.
     *
     * @param main plugin class
     * @param landProcessor helper for land calculations
     */
    public TownyAdvancedHandler(Main main, LandProcessor landProcessor) {
        this.main = main;
        this.landProcessor = landProcessor;
        this.api = TownyAPI.getInstance();
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
        Collection<TownBlock> claims = getClaims(name);
        int townSize = main.getOptions().getTownBlockSize();
        for (TownBlock claim : claims) {
            double minX = claim.getX() * townSize;
            double minY = this.main.getOptions().getMinLandHeight();
            double minZ = claim.getZ() * townSize;
            double maxX = minX + townSize;
            double maxY = this.main.getOptions().getMaxLandHeight();
            double maxZ = minZ + townSize;
            numBlocks += (maxX - minX) * (maxZ - minZ) * (maxY - minY);
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
            Collection<TownBlock> claims = getClaims(name);
            int townSize = main.getOptions().getTownBlockSize();
            for (TownBlock claim : claims) {
                World world = claim.getWorldCoord().getBukkitWorld();
                double minX = claim.getX() * townSize;
                double minZ = claim.getZ() * townSize;
                double maxX = minX + townSize;
                double maxZ = minZ + townSize;
                processEntityClaim(id, maxX, maxZ, minX, minZ, world);
            }
        } catch (NoClassDefFoundError | NullPointerException ignored) {
        }
    }

    /**
     * Processes the worth of a claim identified between 2 locations.
     *
     * @param id key to identify task
     * @param maxX max value of x
     * @param maxZ max value of z
     * @param minX min value of x
     * @param minZ min value of z
     * @param world world that the claim is in
     */
    public void processEntityClaim(int id, double maxX, double maxZ, double minX, double minZ,
            World world) {
        double minY = this.main.getOptions().getMinLandHeight();
        double maxY = this.main.getOptions().getMaxLandHeight();
        landProcessor.processEntityClaim(id, maxX, minX, maxY, minY, maxZ, minZ, world);
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
     * Gets claims based on player.
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
     * Gets claims based on group.
     *
     * @param name name of group to get claims for
     *
     * @return List of claims of group
     */
    private Collection<TownBlock> getClaimsByGroup(String name) {
        if (main.getOptions().getGroupType().equalsIgnoreCase("townyadvancedtown")) {
            Town town = api.getTown(name);
            return town.getTownBlocks();
        } else if (main.getOptions().getGroupType().equalsIgnoreCase(
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
