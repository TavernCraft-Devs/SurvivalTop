package tk.taverncraft.survivaltop.messages;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.ChatPaginator;

import tk.taverncraft.survivaltop.stats.cache.EntityCache;
import tk.taverncraft.survivaltop.utils.types.Pair;

import static org.bukkit.util.ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH;

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
     */
    public static void setUpLeaderboard(HashMap<String, EntityCache> leaderboard, double minimumWealth) {
        int positionsPerPage = 10;

        String header = getMessage("leaderboard-header");
        String footer = messageKeysMap.get("leaderboard-footer");
        String messageTemplate = messageKeysMap.get("leaderboard-body");
        StringBuilder message = new StringBuilder(header);
        int position = 1;
        int currentPage = 1;
        for (EntityCache eCache : leaderboard.values()) {
            String name = eCache.getName();

            // handle null player names (can happen if world folder is deleted)
            if (name == null) {
                continue;
            }

            double wealth = eCache.getTotalWealth();
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

    /**
     * Creates and returns text component for a message.
     *
     * @param key key of message
     *
     * @return text component for message
     */
    public static TextComponent getTextComponentMessage(String key) {
        char[] message = getMessage(key).toCharArray();
        int lastIndex = message.length - 1;
        List<Pair> pairs = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        net.md_5.bungee.api.ChatColor color = net.md_5.bungee.api.ChatColor.WHITE;
        for (int i = 0 ; i <= lastIndex; i++) {
            char c = message[i];
            if (c == '&' && i != lastIndex) {
                net.md_5.bungee.api.ChatColor nextColor = net.md_5.bungee.api.ChatColor.getByChar(message[i + 1]);
                if (color == null) {
                    sb.append(c);
                } else {
                    pairs.add(new Pair(sb.toString(), color));
                    color = nextColor;
                    sb = new StringBuilder();
                }
            } else {
                sb.append(c);
            }
        }
        pairs.add(new Pair(sb.toString(), color));

        ComponentBuilder componentBuilder = new ComponentBuilder("");
        int numPairs = pairs.size();
        for (int i = 0; i < numPairs; i++) {
            Pair pair = pairs.get(i);
            componentBuilder.append(pair.getMessage()).color(pair.getColor());
        }
        BaseComponent[] baseComponent = componentBuilder.create();

        TextComponent textComponent = new TextComponent();
        for (BaseComponent bc : baseComponent) {
            textComponent.addExtra(bc);
        }
        return textComponent;
    }
}

