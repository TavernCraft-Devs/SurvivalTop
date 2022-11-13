package tk.taverncraft.survivaltop.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.ChatPaginator;

import tk.taverncraft.survivaltop.stats.cache.EntityCache;

import static org.bukkit.util.ChatPaginator.*;

/**
 * MessageManager handles all formatting and sending of messages to the command sender.
 */
public class MessageManager {
    private static final HashMap<String, String> messageKeysMap = new HashMap<>();

    private static String completeLeaderboard;

    /**
     * Sets the messages to use.
     *
     * @param lang the configuration to base the messages on
     */
    public static void setMessages(FileConfiguration lang) {
        Set<String> messageKeysSet = lang.getConfigurationSection("").getKeys(false);

        for (String messageKey : messageKeysSet) {
            messageKeysMap.put(messageKey, ChatColor.translateAlternateColorCodes('&',
                    lang.get(messageKey).toString() + " "));
        }
    }

    /**
     * Sends message to the sender.
     *
     * @param sender sender to send message to
     * @param messageKey key to get message with
     */
    public static void sendMessage(CommandSender sender, String messageKey) {
        String message = getMessage(messageKey);
        sender.sendMessage(message);
    }

    /**
     * Sends message to the sender, replacing placeholders.
     *
     * @param sender sender to send message to
     * @param messageKey key to get message with
     * @param keys placeholder keys
     * @param values placeholder values
     */
    public static void sendMessage(CommandSender sender, String messageKey, String[] keys,
            String[] values) {
        String message = getMessage(messageKey);
        for (int i = 0; i < keys.length; i++) {
            message = message.replaceAll(keys[i], values[i]);
        }
        sender.sendMessage(message);
    }

    /**
     * Retrieves message value given the message key.
     *
     * @param messageKey key to retrieve message with
     */
    public static String getMessage(String messageKey) {
        String prefix = messageKeysMap.get("prefix");
        return prefix.substring(0, prefix.length() - 1) + messageKeysMap.get(messageKey);
    }

    public static String getSignFormat(String[] keys, String[] values) {
        String message = messageKeysMap.get("leaderboard-sign");
        for (int i = 0; i < keys.length; i++) {
            message = message.replaceAll(keys[i], values[i]);
        }
        return message;
    }

    /**
     * Shows leaderboard to the user.
     *
     * @param sender sender to send message to
     * @param pageNum page number of leaderboard
     */
    public static void showLeaderboard(CommandSender sender, int pageNum) {
        if (completeLeaderboard == null) {
            sendMessage(sender, "no-updated-leaderboard");
            return;
        }

        int linesPerPage = 12;
        ChatPaginator.ChatPage page = ChatPaginator.paginate(completeLeaderboard, pageNum,
                GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH, linesPerPage);
        for (String line : page.getLines()) {
            sender.sendMessage(line);
        }
    }

    /**
     * Sets up message for leaderboard beforehand to improve performance.
     *
     * @param leaderboard hashmap of leaderboard positions
     * @param minimumWealth minimum wealth to show on leaderboard
     * @param useGroup whether group is enabled
     * @param groupUuidToNameMap map of temporary uuid to name for groups
     */
    public static void setUpLeaderboard(HashMap<UUID, EntityCache> leaderboard, double minimumWealth,
            boolean useGroup, HashMap<UUID, String> groupUuidToNameMap) {
        int positionsPerPage = 10;

        String header = getMessage("leaderboard-header");
        String footer = messageKeysMap.get("leaderboard-footer");
        String messageTemplate = messageKeysMap.get("leaderboard-body");
        StringBuilder message = new StringBuilder(header);
        int position = 1;
        int currentPage = 1;
        for (Map.Entry<UUID, EntityCache> entry : leaderboard.entrySet()) {
            UUID uuid = entry.getKey();
            String name;

            if (useGroup) {
                name = groupUuidToNameMap.get(uuid);
            } else {
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                name = player.getName();
            }
            // handle null player names (can happen if world folder is deleted)
            if (name == null) {
                continue;
            }

            double wealth = entry.getValue().getTotalWealth();
            if (wealth < minimumWealth) {
                continue;
            }
            message.append(messageTemplate);
            message = new StringBuilder(message.toString().replaceAll("%num%",
                    String.valueOf(position))
                    .replaceAll("%entity%", name)
                    .replaceAll("%wealth%", new BigDecimal(wealth).setScale(2,
                            RoundingMode.CEILING).toPlainString()));
            if (position % positionsPerPage == 0) {
                currentPage++;
                message = new StringBuilder(message.append(footer).toString().replaceAll(
                        "%page%", String.valueOf(currentPage)));
                message.append(header);
            }
            position++;
        }

        completeLeaderboard = message.toString();
    }
}

