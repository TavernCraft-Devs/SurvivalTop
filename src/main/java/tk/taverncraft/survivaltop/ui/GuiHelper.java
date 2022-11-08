package tk.taverncraft.survivaltop.ui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUIHelper is an abstract class containing shared default initializations for all GUI pages.
 */
abstract class GuiHelper {
    private final int subPageSize = 54;
    protected String identifier = "§s§u§r§v§t§o§p";
    protected final Material background = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
    private final Material itemGround = Material.YELLOW_STAINED_GLASS_PANE;

    /**
     * Creates template for subpage.
     *
     * @param entityName name of entity whose stats are shown
     * @param pageNum page number to show
     * @param viewType type of view (block, spawner or container)
     *
     * @return an inventory gui template for subpage
     */
    public Inventory initializeSubPageTemplate(String entityName, int pageNum, String viewType) {
        Inventory inv = Bukkit.createInventory(null, this.subPageSize,
                entityName + viewType + identifier);
        for (int i = 0; i < subPageSize; i++) {
            inv.setItem(i, createGuiItem(background, "", false));
        }

        for (int i = 10; i < 35; i++) {
            if (i == 17 || i == 26) {
                i++;
                continue;
            }
            inv.setItem(i, createGuiItem(itemGround, "", false));
        }

        if (pageNum != 1) {
            inv.setItem(47, createGuiItem(Material.ARROW, "Previous Page",
                false, "Go to page " + (pageNum - 1)));
        }
        inv.setItem(49, createGuiItem(Material.COMPASS, "Main Page",
                false, "Return to Main Page"));
        inv.setItem(51, createGuiItem(Material.ARROW, "Next Page",
                false, "Go to page " + (pageNum + 1)));
        return inv;
    }

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
    protected ItemStack createGuiItem(Material material, String name,
            boolean isEnchanted, String... lore) {
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

        meta.setDisplayName(ChatColor.AQUA + name);

        // if no lore
        if (lore.length == 0) {
            item.setItemMeta(meta);
            return item;
        }

        // if there is lore
        List<String> parsedLore = new ArrayList<>();
        for (String str : lore) {
            String[] tempArr = str.split(":", 2);
            if (tempArr.length == 2) {
                parsedLore.add(ChatColor.YELLOW + tempArr[0] + ":" + ChatColor.GREEN + tempArr[1]);
            } else {
                parsedLore.add(ChatColor.YELLOW + str);
            }
        }
        meta.setLore(parsedLore);
        item.setItemMeta(meta);
        return item;
    }
}
