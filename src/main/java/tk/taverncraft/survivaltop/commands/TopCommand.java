package tk.taverncraft.survivaltop.commands;

import org.bukkit.command.CommandSender;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.messages.MessageManager;
import tk.taverncraft.survivaltop.permissions.PermissionsManager;

/**
 * TopCommand contains the execute method for when a user views the leaderboard.
 */
public class TopCommand {
    private final Main main;
    private final PermissionsManager permissionsManager;

    /**
     * Constructor for TopCommand.
     *
     * @param main plugin class
     */
    public TopCommand(Main main) {
        this.main = main;
        this.permissionsManager = new PermissionsManager(main);
    }

    /**
     * Shows leaderboard to user.
     *
     * @param sender user who sent the command
     * @param args command args possibly containing page number
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender, String[] args) {
        if (!permissionsManager.hasTopCmdPerm(sender)) {
            return true;
        }

        // show first page if no page number provided
        int pageNum = 1;
        try {
            pageNum = Integer.parseInt(args[1]);
        } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
        }
        if (main.getOptions().isUseInteractiveLeaderboard()) {
            MessageManager.showInteractiveLeaderboard(sender, pageNum);
        } else {
            MessageManager.showLeaderboard(sender, pageNum,
                    main.getOptions().getLeaderboardPositionsPerPage());
        }
        return true;
    }
}

