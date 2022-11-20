package tk.taverncraft.survivaltop.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.gui.types.InfoGui;
import tk.taverncraft.survivaltop.messages.MessageManager;
import tk.taverncraft.survivaltop.utils.services.ValidationManager;

/**
 * ItemInfoCommand contains the execute method for when a user inputs command to view the value of
 * blocks/spawners/containers.
 */
public class ItemInfoCommand {
    private final Main main;
    private final String itemInfoPerm = "survtop.iteminfo";
    private final ValidationManager validationManager;

    /**
     * Constructor for ItemInfoCommand.
     *
     * @param main plugin class
     */
    public ItemInfoCommand(Main main) {
        this.main = main;
        this.validationManager = new ValidationManager(main);
    }

    /**
     * Shows all item info to user.
     *
     * @param sender user who sent the command
     *
     * @return true at end of execution
     */
    public boolean execute(CommandSender sender) {
        if (!validationManager.hasPermission(itemInfoPerm, sender)) {
            return true;
        }

        // if sender is player, show info in a gui
        if (sender instanceof Player) {
            Inventory inv = main.getGuiManager().getMainInfoPage();
            if (inv != null) {
                ((Player) sender).openInventory(inv);
            } else {
                MessageManager.sendMessage(sender, "unexpected-error");
            }
        } else {
            MessageManager.sendMessage(sender, "player-only-command");
        }
        return true;
    }

}
