package tk.taverncraft.survivaltop.commands;

import org.bukkit.command.CommandSender;

import tk.taverncraft.survivaltop.messages.MessageManager;

/**
 * InvalidCommand contains the execute method for when a user inputs an unrecognised command.
 */
public class InvalidCommand {

    /**
     * Constructor for InvalidCommand.
     */
    public InvalidCommand() {}

    /**
     * Handles all invalid commands from the user.
     *
     * @param sender user who sent the command
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender) {
        MessageManager.sendMessage(sender, "invalid-command");
        return true;
    }
}

