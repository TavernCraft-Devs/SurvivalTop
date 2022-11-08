package tk.taverncraft.survivaltop.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.ui.InfoGui;
import tk.taverncraft.survivaltop.utils.MessageManager;
import tk.taverncraft.survivaltop.utils.ValidationManager;

/**
 * LandInfoCommand contains the execute method for when a user inputs command to view the value of
 * blocks/spawners/containers.
 */
public class LandInfoCommand {

    private final String landInfoPerm = "survtop.landinfo";
    private ValidationManager validationManager;

    /**
     * Constructor for LandInfoCommand.
     *
     * @param main plugin class
     */
    public LandInfoCommand(Main main) {
        this.validationManager = new ValidationManager(main);
    }

    /**
     * Shows all land info to user.
     *
     * @param sender user who sent the command
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender) {
        if (!validationManager.hasPermission(landInfoPerm, sender)) {
            return true;
        }

        // if sender is player, show info in a gui
        if (sender instanceof Player) {
            Inventory inv = InfoGui.mainPage;
            if (inv != null) {
                ((Player) sender).openInventory(InfoGui.mainPage);
            } else {
                MessageManager.sendMessage(sender, "unexpected-error");
            }
        } else {
            MessageManager.sendMessage(sender, "player-only-command");
        }
        return true;
    }

}
