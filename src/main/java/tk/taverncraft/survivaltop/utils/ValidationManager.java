package tk.taverncraft.survivaltop.utils;

import org.bukkit.command.CommandSender;

import tk.taverncraft.survivaltop.Main;

public class ValidationManager {
    Main main;

    public ValidationManager(Main main) {
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
