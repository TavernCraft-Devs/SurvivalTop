package tk.taverncraft.survivaltop.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

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
            } catch (Exception e) {

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

    private static String parseWithColours(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private static List<String> parseWithColours(String[] lore) {
        List<String> colouredLore = new ArrayList<>();
        for (String line : lore) {
            colouredLore.add(parseWithColours(line));
        }
        return colouredLore;
    }

    public static List<String> parseLore(List<String> lore, String placeholder, double value) {
        List<String> parsedLore = new ArrayList<>();
        if (lore == null) {
            return parsedLore;
        }
        for (String s : lore) {
            parsedLore.add(s.replaceAll(placeholder, String.valueOf(value)));
        }
        return parsedLore;
    }

    public static String parseName(String lore, String placeholder, String name) {
        return lore.replaceAll(placeholder, name);
    }
}
