package tk.taverncraft.survivaltop.permissions;

import org.bukkit.command.CommandSender;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.messages.MessageManager;

/**
 * PermissionsManager performs basic validation operations.
 */
public class PermissionsManager {
    private final Main main;

    private final String dumpCmdPerm = "survtop.dump";
    private final String helpCmdPerm = "survtop.help";
    private final String itemInfoCmdPerm = "survtop.iteminfo";
    private final String reloadCmdPerm = "survtop.reload";
    private final String statsSelfCmdPerm = "survtop.stats.self";
    private final String statsOthersCmdPerm = "survtop.stats.others";
    private final String topCmdPerm = "survtop.top";
    private final String updateCmdPerm = "survtop.update";
    private final String signRemovePerm = "survtop.sign.removeTask";
    private final String signAddPerm = "survtop.sign.addTask";
    private final String guiDetailsSelfPerm = "survtop.gui.details.self";
    private final String guiDetailsOthersPerm = "survtop.gui.details.others";

    /**
     * Constructor for PermissionsManager.
     *
     * @param main plugin class
     */
    public PermissionsManager(Main main) {
        this.main = main;
    }

    /**
     * Validates if sender has permission and sends a message if not.
     *
     * @param permission permission node to check for
     * @param sender the player executing the command
     */
    public boolean hasPermission(String permission, CommandSender sender) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        MessageManager.sendMessage(sender, "no-permission");
        return false;
    }

    public boolean hasDumpCmdPerm(CommandSender sender) {
        return hasPermission(dumpCmdPerm, sender);
    }

    public boolean hasHelpCmdPerm(CommandSender sender) {
        return hasPermission(helpCmdPerm, sender);
    }

    public boolean hasItemInfoCmdPerm(CommandSender sender) {
        return hasPermission(itemInfoCmdPerm, sender);
    }

    public boolean hasReloadCmdPerm(CommandSender sender) {
        return hasPermission(reloadCmdPerm, sender);
    }

    public boolean hasStatsSelfCmdPerm(CommandSender sender) {
        return hasPermission(statsSelfCmdPerm, sender);
    }

    public boolean hasStatsOthersCmdPerm(CommandSender sender) {
        return hasPermission(statsOthersCmdPerm, sender);
    }

    public boolean hasTopCmdPerm(CommandSender sender) {
        return hasPermission(topCmdPerm, sender);
    }

    public boolean hasUpdateCmdPerm(CommandSender sender) {
        return hasPermission(updateCmdPerm, sender);
    }

    public boolean hasSignAddPerm(CommandSender sender) {
        return hasPermission(signAddPerm, sender);
    }

    public boolean hasSignRemovePerm(CommandSender sender) {
        return hasPermission(signRemovePerm, sender);
    }

    public boolean hasGuiDetailsSelfPerm(CommandSender sender) {
        return hasPermission(guiDetailsSelfPerm, sender);
    }

    public boolean hasGuiDetailsOthersPerm(CommandSender sender) {
        return hasPermission(guiDetailsOthersPerm, sender);
    }

    /**
     * Validates if inputted player exist and sends a message if not.
     *
     * @param name the name of the player to check for
     * @param sender the player executing the command
     */
    public boolean playerExist(String name, CommandSender sender) {
        if (name.length() > 16 || this.main.getServer().getOfflinePlayer(name).getFirstPlayed() == 0L) {
            MessageManager.sendMessage(sender, "entity-not-exist",
                new String[]{"%entity%"},
                new String[]{name});
            return false;
        }
        return true;
    }

    /**
     * Validates if inputted group exist and sends a message if not.
     *
     * @param name the name of the group to check for
     * @param sender the player executing the command
     */
    public boolean groupExist(String name, CommandSender sender) {
        if (!this.main.getGroupManager().isValidGroup(name)) {
            MessageManager.sendMessage(sender, "entity-not-exist",
                new String[]{"%entity%"},
                new String[]{name});
            return false;
        }
        return true;
    }
}
