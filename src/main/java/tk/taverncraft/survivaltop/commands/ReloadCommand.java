package tk.taverncraft.survivaltop.commands;

import org.bukkit.command.CommandSender;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.messages.MessageManager;
import tk.taverncraft.survivaltop.permissions.PermissionsManager;

/**
 * ReloadCommand contains the execute method for when a user inputs command to reload plugin.
 */
public class ReloadCommand {
    private final Main main;
    private final PermissionsManager permissionsManager;

    /**
     * Constructor for ReloadCommand.
     *
     * @param main plugin class
     */
    public ReloadCommand(Main main) {
        this.main = main;
        this.permissionsManager = new PermissionsManager(main);
    }

    /**
     * Reloads all files and re-initializes values.
     *
     * @param sender user who sent the command
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender) {
        if (!permissionsManager.hasReloadCmdPerm(sender)) {
            return true;
        }

        try {
            // stop existing player stats calculations
            main.getTaskManager().stopAllTasks();

            // reload configs and reinitialize options
            main.getConfigManager().createConfigs();
            main.getOptions().initializeOptions();

            // check dependencies
            if (!main.getDependencyManager().checkAllDependencies()) {
                MessageManager.sendMessage(sender, "reload-fail");
                return true;
            }

            // reinitialize manager values
            main.getStorageManager().initializeValues();
            main.getLandManager().setStopOperations(true);
            main.getLandManager().initializeLandOperations();
            main.getLandManager().initializeLandType();
            main.getInventoryManager().setStopOperations(true);
            main.getInventoryManager().initializeWorth();
            main.getPapiManager().initializePlaceholders();
            main.getGuiManager().initializeMenuOptions();
            main.getGroupManager().initializeLandType();
            main.getLogManager().stopExistingTasks();
            main.getCacheManager().initializeValues();
            main.getLeaderboardManager().stopExistingScheduleTasks();
            main.getLeaderboardManager().scheduleLeaderboardUpdate(
                    main.getOptions().getUpdateInterval(),
                    main.getOptions().getUpdateInterval()
            );

            MessageManager.sendMessage(sender, "reload-success");
        } catch (Exception e) {
            main.getLogger().info(e.getMessage());
            MessageManager.sendMessage(sender, "reload-fail");
        }
        return true;
    }
}