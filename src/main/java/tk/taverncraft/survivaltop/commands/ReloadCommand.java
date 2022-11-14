package tk.taverncraft.survivaltop.commands;

import org.bukkit.command.CommandSender;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.ui.InfoGui;
import tk.taverncraft.survivaltop.utils.MessageManager;
import tk.taverncraft.survivaltop.utils.ValidationManager;

/**
 * ReloadCommand contains the execute method for when a user inputs command to reload plugin.
 */
public class ReloadCommand {

    private final String reloadPerm = "survtop.reload";
    private Main main;
    private ValidationManager validationManager;

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

        if (main.getLeaderboardManager().isUpdating()) {
            MessageManager.sendMessage(sender, "update-in-progress");
            return true;
        }

        try {
            // stop existing player stats calculations
            main.getEntityStatsManager().stopEntityStatsCalculations();

            // reload files
            main.createConfigs();
            main.setOptions();

            // check dependencies
            if (!main.getDependencyManager().checkAllDependencies()) {
                MessageManager.sendMessage(sender, "reload-fail");
                return true;
            }

            // reinitialize manager values
            main.getStorageManager().initializeValues();
            main.getServerStatsManager().initializeValues();
            main.getLandManager().initializeCalculationType();
            main.getLandManager().initializeLandOperations();
            main.getLandManager().initializeLandType();
            main.getInventoryManager().initializeWorth();
            new InfoGui(main);
            main.getGroupManager().initializeLandType();
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