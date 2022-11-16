package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

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
     * Processes the worth of a land.
     *
     * @param uuid uuid of sender if this is run through stats command; otherwise entities
     * @param name name of entity to get land worth for
     * @param isLeaderboardUpdate true if is a leaderboard update, false otherwise (i.e. stats)
     */
    public void processEntityLand(UUID uuid, String name, boolean isLeaderboardUpdate) {
        try {
            Set<SimpleChunkLocation> claims;
            if (this.main.groupIsEnabled()) {
                claims = getClaimsByGroup(name);
            } else {
                claims = getClaimsByPlayer(name);
            }
            for (SimpleChunkLocation claim : claims) {
                World world = claim.getBukkitWorld();
                int x = claim.getX() * 16;
                int z = claim.getZ() * 16;
                Location loc1 = new Location(world, x, 0, z);
                Location loc2 = new Location(world, x + 15, 0, z + 15);
                processEntityClaim(uuid, loc1, loc2, world, isLeaderboardUpdate);
            }
        } catch (NoClassDefFoundError | NullPointerException ignored) {
        }
    }

    /**
     * Processes the worth of a claim identified between 2 locations.
     *
     * @param uuid uuid of sender if this is run through stats command; otherwise entities
     * @param l1 location 1
     * @param l2 location 2
     * @param world world that the claim is in
     * @param isLeaderboardUpdate true if is a leaderboard update, false otherwise (i.e. stats)
     */
    public void processEntityClaim(UUID uuid, Location l1, Location l2, World world,
            boolean isLeaderboardUpdate) {
        double minX = Math.min(l1.getX(), l2.getX());
        double minY = this.main.getMinHeight();
        double minZ = Math.min(l1.getZ(), l2.getZ());
        double maxX = Math.max(l1.getX(), l2.getX()) + 1;
        double maxY = this.main.getMaxHeight();
        double maxZ = Math.max(l1.getZ(), l2.getZ()) + 1;
        landOperationsHelper.processEntityClaim(uuid, maxX, minX, maxY, minY, maxZ, minZ, world,
                isLeaderboardUpdate);
    }

    /**
     * Get claims based on player.
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
     * Get claims based on group.
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
