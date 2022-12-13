package tk.taverncraft.survivaltop.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.messages.MessageManager;
import tk.taverncraft.survivaltop.permissions.PermissionsManager;

/**
 * StatsCommand contains the execute method for when a user views stats of self or others.
 */
public class StatsCommand {
    private final Main main;
    private final PermissionsManager permissionsManager;

    /**
     * Constructor for StatsCommand.
     *
     * @param main plugin class
     */
    public StatsCommand(Main main) {
        this.main = main;
        this.permissionsManager = new PermissionsManager(main);
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

        if (!permissionsManager.hasStatsSelfCmdPerm(sender)) {
            return;
        }

        Player player = (Player) sender;
        String name = player.getName();

        // if group is enabled, get name of the group the player belongs to instead
        if (main.getOptions().groupIsEnabled()) {
            name = main.getGroupManager().getGroupOfPlayer(name);
            if (!permissionsManager.groupExist(name, sender)) {
                return;
            }
        }

        // check if there is an ongoing calculation task (guard against spam)
        if (main.getTaskManager().hasCreator(sender)) {
            MessageManager.sendMessage(sender, "calculation-in-progress");
            return;
        }

        main.getStatsManager().getStatsForPlayer(sender, name);
    }

    /**
     * Shows user stats for others.
     *
     * @param sender user who sent the command
     * @param args command arguments
     */
    private void getStatsForOthers(CommandSender sender, String[] args) {
        String name = args[1];
        if (!permissionsManager.hasStatsOthersCmdPerm(sender)) {
            return;
        }

        // check if group/player provided exist
        if (this.main.getOptions().groupIsEnabled()) {
            if (!permissionsManager.groupExist(args[1], sender)) {
                return;
            }
        } else {
            if (!permissionsManager.playerExist(args[1], sender)) {
                return;
            }
        }

        // check if there is an ongoing calculation task (guard against spam)
        if (main.getTaskManager().hasCreator(sender)) {
            MessageManager.sendMessage(sender, "calculation-in-progress");
            return;
        }

        main.getStatsManager().getStatsForPlayer(sender, name);
    }
}
