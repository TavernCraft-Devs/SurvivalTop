package tk.taverncraft.survivaltop.commands;

import org.bukkit.command.CommandSender;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.ui.InfoGui;
import tk.taverncraft.survivaltop.messages.MessageManager;
import tk.taverncraft.survivaltop.utils.services.ValidationManager;

/**
 * ReloadCommand contains the execute method for when a user inputs command to reload plugin.
 */
public class ReloadCommand {

    private final String reloadPerm = "survtop.reload";
    private final Main main;
    private final ValidationManager validationManager;

    /**
     * Constructor for ReloadCommand.
     *
     * @param main plugin class
     */
    public ReloadCommand(Main main) {
        this.main = main;
        this.validationManager = new ValidationManager(main);
    }

    /**
     * Reloads all files and re-initializes values.
     *
     * @param sender user who sent the command
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender) {
        if (!validationManager.hasPermission(reloadPerm, sender)) {
            return true;
        }

        try {
            // stop existing player stats calculations
            main.getEntityStatsManager().stopEntityStatsCalculations();

            // reload configs and reinitialize options
            main.createConfigs();
            main.getOptions().initializeOptions();

            // check dependencies
            if (!main.getDependencyManager().checkAllDependencies()) {
                MessageManager.sendMessage(sender, "reload-fail");
                return true;
            }

            // reinitialize manager values
            main.getStorageManager().initializeValues();
            main.getServerStatsManager().initializeValues();
            main.getLandManager().initializeLandOperations();
            main.getLandManager().initializeLandType();
            main.getLandManager().doCleanUpForLeaderboard();
            main.getInventoryManager().initializeWorth();
            main.getInventoryManager().doCleanUpForLeaderboard();
            new InfoGui(main);
            main.getGroupManager().initializeLandType();
            main.getLogManager().stopExistingTasks();
            main.getLeaderboardManager().stopExistingTasks();
            main.getLeaderboardManager().scheduleLeaderboardUpdate(
                    main.getConfig().getInt("update-interval"),
                    main.getConfig().getInt("update-interval")
            );

            MessageManager.sendMessage(sender, "reload-success");
        } catch (Exception e) {
            main.getLogger().info(e.getMessage());
            MessageManager.sendMessage(sender, "reload-fail");
        }
        return true;
    }
}