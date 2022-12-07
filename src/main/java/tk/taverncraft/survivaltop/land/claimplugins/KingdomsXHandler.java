package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.land.location.SimpleChunkLocation;
import org.kingdoms.constants.player.KingdomPlayer;
import org.kingdoms.main.Kingdoms;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.land.operations.LandOperationsHelper;

/**
 * Handles land wealth calculated using KingdomsX plugin.
 */
public class KingdomsXHandler implements LandClaimPluginHandler {
    private final Main main;
    private final LandOperationsHelper landOperationsHelper;

    /**
     * Constructor for KingdomsXHandler.
     *
     * @param main plugin class
     * @param landOperationsHelper helper for land calculations
     */
    public KingdomsXHandler(Main main, LandOperationsHelper landOperationsHelper) {
        this.main = main;
        this.landOperationsHelper = landOperationsHelper;
    }

    /**
     * Gets the claim info for an entity.
     *
     * @param name name of entity to get claim info for
     *
     * @return size 2 array with 1st element = number of claims and 2nd element = number of blocks
     */
    public Long[] getClaimsInfo(String name) {
        Set<SimpleChunkLocation> claims = getClaims(name);
        double height = main.getOptions().getMaxLandHeight() - main.getOptions().getMinLandHeight();
        long numBlocks = claims.size() * 16L * 16L * Double.valueOf(height).longValue();
        return new Long[]{(long) claims.size(), numBlocks};
    }

    /**
     * Processes the worth of a land.
     *
     * @param name name of entity to get land worth for
     * @param id key to identify task)
     */
    public void processEntityLand(String name, int id) {
        try {
            Set<SimpleChunkLocation> claims = getClaims(name);
            for (SimpleChunkLocation claim : claims) {
                landOperationsHelper.processEntityChunk(id, claim.toChunk(), claim.getBukkitWorld());
            }
        } catch (NoClassDefFoundError | NullPointerException ignored) {
        }
    }

    /**
     * Gets the claim for entity.
     *
     * @param name name of entity
     */
    private Set<SimpleChunkLocation> getClaims(String name) {
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
    private Set<SimpleChunkLocation> getClaimsByPlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        KingdomPlayer kPlayer = KingdomPlayer.getKingdomPlayer(player.getUniqueId());
        return kPlayer.getClaims();
    }

    /**
     * Gets claims based on group.
     *
     * @param name name of group to get claims for
     *
     * @return List of claims of group
     */
    private Set<SimpleChunkLocation> getClaimsByGroup(String name) {
        if (this.main.getConfig().getString("group-type").equalsIgnoreCase(
                "factionsuuid")) {
            Kingdom kingdom = Kingdoms.get().getDataHandlers().getKingdomManager().getData(name);
            return kingdom.getLandLocations();
        }

        List<OfflinePlayer> players = this.main.getGroupManager().getPlayers(name);
        Set<SimpleChunkLocation> claims = new HashSet<>();
        for (OfflinePlayer player : players) {
            KingdomPlayer kingdomPlayer = KingdomPlayer.getKingdomPlayer(player);
            Kingdom kingdom = kingdomPlayer.getKingdom();
            claims.addAll(kingdom.getLandLocations());
        }
        return claims;
    }
}
