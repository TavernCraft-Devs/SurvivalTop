package tk.taverncraft.survivaltop.commands;

import org.bukkit.command.CommandSender;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.logs.LogManager;
import tk.taverncraft.survivaltop.messages.MessageManager;
import tk.taverncraft.survivaltop.permissions.PermissionsManager;

/**
 * DumpCommand contains the execute method for when a user dumps logs for debugging.
 */
public class DumpCommand {
    private final Main main;
    private final PermissionsManager permissionsManager;

    /**
     * Constructor for DumpCommand.
     *
     * @param main plugin class
     */
    public DumpCommand(Main main) {
        this.main = main;
        this.permissionsManager = new PermissionsManager(main);
    }

    /**
     * Dumps plugin logs for user.
     *
     * @param sender user who sent the command
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender) {
        if (!permissionsManager.hasDumpCmdPerm(sender)) {
            return true;
        }

        LogManager logManager = main.getLogManager();
        if (logManager.isLogging()) {
            MessageManager.sendMessage(sender, "log-in-progress");
            return true;
        }

        logManager.startLogDump(sender);
        MessageManager.sendMessage(sender, "log-started");
        return true;
    }
}

