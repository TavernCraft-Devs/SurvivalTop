package tk.taverncraft.survivaltop.commands;

import org.bukkit.command.CommandSender;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.utils.MessageManager;
import tk.taverncraft.survivaltop.utils.ValidationManager;

/**
 * HelpCommand contains the execute method for when a user inputs the command to get help for the plugin.
 */
public class HelpCommand {

    private final String helpPerm = "survtop.help";
    private ValidationManager validationManager;

    /**
     * Constructor for HelpCommand.
     *
     * @param main plugin class
     */
    public HelpCommand(Main main) {
        this.validationManager = new ValidationManager(main);
    }

    /**
     * Shows a list of helpful commands to the user.
     *
     * @param sender user who sent the command
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender) {
        if (!validationManager.hasPermission(helpPerm, sender)) {
            return true;
        }

        MessageManager.sendMessage(sender, "help-text");
        return true;
    }
}

