package tk.taverncraft.survivaltop.commands;

import org.bukkit.command.CommandSender;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.utils.MessageManager;
import tk.taverncraft.survivaltop.utils.ValidationManager;

/**
 * TopCommand contains the execute method for when a user views the wealth leaderboard.
 */
public class TopCommand {

    private final String topPerm = "survtop.top";
    Main main;
    ValidationManager validationManager;

    /**
     * Constructor for TopCommand.
     */
    public TopCommand(Main main) {
        this.main = main;
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

        try {
            int pageNum = Integer.parseInt(args[1]);
            MessageManager.showLeaderboard(sender, pageNum);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            MessageManager.showLeaderboard(sender, 1);
        }
        return true;
    }
}

