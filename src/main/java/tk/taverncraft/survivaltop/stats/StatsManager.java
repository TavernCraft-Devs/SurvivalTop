package tk.taverncraft.survivaltop.stats;

import java.time.Instant;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.gui.types.StatsGui;
import tk.taverncraft.survivaltop.cache.EntityCache;
import tk.taverncraft.survivaltop.messages.MessageManager;

import static tk.taverncraft.survivaltop.task.queue.TaskType.PLAYER;

/**
 * StatsManager handles all logic for getting entity (player/group) stats but does not
 * store any information. Information storage belongs to the overall ServerStatsManager.
 */
public class StatsManager {
    private final Main main;

    /**
     * Constructor for StatsManager.
     *
     * @param main plugin class
     */
    public StatsManager(Main main) {
        this.main = main;
    }

    /**
     * Gets stats for an entity for sender who requested via stats command.
     *
     * @param sender sender who requested for stats
     * @param name name of entity to get stats for
     */
    public void getStatsForPlayer(CommandSender sender, String name) {
        if (main.getOptions().isCalculationMode0()) {
            MessageManager.sendMessage(sender, "start-calculating-stats");
            main.getTaskManager().createTask(sender, name, PLAYER);
        } else {
            getCachedStats(sender, name);
        }
    }

    /**
     * Entry point for getting the cached stats of an entity.
     *
     * @param sender sender who requested for stats
     * @param name name of entity
     */
    public void getCachedStats(CommandSender sender, String name) {
        EntityCache eCache = main.getCacheManager().getLatestCache(name.toUpperCase());
        // if stats cache not found or invalid, look into leaderboard cache
        if (eCache == null) {
            if (main.getOptions().isCalculationMode1()) {
                MessageManager.sendMessage(sender, "start-calculating-stats");
                main.getTaskManager().createTask(sender, name, PLAYER);
            } else {
                MessageManager.sendMessage(sender, "no-updated-leaderboard");
            }
            return;
        }

        if (main.getOptions().isUseGuiStats() && sender instanceof Player) {
            eCache.setGui(main);
        } else {
            eCache.setChat();
        }
        main.getCacheManager().saveToStatsCache(name.toUpperCase(), eCache);

        long timeElapsed = Instant.now().getEpochSecond() - eCache.getCacheTime();
        if (main.getOptions().isUseGuiStats() && sender instanceof Player) {
            MessageManager.sendMessage(sender, "calculation-complete-cache",
                new String[]{"%time%"},
                new String[]{String.valueOf(timeElapsed)});
            MessageManager.sendGuiStatsReadyMessage(sender, name.toUpperCase());
        } else {
            MessageManager.sendMessage(sender, "calculation-complete-cache",
                new String[]{"%time%"},
                new String[]{String.valueOf(timeElapsed)});
            MessageManager.sendMessage(sender, "entity-stats", eCache.getPlaceholders(),
                eCache.getValues());
        }
    }
}


