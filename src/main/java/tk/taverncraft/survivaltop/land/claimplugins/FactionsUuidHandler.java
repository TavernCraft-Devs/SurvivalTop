package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.land.processor.LandProcessor;
import tk.taverncraft.survivaltop.utils.types.ClaimInfo;

/**
 * Handles land wealth calculated using FactionsUuid plugin.
 */
public class FactionsUuidHandler implements LandClaimPluginHandler {
    private final Main main;
    private final LandProcessor landProcessor;

    /**
     * Constructor for FactionsUuidHandler.
     *
     * @param main plugin class
     * @param landProcessor helper for land calculations
     */
    public FactionsUuidHandler(Main main, LandProcessor landProcessor) {
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
        Set<FLocation> claims = getClaims(name);
        double height = main.getOptions().getMaxLandHeight() - main.getOptions().getMinLandHeight();
        long numBlocks = claims.size() * 16L * 16L * Double.valueOf(height).longValue();
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
            Set<FLocation> claims = getClaims(name);
            for (FLocation claim : claims) {
                landProcessor.processEntityChunk(id, claim.getChunk(), claim.getWorld());
            }
        } catch (NoClassDefFoundError | NullPointerException ignored) {
        }
    }

    /**
     * Gets the claim for entity.
     *
     * @param name name of entity
     */
    private Set<FLocation> getClaims(String name) {
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
    private Set<FLocation> getClaimsByPlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        FPlayer fPlayer = FPlayers.getInstance().getByOfflinePlayer(player);
        Faction faction = fPlayer.getFaction();
        return faction.getAllClaims();
    }

    /**
     * Gets claims based on group.
     *
     * @param name name of group to get claims for
     *
     * @return List of claims of group
     */
    private Set<FLocation> getClaimsByGroup(String name) {
        // todo: minor cleanup here to streamline checks
        String groupType = main.getOptions().getGroupType().toLowerCase();
        if (groupType.equals("factionsuuid") || groupType.equals("saberfactions")) {
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
