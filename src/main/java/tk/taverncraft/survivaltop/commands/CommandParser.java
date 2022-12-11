package tk.taverncraft.survivaltop.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import tk.taverncraft.survivaltop.Main;

/**
 * CommandParser contains the onCommand method that handles user command input.
 */
public class CommandParser implements CommandExecutor {
    private final Main main;

    /**
     * Constructor for CommandParser.
     *
     * @param main plugin class
     */
    public CommandParser(Main main) {
        this.main = main;
    }

    /**
     * Entry point of commands.
     *
     * @param sender user who sent command
     * @param cmd command which was executed
     * @param label alias of the command
     * @param args arguments following the command
     *
     * @return true at end of execution
     */
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label,
            final String[] args) {

        // if no arguments provided or is null, return invalid command
        if (args.length == 0) {
            return new InvalidCommand().execute(sender);
        }

        final String chatCmd = args[0];

        if (chatCmd == null) {
            return new InvalidCommand().execute(sender);
        }

        switch (chatCmd.toUpperCase()) {

        // command to view own or others' stats
        case "STATS":
            return new StatsCommand(main).execute(sender, args);

        // command to view wealth leaderboard
        case "TOP":
            return new TopCommand(main).execute(sender, args);

        // command to view item info
        case "ITEMINFO":
            return new ItemInfoCommand(main).execute(sender);

        // command to manually trigger leaderboard update
        case "UPDATE":
            return new UpdateCommand(main).execute(sender);

        // command to view all commands
        case "HELP":
            return new HelpCommand(main).execute(sender);

        // command to reload plugin
        case "RELOAD":
            return new ReloadCommand(main).execute(sender);

        // command to dump logs for debugging
        case "DUMP":
            return new DumpCommand(main).execute(sender);

        // special command for stats inventory view
        case "OPENSTATSGUI":
            if (args.length == 2) {
                main.getGuiManager().getMainStatsPage(main.getSenderUuid(sender), args[1]);
            }
            return true;

        // all other cases treated as invalid
        default:
            return new InvalidCommand().execute(sender);
        }
    }
}
