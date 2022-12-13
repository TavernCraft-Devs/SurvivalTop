package tk.taverncraft.survivaltop.messages;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.ChatPaginator;

import tk.taverncraft.survivaltop.cache.EntityCache;
import tk.taverncraft.survivaltop.utils.types.TextComponentPair;

import static org.bukkit.util.ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH;

/**
 * MessageManager handles all formatting and sending of messages to the command sender.
 */
public class MessageManager {
    private static final HashMap<String, String> messageKeysMap = new HashMap<>();

    // used if leaderboard is not interactive
    private static String completeLeaderboard;

    // used if leaderboard is interactive
    private static BaseComponent[][] completeInteractiveLeaderboard;

    // standard text component message sent when gui stats are ready
    private static TextComponent guiStatsReadyMessage;

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
     * Sends message to the sender.
     *
     * @param sender sender to send message to
     * @param messageKey key to get message with
     * @param keys placeholder keys
     * @param values placeholder values
     */
    public static void sendMessage(CommandSender sender, String messageKey, String[] keys,
            String[] values) {
        String message = getParsedMessage(messageKey, keys, values, true);
        sender.sendMessage(message);
    }

    /**
     * Replaces placeholder keys with values.
     *
     * @param messageKey key to get message with
     * @param keys placeholder keys
     * @param values placeholder values
     * @param includePrefix whether to include plugin prefix in message
     *
     * @return parsed message
     */
    private static String getParsedMessage(String messageKey, String[] keys, String[] values,
            boolean includePrefix) {
        String message = includePrefix ? getMessage(messageKey) : messageKeysMap.get(messageKey);
        for (int i = 0; i < keys.length; i++) {
            message = message.replaceAll(keys[i], values[i]);
        }
        return message;
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

    /**
     * Gets the formatting used for signs and returns the parsed lines.
     *
     * @param keys placeholder keys
     * @param values placeholder values
     *
     * @return formatted text for signs
     */
    public static String getSignFormat(String[] keys, String[] values) {
        String message = messageKeysMap.get("leaderboard-sign");
        for (int i = 0; i < keys.length; i++) {
            message = message.replaceAll(keys[i], values[i]);
        }
        return message;
    }

    /**
     * Shows non-interactive leaderboard to the user.
     *
     * @param sender sender to send message to
     * @param pageNum page number of leaderboard
     */
    public static void showLeaderboard(CommandSender sender, int pageNum, int linesPerPage) {
        if (completeLeaderboard == null) {
            sendMessage(sender, "no-updated-leaderboard");
            return;
        }

        ChatPaginator.ChatPage page = ChatPaginator.paginate(completeLeaderboard, pageNum,
                GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH, linesPerPage + 2);
        for (String line : page.getLines()) {
            sender.sendMessage(line);
        }
    }

    /**
     * Shows interactive leaderboard to the user.
     *
     * @param sender sender to send message to
     * @param pageNum page number of leaderboard
     */
    public static void showInteractiveLeaderboard(CommandSender sender, int pageNum) {
        if (completeInteractiveLeaderboard == null) {
            sendMessage(sender, "no-updated-leaderboard");
            return;
        }

        sender.spigot().sendMessage(completeInteractiveLeaderboard[pageNum - 1]);
    }

    /**
     * Sets up message for non-interactive leaderboard beforehand to improve performance.
     *
     * @param entityCacheList ordered list of entities for leaderboard
     * @param minimumWealth minimum wealth to show on leaderboard
     * @param positionsPerPage number of positions shown per page
     */
    public static void setUpLeaderboard(ArrayList<EntityCache> entityCacheList,
            double minimumWealth, int positionsPerPage) {
        String header = messageKeysMap.get("leaderboard-header");
        String footer = messageKeysMap.get("leaderboard-footer");
        String messageTemplate = messageKeysMap.get("leaderboard-body");
        StringBuilder message = new StringBuilder(header);
        int position = 1;
        int currentPage = 1;
        for (EntityCache eCache : entityCacheList) {
            String name = eCache.getName();

            // handle null player names (can happen if world folder is deleted)
            if (name == null) {
                continue;
            }

            double wealth = eCache.getTotalWealth();
            if (wealth < minimumWealth) {
                continue;
            }

            String entry = messageTemplate.replaceAll("%num%", String.valueOf(position))
                .replaceAll("%entity%", name)
                .replaceAll("%wealth%", new BigDecimal(wealth).setScale(2,
                    RoundingMode.HALF_UP).toPlainString());
            message.append(entry);
            if (position % positionsPerPage == 0) {
                currentPage++;
                message.append(footer.replaceAll("%page%", String.valueOf(currentPage)));
                message.append(header);
            }
            position++;
        }
        completeLeaderboard = message.toString();
    }

    /**
     * Sets up message for interactive leaderboard beforehand to improve performance.
     *
     * @param entityCacheList ordered list of entities for leaderboard
     * @param minimumWealth minimum wealth to show on leaderboard
     * @param positionsPerPage number of positions shown per page
     */
    public static void setUpInteractiveLeaderboard(ArrayList<EntityCache> entityCacheList,
            double minimumWealth, int positionsPerPage) {
        int size = (int) Math.ceil((double) entityCacheList.size() / positionsPerPage);
        completeInteractiveLeaderboard = new BaseComponent[size][];

        String header = messageKeysMap.get("leaderboard-header")
                .substring(0, messageKeysMap.get("leaderboard-header").length() - 1);
        String footer = messageKeysMap.get("leaderboard-footer")
                .substring(0, messageKeysMap.get("leaderboard-footer").length() - 2);
        String messageTemplate = messageKeysMap.get("leaderboard-body")
                .substring(0, messageKeysMap.get("leaderboard-body").length() - 1);
        ComponentBuilder message = new ComponentBuilder(header);
        int position = 1;
        int currentPage = 1;
        for (EntityCache eCache : entityCacheList) {
            String name = eCache.getName();

            // handle null player names (can happen if world folder is deleted)
            if (name == null) {
                continue;
            }

            double wealth = eCache.getTotalWealth();
            if (wealth < minimumWealth) {
                continue;
            }

            String entry = messageTemplate.replaceAll("%num%", String.valueOf(position))
                .replaceAll("%entity%", name)
                .replaceAll("%wealth%", new BigDecimal(wealth).setScale(2,
                    RoundingMode.HALF_UP).toPlainString());
            TextComponent component = getTextComponentMessage(entry);
            eCache.setChat();
            String parsedMessage = getParsedMessage("leaderboard-hover",
                    eCache.getPlaceholders(), eCache.getValues(), false);
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    getBaseComponentArrMessage(parsedMessage)));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/st openstatsgui " + name.toUpperCase()));
            message.append(component);
            if (position % positionsPerPage == 0) {
                message.append(footer.replaceAll("%page%", String.valueOf(currentPage + 1)));
                completeInteractiveLeaderboard[currentPage - 1] = message.create();
                currentPage++;
                message = new ComponentBuilder(header);
            }
            position++;
        }
        completeInteractiveLeaderboard[currentPage - 1] = message.create();
    }

    /**
     * Sends clickable text component to player when gui stats are ready.
     *
     * @param sender player to send message to
     * @param name name of entity whose stats are being shown
     */
    public static void sendGuiStatsReadyMessage(CommandSender sender, String name) {
        if (guiStatsReadyMessage == null) {
            guiStatsReadyMessage = getTextComponentMessage(getMessage("gui-stats-ready"));
        }
        guiStatsReadyMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
            "/st openstatsgui " + name));
        sender.spigot().sendMessage(guiStatsReadyMessage);
    }

    /**
     * Sends message to player when chat stats are ready.
     *
     * @param sender player to send message to
     */
    public static void sendChatStatsReadyMessage(CommandSender sender, EntityCache eCache) {
        String message = getParsedMessage("entity-stats", eCache.getPlaceholders(),
                eCache.getValues(), false);
        sender.sendMessage(message);
    }

    /**
     * Creates and returns base component array for a message.
     *
     * @param message to put in base component array
     *
     * @return base component array for message
     */
    public static BaseComponent[] getBaseComponentArrMessage(String message) {
        char[] chars = message.toCharArray();
        int lastIndex = chars.length - 1;
        List<TextComponentPair> textComponentPairs = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        net.md_5.bungee.api.ChatColor color = net.md_5.bungee.api.ChatColor.WHITE;
        for (int i = 0 ; i <= lastIndex; i++) {
            char c = chars[i];
            if (c == '&' && i != lastIndex) {
                net.md_5.bungee.api.ChatColor nextColor = net.md_5.bungee.api.ChatColor
                        .getByChar(chars[i + 1]);
                if (color == null) {
                    sb.append(c);
                } else {
                    textComponentPairs.add(new TextComponentPair(sb.toString(), color));
                    color = nextColor;
                    sb = new StringBuilder();
                }
            } else {
                sb.append(c);
            }
        }
        textComponentPairs.add(new TextComponentPair(sb.toString(), color));

        ComponentBuilder componentBuilder = new ComponentBuilder("");
        int numPairs = textComponentPairs.size();
        for (int i = 0; i < numPairs; i++) {
            TextComponentPair textComponentPair = textComponentPairs.get(i);
            componentBuilder.append(textComponentPair.getMessage()).color(
                    textComponentPair.getColor());
        }
        return componentBuilder.create();
    }

    /**
     * Creates and returns text component for a message.
     *
     * @param message to put in text component
     *
     * @return text component for message
     */
    public static TextComponent getTextComponentMessage(String message) {
        BaseComponent[] baseComponent = getBaseComponentArrMessage(message);

        TextComponent textComponent = new TextComponent();
        for (BaseComponent bc : baseComponent) {
            textComponent.addExtra(bc);
        }
        return textComponent;
    }
}

