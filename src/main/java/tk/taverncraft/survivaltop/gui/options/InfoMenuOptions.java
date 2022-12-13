package tk.taverncraft.survivaltop.gui.options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.gui.GuiUtils;
import tk.taverncraft.survivaltop.gui.types.InfoGui;

/**
 * InfoMenuOptions loads all configured menu options for info pages.
 */
public class InfoMenuOptions {
    private final Main main;
    private final int mainPageSize;
    private final int subPageSize;

    // identifiers
    private final String mainIdentifier = "§m§i§s§t§o§p";
    private final String infoPageIdentifier = "§i§s§t§o§p";
    private final String blockIdentifier = "§b§i§s§t§o§p";
    private final String spawnerIdentifier = "§s§i§s§t§o§p";
    private final String containerIdentifier = "§c§i§s§t§o§p";
    private final String inventoryIdentifier = "§i§i§s§t§o§p";

    // menu titles
    private final String mainPageTitle;
    private final String subPageBlockTitle;
    private final String subPageSpawnerTitle;
    private final String subPageContainerTitle;
    private final String subPageInventoryTitle;

    // button slots for use in inventory click events
    private final HashMap<String, Integer> mainButtonSlots = new HashMap<>();
    private final HashMap<String, Integer> subButtonSlots = new HashMap<>();

    // button items
    private final HashMap<Integer, ItemStack> mainPageButtons = new HashMap<>();
    private final HashMap<Integer, ItemStack> subPageButtons = new HashMap<>();

    // backgrounds
    private final HashMap<Integer, ItemStack> mainPageBackground = new HashMap<>();
    private final HashMap<Integer, ItemStack> subPageBackground = new HashMap<>();

    // sub page items
    private final String subPageItemName;
    private final List<String> subPageItemLore;
    private final List<Integer> subPageItemSlots;

    /**
     * Constructor for InfoMenuOptions.
     *
     * @param main plugin class
     */
    public InfoMenuOptions(Main main) {
        this.main = main;
        FileConfiguration config = main.getConfigManager().getInfoMenuConfig();
        mainPageSize = (int) Math.round(config.getInt("main-page-size", 27) / 9.0) * 9;
        subPageSize = (int) Math.round(config.getInt("sub-page-size", 54) / 9.0) * 9;
        mainPageTitle = "§8" + config.getString("main-page-title", "Item Values Info") +
                mainIdentifier;
        subPageBlockTitle = config.getString("sub-page-block-title", "Block Info") +
                blockIdentifier ;
        subPageSpawnerTitle = config.getString("sub-page-spawner-title", "Spawner Info") +
                spawnerIdentifier;
        subPageContainerTitle = config.getString("sub-page-container-title",
                "Container Info") + containerIdentifier;
        subPageInventoryTitle = config.getString("sub-page-inventory-title",
                "Inventory Info") + inventoryIdentifier;

        setUpMainPageBackground(config);
        setUpSubPageBackground(config);
        subPageItemName = config.getString("sub-page-items.name");
        subPageItemLore = config.getStringList("sub-page-items.lore");
        subPageItemSlots = config.getIntegerList("sub-page-items.slots");

        for (String key: config.getConfigurationSection("sub-page-buttons")
                .getKeys(false)) {
            setUpSubPageButton(config, key);
        }
        for (String key : config.getConfigurationSection("main-page-buttons")
                .getKeys(false)) {
            setUpMainPageButton(config, key);
        }
    }

    /**
     * Sets up the background for the main page.
     *
     * @param config config file to refer to for setting up background
     */
    private void setUpMainPageBackground(FileConfiguration config) {
        for (String key : config.getConfigurationSection("main-page-background")
                .getKeys(false)) {
            int slot = Integer.parseInt(key);
            Material material = Material.valueOf(config.getString("main-page-background." +
                    key));
            ItemStack itemStack = GuiUtils.createGuiItem(material, "", false,
                    null);
            mainPageBackground.put(slot, itemStack);
        }
    }

    /**
     * Sets up the background for the subpage.
     *
     * @param config config file to refer to for setting up background
     */
    private void setUpSubPageBackground(FileConfiguration config) {
        for (String key : config.getConfigurationSection("sub-page-background")
                .getKeys(false)) {
            int slot = Integer.parseInt(key);
            Material material = Material.valueOf(config.getString("sub-page-background." +
                    key));
            ItemStack itemStack = GuiUtils.createGuiItem(material, "", false,
                    null);
            subPageBackground.put(slot, itemStack);
        }
    }

    /**
     * Sets up the buttons for the main page.
     *
     * @param config config file to refer to for setting up buttons
     * @param button button to set up
     */
    private void setUpMainPageButton(FileConfiguration config, String button) {
        ConfigurationSection configurationSection = config.getConfigurationSection(
                "main-page-buttons." + button);
        int slot = configurationSection.getInt("slot");
        Material material = Material.valueOf(configurationSection.getString("material"));
        String name = configurationSection.getString("name");
        boolean isEnchanted = configurationSection.getBoolean("enchanted", false);
        List<String> lore = configurationSection.getStringList("lore");

        ItemStack itemStack = GuiUtils.createGuiItem(material, name, isEnchanted,
                lore.toArray(new String[0]));
        mainPageButtons.put(slot, itemStack);
        mainButtonSlots.put(button, slot);
    }

    /**
     * Sets up the buttons for the subpage.
     *
     * @param config config file to refer to for setting up buttons
     * @param button button to set up
     */
    private void setUpSubPageButton(FileConfiguration config, String button) {
        ConfigurationSection configurationSection = config.getConfigurationSection(
                "sub-page-buttons." + button);
        int slot = configurationSection.getInt("slot");
        Material material = Material.valueOf(configurationSection.getString("material"));
        String name = configurationSection.getString("name");
        boolean isEnchanted = configurationSection.getBoolean("enchanted", false);
        List<String> lore = configurationSection.getStringList("lore");

        ItemStack itemStack = GuiUtils.createGuiItem(material, name, isEnchanted,
                lore.toArray(new String[0]));
        subPageButtons.put(slot, itemStack);
        subButtonSlots.put(button, slot);
    }

    /**
     * Creates the gui for item info.
     *
     * @return gui for item info
     */
    public InfoGui createInfoGui() {
        LinkedHashMap<String, Double> blockList = main.getLandManager().getBlockWorth();
        LinkedHashMap<String, Double> spawnerList = main.getLandManager().getSpawnerWorth();
        LinkedHashMap<String, Double> containerList = main.getLandManager().getContainerWorth();
        LinkedHashMap<String, Double> inventoryList = main.getInventoryManager()
                .getInventoryItemWorth();
        Inventory mainPage = prepareMainPage();
        ArrayList<Inventory> blockViews = prepareSubPage(blockList, "Block Info");
        ArrayList<Inventory> spawnerViews = prepareSubPage(spawnerList, "Spawner Info");
        ArrayList<Inventory> containerViews = prepareSubPage(containerList,
                "Container Info");
        ArrayList<Inventory> inventoryViews = prepareSubPage(inventoryList,
                "Inventory Info");
        return new InfoGui(mainPage, blockViews, spawnerViews, containerViews, inventoryViews);
    }

    /**
     * Sets up the main page for item info.
     *
     * @return an inventory gui for main page
     */
    public Inventory prepareMainPage() {
        Inventory inv = Bukkit.createInventory(null, mainPageSize,
                GuiUtils.parseWithColours(mainPageTitle));
        for (Map.Entry<Integer, ItemStack> map : mainPageBackground.entrySet()) {
            inv.setItem(map.getKey(), map.getValue());
        }

        for (Map.Entry<Integer, ItemStack> map : mainPageButtons.entrySet()) {
            inv.setItem(map.getKey(), map.getValue());
        }

        return inv;
    }

    /**
     * Prepares the inventory views for block, spawner and container.
     *
     * @param materialList list of materials to show in gui
     * @param viewType type of view (block, spawner or container)
     *
     * @return An array list representing pages of inventory for the view type
     */
    private ArrayList<Inventory> prepareSubPage(LinkedHashMap<String, Double> materialList,
            String viewType) {
        ArrayList<Inventory> entityViews = new ArrayList<>();
        int pageNum = 1;
        Inventory entityView = getSubPageTemplate(pageNum, viewType);

        // if no entity, return empty inventory
        if (materialList == null ) {
            entityViews.add(entityView);
            return entityViews;
        }

        int counter = 0;
        int endCount = subPageItemSlots.size() - 1;
        int slot = subPageItemSlots.get(counter);
        for (Map.Entry<String, Double> map : materialList.entrySet()) {
            String name = map.getKey();
            double worth = map.getValue();
            Material material;
            if (viewType.equals("Spawner Info")) {
                material = Material.SPAWNER;
            } else {
                material = Material.getMaterial(name);
            }

            List<String> parsedLore = GuiUtils.parseWealth(subPageItemLore, "%worth%",
                    worth);
            String parsedName = GuiUtils.parseName(subPageItemName, "%name%", name);
            entityView.setItem(slot, GuiUtils.createGuiItem(material, parsedName, false,
                parsedLore.toArray(new String[0])));

            counter++;
            slot = subPageItemSlots.get(counter);

            // next page
            if (counter == endCount) {
                entityViews.add(entityView);
                pageNum++;
                slot = 10;
                entityView = getSubPageTemplate(pageNum, viewType);
            }
        }
        entityViews.add(entityView);

        return entityViews;
    }

    /**
     * Creates template for subpage.
     *
     * @param pageNum page number to show
     * @param viewType type of view (block, spawner, container or inventory)
     *
     * @return an inventory gui template for subpage
     */
    public Inventory getSubPageTemplate(int pageNum, String viewType) {
        Inventory inv;
        String pageNumPrefix = "§" + pageNum + "§8";
        switch (viewType) {
        case "Block Info":
            inv = Bukkit.createInventory(null, subPageSize, pageNumPrefix +
                GuiUtils.parseWithColours(subPageBlockTitle));
            break;
        case "Spawner Info":
            inv = Bukkit.createInventory(null, subPageSize, pageNumPrefix +
                GuiUtils.parseWithColours(subPageSpawnerTitle));
            break;
        case "Container Info":
            inv = Bukkit.createInventory(null, subPageSize, pageNumPrefix +
                GuiUtils.parseWithColours(subPageContainerTitle));
            break;
        default:
            inv = Bukkit.createInventory(null, subPageSize, pageNumPrefix +
                GuiUtils.parseWithColours(subPageInventoryTitle));
            break;
        }

        for (Map.Entry<Integer, ItemStack> map : subPageBackground.entrySet()) {
            inv.setItem(map.getKey(), map.getValue());
        }

        for (Map.Entry<Integer, ItemStack> map : subPageButtons.entrySet()) {
            int slot = map.getKey();
            if (pageNum == 1 && slot == getPrevPageSlot()) {
                continue;
            }
            if (slot == getNextPageSlot() || slot == getPrevPageSlot()) {
                int pageToUse = slot == getNextPageSlot() ? pageNum + 1 : pageNum - 1;
                ItemStack itemStack = map.getValue();
                ItemMeta meta = itemStack.getItemMeta();
                List<String> parsedLore = GuiUtils.parsePage(meta.getLore(), "%page%",
                    pageToUse);
                meta.setLore(parsedLore);
                itemStack.setItemMeta(meta);
                inv.setItem(slot, itemStack);
            } else {
                inv.setItem(slot, map.getValue());
            }
        }
        return inv;
    }

    // getters below are for information used in handling of inventory click events

    public String getMainPageIdentifier() {
        return mainIdentifier;
    }

    public String getInfoPageIdentifier() {
        return infoPageIdentifier;
    }

    public String getSubPageBlockIdentifier() {
        return blockIdentifier;
    }

    public String getSubPageSpawnerIdentifier() {
        return spawnerIdentifier;
    }

    public String getSubPageContainerIdentifier() {
        return containerIdentifier;
    }

    public String getSubPageInventoryIdentifier() {
        return inventoryIdentifier;
    }

    public int getMainMenuSlot() {
        return subButtonSlots.get("main-menu");
    }

    public int getNextPageSlot() {
        return subButtonSlots.get("next-page");
    }

    public int getPrevPageSlot() {
        return subButtonSlots.get("previous-page");
    }

    public int getBlockInfoSlot() {
        return mainButtonSlots.get("block-info");
    }

    public int getSpawnerInfoSlot() {
        return mainButtonSlots.get("spawner-info");
    }

    public int getContainerInfoSlot() {
        return mainButtonSlots.get("container-info");
    }

    public int getInventoryInfoSlot() {
        return mainButtonSlots.get("inventory-info");
    }
}
