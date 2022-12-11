package tk.taverncraft.survivaltop.gui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * GuiUtils contains generic functions that helps in creating a GUI.
 */
public class GuiUtils {

    /**
     * Creates an item to show in the GUI.
     *
     * @param material material to use
     * @param name name to show
     * @param isEnchanted whether the item should be enchanted
     * @param lore lore of the item
     *
     * @return item that is to be shown in the GUI
     */
    public static ItemStack createGuiItem(final Material material, final String name,
                                      boolean isEnchanted, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);

        if (isEnchanted) {
            item.addUnsafeEnchantment(Enchantment.LURE, 1);
        }

        final ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        if (isEnchanted) {
            try {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            } catch (Exception ignored) {

            }
        }

        // Set the name of the item
        if (name != null) {
            meta.setDisplayName(parseWithColours(name));
        }

        // Set the lore of the item
        if (lore != null && lore.length != 0) {
            List<String> colouredLore = parseWithColours(lore);
            meta.setLore(colouredLore);
        } else {
            meta.setLore(new ArrayList<>());
        }

        item.setItemMeta(meta);

        return item;
    }

    /**
     * Parse colour for a string.
     *
     * @param text string to parse colour for
     *
     * @return string with parsed colours
     */
    public static String parseWithColours(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Parse colour for strings.
     *
     * @param lore lore to parse colour for
     *
     * @return lore with parsed colours
     */
    private static List<String> parseWithColours(String[] lore) {
        List<String> colouredLore = new ArrayList<>();
        for (String line : lore) {
            colouredLore.add(parseWithColours(line));
        }
        return colouredLore;
    }

    /**
     * Parses quantity placeholder in lore with item quantity.
     *
     * @param lore lore to parse
     * @param placeholder placeholder to replace
     * @param value value to use
     *
     * @return parsed wealth for lore
     */
    public static List<String> parseQuantity(List<String> lore, String placeholder, int value) {
        List<String> parsedQuantity = new ArrayList<>();
        if (lore == null) {
            return parsedQuantity;
        }
        for (String s : lore) {
            parsedQuantity.add(s.replaceAll(placeholder, String.valueOf(value)));
        }
        return parsedQuantity;
    }

    /**
     * Parses wealth placeholder in lore with wealth value.
     *
     * @param lore lore to parse
     * @param placeholder placeholder to replace
     * @param value value to use
     *
     * @return parsed wealth for lore
     */
    public static List<String> parseWealth(List<String> lore, String placeholder, double value) {
        List<String> parsedWealth = new ArrayList<>();
        if (lore == null) {
            return parsedWealth;
        }
        for (String s : lore) {
            parsedWealth.add(s.replaceAll(placeholder, new BigDecimal(value).setScale(2,
                RoundingMode.HALF_UP).toPlainString()));
        }
        return parsedWealth;
    }

    /**
     * Parses a name to replace placeholder with string value.
     *
     * @param lore lore to parse
     * @param placeholder placeholder to replace
     * @param name name to use
     *
     * @return parsed name
     */
    public static String parseName(String lore, String placeholder, String name) {
        return lore.replaceAll(placeholder, name);
    }

    /**
     * Parses page placeholder in lore with int value.
     *
     * @param lore lore to parse
     * @param placeholder placeholder to replace
     * @param page page to use
     *
     * @return parsed page
     */
    public static List<String> parsePage(List<String> lore, String placeholder, int page) {
        List<String> parsedPage = new ArrayList<>();
        if (lore == null) {
            return parsedPage;
        }
        for (String s : lore) {
            parsedPage.add(s.replaceAll(placeholder, String.valueOf(page)));
        }
        return parsedPage;
    }
}
