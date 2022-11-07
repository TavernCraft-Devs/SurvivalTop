package tk.taverncraft.survivaltop.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.bukkit.entity.Player;
import tk.taverncraft.survivaltop.Main;

/**
 * CommandParser contains the onCommand method that handles user command input.
 */
public class CommandParser implements CommandExecutor {
    Main main;

    /**
     * Constructor for CommandParser.
     */
    public CommandParser(Main main) {
        this.main = main;
    }

    /**
     * Entry point of commands.
     */
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (cmd.getName().equalsIgnoreCase("survivaltop")) {

            // if no arguments provided or is null, return invalid command
            if (args.length == 0) {
                return new InvalidCommand(this.main).execute(sender);
            }

            final String chatCmd = args[0];

            if (chatCmd == null) {
                return new InvalidCommand(this.main).execute(sender);
            }

            // command to view own or others' stats
            if (chatCmd.equals("stats")) {
                if (args.length == 2) {
                    return new StatsCommand(this.main).execute(sender, args);
                } else {
                    return new StatsCommand(this.main).execute(sender);
                }
            }

            // command to view wealth leaderboard
            if (chatCmd.equals("top")) {
                return new TopCommand(this.main).execute(sender, args);
            }

            // command to view blockinfo
            if (chatCmd.equals("landinfo")) {
                return new LandInfoCommand(this.main).execute(sender);
            }

            // command to manually trigger leaderboard update
            if (chatCmd.equals("update")) {
                return new UpdateCommand(this.main).execute(sender);
            }

            // command to view all commands
            if (chatCmd.equals("help")) {
                return new HelpCommand(this.main).execute(sender);
            }

            // command to reload plugin
            if (chatCmd.equals("reload")) {
                return new ReloadCommand(this.main).execute(sender);
            }

            // special command for stats inventory view
            if (chatCmd.equals("openstatsinv") && sender instanceof Player) {
                main.getEntityStatsManager().openMainStatsPage(((Player) sender).getUniqueId());
                return true;
            }

            return new InvalidCommand(this.main).execute(sender);
        }
        return true;
    }
}
