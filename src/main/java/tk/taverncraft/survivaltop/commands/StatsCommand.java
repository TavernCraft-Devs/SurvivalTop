package tk.taverncraft.survivaltop.commands;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.utils.MessageManager;
import tk.taverncraft.survivaltop.utils.ValidationManager;

/**
 * StatsCommand contains the execute method for when a user views stats of self or others.
 */
public class StatsCommand {

    private final String statsSelfPerm = "survtop.stats.self";
    private final String statsOthersPerm = "survtop.stats.others";
    Main main;
    ValidationManager validationManager;

    /**
     * Constructor for StatsCommand.
     */
    public StatsCommand(Main main) {
        this.main = main;
        this.validationManager = new ValidationManager(main);
    }

    /**
     * Shows user own stats.
     *
     * @param sender user who sent the command
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageManager.sendMessage(sender, "player-only-command");
            return true;
        }

        if (!validationManager.hasPermission(statsSelfPerm, sender)) {
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        String name;
        if (this.main.groupIsEnabled()) {
            name = this.main.getGroupManager().getGroupOfPlayer(player.getName());
            if (!this.validationManager.groupExist(name, sender)) {
                return true;
            }
        } else {
            name = player.getName();
        }

        if (main.getEntityStatsManager().senderHasCalculationInProgress(uuid)) {
            MessageManager.sendMessage(sender, "calculation-in-progress");
            return true;
        }

        main.getEntityStatsManager().setCalculatingStats(sender);
        MessageManager.sendMessage(sender, "start-calculating-stats");
        main.getEntityStatsManager().getEntityStats(sender, name);
        return true;
    }

    /**
     * Views the stats of another entity.
     *
     * @param sender user who sent the command
     * @param args command arguments
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender, String[] args) {
        String name = args[1];
        if (!validationManager.hasPermission(statsOthersPerm, sender)) {
            return true;
        }

        if (this.main.groupIsEnabled()) {
            if (!this.validationManager.groupExist(args[1], sender)) {
                return true;
            }
        } else {
            if (!this.validationManager.playerExist(args[1], sender)) {
                return true;
            }
        }

        UUID uuid;
        if (sender instanceof Player) {
            uuid = ((Player) sender).getUniqueId();
        } else {
            uuid = main.getConsoleUuid();
        }

        if (main.getEntityStatsManager().senderHasCalculationInProgress(uuid)) {
            MessageManager.sendMessage(sender, "calculation-in-progress");
            return true;
        }

        main.getEntityStatsManager().setCalculatingStats(sender);
        MessageManager.sendMessage(sender, "start-calculating-stats");
        main.getEntityStatsManager().getEntityStats(sender, name);
        return true;
    }
}
