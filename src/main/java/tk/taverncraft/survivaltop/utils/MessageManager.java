package tk.taverncraft.survivaltop.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.ChatPaginator;

import static org.bukkit.util.ChatPaginator.*;

/**
 * MessageManager handles all formatting and sending of messages to the command sender.
 */
public class MessageManager {
    private static final HashMap<String, String> messageKeysMap = new HashMap<>();

    private static String completeBlockInfo;
    private static String completeSpawnerInfo;
    private static String completeContainerInfo;
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
    public static void sendMessage(CommandSender sender, String messageKey, String[] keys, String[] values) {
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
     * Shows block info to the user.
     *
     * @param sender sender to send message to
     * @param pageNum page number of info
     */
    public static void showBlockInfo(CommandSender sender, int pageNum) {
        int linesPerPage = 12;
        ChatPaginator.ChatPage page = ChatPaginator.paginate(completeBlockInfo, pageNum, GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH, linesPerPage);
        for (String line : page.getLines()) {
            sender.sendMessage(line);
        }
    }

    /**
     * Shows spawner info to the user.
     *
     * @param sender sender to send message to
     * @param pageNum page number of info
     */
    public static void showSpawnerInfo(CommandSender sender, int pageNum) {
        int linesPerPage = 12;
        ChatPaginator.ChatPage page = ChatPaginator.paginate(completeSpawnerInfo, pageNum, GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH, linesPerPage);
        for (String line : page.getLines()) {
            sender.sendMessage(line);
        }
    }

    /**
     * Shows container info to the user.
     *
     * @param sender sender to send message to
     * @param pageNum page number of info
     */
    public static void showContainerInfo(CommandSender sender, int pageNum) {
        int linesPerPage = 12;
        ChatPaginator.ChatPage page = ChatPaginator.paginate(completeContainerInfo, pageNum, GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH, linesPerPage);
        for (String line : page.getLines()) {
            sender.sendMessage(line);
        }
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
        ChatPaginator.ChatPage page = ChatPaginator.paginate(completeLeaderboard, pageNum, GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH, linesPerPage);
        for (String line : page.getLines()) {
            sender.sendMessage(line);
        }
    }

    /**
     * Sets up message for block info beforehand to improve performance.
     *
     * @param blockInfo hashmap of block worth
     */
    public static void setUpBlockInfo(HashMap<String, Double> blockInfo) {
        int positionsPerPage = 10;

        String header = getMessage("blockinfo-header");
        String footer = getMessage("blockinfo-footer");
        String messageTemplate = messageKeysMap.get("blockinfo-body");
        StringBuilder message = new StringBuilder(header);
        int position = 1;
        int currentPage = 1;
        for (HashMap.Entry<String, Double> entry : blockInfo.entrySet()) {
            String block = entry.getKey();
            double value = entry.getValue();
            message.append(messageTemplate);
            message = new StringBuilder(message.toString().replaceAll("%num%", String.valueOf(position))
                    .replaceAll("%block%", block)
                    .replaceAll("%value%", new BigDecimal(value).toPlainString()));
            if (position % positionsPerPage == 0) {
                currentPage++;
                message = new StringBuilder(message.append(footer).toString().replaceAll("%page%", String.valueOf(currentPage)));
                message.append(header);
            }
            position++;
        }

        completeBlockInfo = message.toString();
    }

    /**
     * Sets up message for spawner info beforehand to improve performance.
     *
     * @param spawnerInfo hashmap of spawner worth
     */
    public static void setUpSpawnerInfo(HashMap<String, Double> spawnerInfo) {
        int positionsPerPage = 10;

        String header = getMessage("spawnerinfo-header");
        String footer = getMessage("spawnerinfo-footer");
        String messageTemplate = messageKeysMap.get("spawnerinfo-body");
        StringBuilder message = new StringBuilder(header);
        int position = 1;
        int currentPage = 1;
        for (HashMap.Entry<String, Double> entry : spawnerInfo.entrySet()) {
            String spawner = entry.getKey();
            double value = entry.getValue();
            message.append(messageTemplate);
            message = new StringBuilder(message.toString().replaceAll("%num%", String.valueOf(position))
                    .replaceAll("%spawner%", spawner)
                    .replaceAll("%value%", new BigDecimal(value).toPlainString()));
            if (position % positionsPerPage == 0) {
                currentPage++;
                message = new StringBuilder(message.append(footer).toString().replaceAll("%page%", String.valueOf(currentPage)));
                message.append(header);
            }
            position++;
        }

        completeSpawnerInfo = message.toString();
    }

    /**
     * Sets up message for container info beforehand to improve performance.
     *
     * @param containerInfo hashmap of container worth
     */
    public static void setUpContainerInfo(HashMap<String, Double> containerInfo) {
        int positionsPerPage = 10;

        String header = getMessage("containerinfo-header");
        String footer = getMessage("containerinfo-footer");
        String messageTemplate = messageKeysMap.get("containerinfo-body");
        StringBuilder message = new StringBuilder(header);
        int position = 1;
        int currentPage = 1;
        for (HashMap.Entry<String, Double> entry : containerInfo.entrySet()) {
            String spawner = entry.getKey();
            double value = entry.getValue();
            message.append(messageTemplate);
            message = new StringBuilder(message.toString().replaceAll("%num%", String.valueOf(position))
                    .replaceAll("%item%", spawner)
                    .replaceAll("%value%", new BigDecimal(value).toPlainString()));
            if (position % positionsPerPage == 0) {
                currentPage++;
                message = new StringBuilder(message.append(footer).toString().replaceAll("%page%", String.valueOf(currentPage)));
                message.append(header);
            }
            position++;
        }

        completeContainerInfo = message.toString();
    }

    /**
     * Sets up message for leaderboard beforehand to improve performance.
     *
     * @param leaderboard hashmap of leaderboard positions
     */
    public static void setUpLeaderboard(HashMap<UUID, Double> leaderboard, double minimumWealth,
                                        boolean useGroup, HashMap<UUID, String> groupUuidToNameMap) {
        int positionsPerPage = 10;

        String header = getMessage("leaderboard-header");
        String footer = messageKeysMap.get("leaderboard-footer");
        String messageTemplate = messageKeysMap.get("leaderboard-body");
        StringBuilder message = new StringBuilder(header);
        int position = 1;
        int currentPage = 1;
        for (HashMap.Entry<UUID, Double> entry : leaderboard.entrySet()) {
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

            double wealth = entry.getValue();
            if (wealth < minimumWealth) {
                continue;
            }
            message.append(messageTemplate);
            message = new StringBuilder(message.toString().replaceAll("%num%", String.valueOf(position))
                    .replaceAll("%entity%", name)
                    .replaceAll("%wealth%", new BigDecimal(wealth).setScale(2, RoundingMode.CEILING).toPlainString()));
            if (position % positionsPerPage == 0) {
                currentPage++;
                message = new StringBuilder(message.append(footer).toString().replaceAll("%page%", String.valueOf(currentPage)));
                message.append(header);
            }
            position++;
        }

        completeLeaderboard = message.toString();
    }
}

