package tk.taverncraft.survivaltop.commands;

import org.bukkit.command.CommandSender;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.messages.MessageManager;
import tk.taverncraft.survivaltop.permissions.PermissionsManager;

/**
 * UpdateCommand contains the execute method for when a user wishes to manually trigger a
 * leaderboard update.
 */
public class UpdateCommand {
    private final Main main;
    private final PermissionsManager permissionsManager;

    /**
     * Constructor for UpdateCommand.
     *
     * @param main plugin class
     */
    public UpdateCommand(Main main) {
        this.main = main;
        this.permissionsManager = new PermissionsManager(main);
    }

    /**
     * Updates the leaderboard.
     *
     * @param sender user who sent the command
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender) {
        if (!permissionsManager.hasUpdateCmdPerm(sender)) {
            return true;
        }

        // check if there is an existing update ongoing (guard against spam)
        if (main.getLeaderboardManager().isUpdating()) {
            MessageManager.sendMessage(sender, "update-in-progress");
            return true;
        }

        main.getLeaderboardManager().doManualLeaderboardUpdate(sender);
        return true;
    }
}
