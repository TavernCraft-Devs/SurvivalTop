package tk.taverncraft.survivaltop.commands;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.messages.MessageManager;
import tk.taverncraft.survivaltop.utils.ValidationManager;

/**
 * StatsCommand contains the execute method for when a user views stats of self or others.
 */
public class StatsCommand {

    private final String statsSelfPerm = "survtop.stats.self";
    private final String statsOthersPerm = "survtop.stats.others";
    private final Main main;
    private final ValidationManager validationManager;

    /**
     * Constructor for StatsCommand.
     *
     * @param main plugin class
     */
    public StatsCommand(Main main) {
        this.main = main;
        this.validationManager = new ValidationManager(main);
    }

    /**
     * Checks if user is requesting stats for self or others and handles request accordingly.
     *
     * @param sender user who sent the command
     * @param args command arguments
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 2) {
            getStatsForOthers(sender, args);
        } else {
            getStatsForSelf(sender);
        }
        return true;
    }

    /**
     * Shows user stats for self.
     *
     * @param sender user who sent the command
     */
    private void getStatsForSelf(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageManager.sendMessage(sender, "player-only-command");
            return;
        }

        if (!validationManager.hasPermission(statsSelfPerm, sender)) {
            return;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        String name = player.getName();

        // if group is enabled, get name of the group the player belongs to instead
        if (this.main.groupIsEnabled()) {
            name = this.main.getGroupManager().getGroupOfPlayer(name);
            if (!this.validationManager.groupExist(name, sender)) {
                return;
            }
        }

        // check if there is an ongoing calculation task (guard against spam)
        if (main.getEntityStatsManager().senderHasCalculationInProgress(uuid)) {
            MessageManager.sendMessage(sender, "calculation-in-progress");
            return;
        }

        MessageManager.sendMessage(sender, "start-calculating-stats");
        if (main.isUseRealTimeStats()) {
            main.getEntityStatsManager().getRealTimeEntityStats(sender, uuid, name);
        } else {
            main.getEntityStatsManager().getCachedEntityStats(sender, uuid, name);
        }
    }

    /**
     * Shows user stats for others.
     *
     * @param sender user who sent the command
     * @param args command arguments
     */
    private void getStatsForOthers(CommandSender sender, String[] args) {
        String name = args[1];
        if (!validationManager.hasPermission(statsOthersPerm, sender)) {
            return;
        }

        // check if group/player provided exist
        if (this.main.groupIsEnabled()) {
            if (!this.validationManager.groupExist(args[1], sender)) {
                return;
            }
        } else {
            if (!this.validationManager.playerExist(args[1], sender)) {
                return;
            }
        }

        UUID uuid = main.getSenderUuid(sender);

        // check if there is an ongoing calculation task (guard against spam)
        if (main.getEntityStatsManager().senderHasCalculationInProgress(uuid)) {
            MessageManager.sendMessage(sender, "calculation-in-progress");
            return;
        }

        MessageManager.sendMessage(sender, "start-calculating-stats");
        if (main.isUseRealTimeStats()) {
            main.getEntityStatsManager().getRealTimeEntityStats(sender, uuid, name);
        } else {
            main.getEntityStatsManager().getCachedEntityStats(sender, uuid, name);
        }
    }
}
