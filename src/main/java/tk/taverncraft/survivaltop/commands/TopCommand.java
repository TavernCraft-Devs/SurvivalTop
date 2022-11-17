package tk.taverncraft.survivaltop.commands;

import org.bukkit.command.CommandSender;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.messages.MessageManager;
import tk.taverncraft.survivaltop.utils.services.ValidationManager;

/**
 * TopCommand contains the execute method for when a user views the leaderboard.
 */
public class TopCommand {

    private final String topPerm = "survtop.top";
    private final ValidationManager validationManager;

    /**
     * Constructor for TopCommand.
     *
     * @param main plugin class
     */
    public TopCommand(Main main) {
        this.validationManager = new ValidationManager(main);
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
        if (!validationManager.hasPermission(topPerm, sender)) {
            return true;
        }

        // show first page if no page number provided
        try {
            int pageNum = Integer.parseInt(args[1]);
            MessageManager.showLeaderboard(sender, pageNum);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            MessageManager.showLeaderboard(sender, 1);
        }
        return true;
    }
}

