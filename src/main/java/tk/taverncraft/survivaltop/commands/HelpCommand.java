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
    Main main;
    ValidationManager validationManager;

    /**
     * Constructor for HelpCommand.
     */
    public HelpCommand(Main main) {
        this.main = main;
        this.validationManager = new ValidationManager(main);
    }

    /**
     * Shows a list of commands to the user.
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

