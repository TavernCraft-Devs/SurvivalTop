package tk.taverncraft.survivaltop.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

/**
 * CommandTabCompleter prompts users to tab complete the possible commands based on current input.
 */
public class CommandTabCompleter implements TabCompleter {
    private static final String[] COMMANDS = {
        "top",
        "stats",
        "iteminfo",
        "update",
        "help",
        "reload",
        "dump"
    };

    /**
     * Overridden method from TabCompleter, entry point for checking of user command to suggest
     * tab complete.
     *
     * @param sender user who sent command
     * @param cmd command which was executed
     * @param label alias of the command
     * @param args arguments following the command
     *
     * @return list of values as suggestions to tab complete for the user
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label,
            String[] args) {
        final List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMANDS), completions);
        } else if (args.length == 2 && args[1].equalsIgnoreCase("STATS")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                completions.add(p.getName());
            }
        }
        return completions;
    }
}
