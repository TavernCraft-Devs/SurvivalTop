package tk.taverncraft.survivaltop.gui.options;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import tk.taverncraft.survivaltop.gui.types.StatsGui;
import tk.taverncraft.survivaltop.utils.types.MutableInt;

/**
 * StatsMenuOptions loads all configured menu options for stats pages.
 */
public class StatsMenuOptions {
    private final Main main;
    private final int mainPageSize;
    private final int subPageSize;
    private final String mainIdentifier = "§m§s§s§t§o§p";
    private final String blockIdentifier = "§b§s§s§t§o§p";
    private final String spawnerIdentifier = "§s§s§s§t§o§p";
    private final String containerIdentifier = "§c§s§s§t§o§p";
    private final String inventoryIdentifier = "§i§s§s§t§o§p";

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

    public StatsMenuOptions(Main main) {
        this.main = main;
        FileConfiguration config = main.getStatsMenuConfig();
        mainPageSize = config.getInt("main-page-size", 27);
        subPageSize = config.getInt("sub-page-size", 54);
        mainPageTitle = config.getString("main-page-title", "%entity%'s Total Stats") + mainIdentifier;
        subPageBlockTitle = config.getString("sub-page-block-title", "%entity%'s Block Stats") + blockIdentifier;
        subPageSpawnerTitle = config.getString("sub-page-spawner-title", "%entity%'s Spawner Stats") + spawnerIdentifier;
        subPageContainerTitle = config.getString("sub-page-container-title", "%entity%'s Container Stats") + containerIdentifier;
        subPageInventoryTitle = config.getString("sub-page-inventory-title", "%entity%'s Inventory Stats") + inventoryIdentifier;

        setupMainPageBackground(config);
        setupSubPageBackground(config);
        subPageItemName = config.getString("sub-page-items.name");
        subPageItemLore = config.getStringList("sub-page-items.lore");
        subPageItemSlots = config.getIntegerList("sub-page-items.slots");

        for (String key: config.getConfigurationSection("sub-page-buttons").getKeys(false)) {
            setUpSubPageButton(config, key);
        }
        for (String key : config.getConfigurationSection("main-page-buttons").getKeys(false)) {
            setUpMainPageButton(config, key);
        }
    }

    private void setupMainPageBackground(FileConfiguration config) {
        for (String key : config.getConfigurationSection("main-page-background")
            .getKeys(false)) {
            int slot = Integer.parseInt(key);
            Material material = Material.valueOf(config.getString("main-page-background." + key));
            ItemStack itemStack = GuiUtils.createGuiItem(material, "", false, null);
            mainPageBackground.put(slot, itemStack);
        }
    }

    private void setupSubPageBackground(FileConfiguration config) {
        for (String key : config.getConfigurationSection("sub-page-background")
            .getKeys(false)) {
            int slot = Integer.parseInt(key);
            Material material = Material.valueOf(config.getString("sub-page-background." + key));
            ItemStack itemStack = GuiUtils.createGuiItem(material, "", false, null);
            subPageBackground.put(slot, itemStack);
        }
    }

    private void setUpMainPageButton(FileConfiguration config, String button) {
        ConfigurationSection configurationSection = config.getConfigurationSection("main-page-buttons." + button);
        int slot = configurationSection.getInt("slot");
        Material material = Material.valueOf(configurationSection.getString("material"));
        String name = configurationSection.getString("name");
        boolean isEnchanted = configurationSection.getBoolean("enchanted", false);
        List<String> lore = configurationSection.getStringList("lore");

        ItemStack itemStack = GuiUtils.createGuiItem(material, name, isEnchanted, lore.toArray(new String[0]));
        mainPageButtons.put(slot, itemStack);
        mainButtonSlots.put(button, slot);
    }

    private void setUpSubPageButton(FileConfiguration config, String button) {
        ConfigurationSection configurationSection = config.getConfigurationSection("sub-page-buttons." + button);
        int slot = configurationSection.getInt("slot");
        Material material = Material.valueOf(configurationSection.getString("material"));
        String name = configurationSection.getString("name");
        boolean isEnchanted = configurationSection.getBoolean("enchanted", false);
        List<String> lore = configurationSection.getStringList("lore");

        ItemStack itemStack = GuiUtils.createGuiItem(material, name, isEnchanted, lore.toArray(new String[0]));
        subPageButtons.put(slot, itemStack);
        subButtonSlots.put(button, slot);
    }

    public String getMainPageIdentifier() {
        return mainIdentifier;
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

    public int getMainPageSize() {
        return mainPageSize;
    }

    public int getSubPageSize() {
        return subPageSize;
    }

    public String getMainPageTitle() {
        return mainPageTitle;
    }

    public HashMap<Integer, ItemStack> getMainPageBackground() {
        return mainPageBackground;
    }

    public HashMap<Integer, ItemStack> getMainPageButtons() {
        return mainPageButtons;
    }

    public HashMap<Integer, ItemStack> getSubPageBackground() {
        return subPageBackground;
    }

    public HashMap<Integer, ItemStack> getSubPageButtons() {
        return subPageButtons;
    }

    public String getSubPageItemName() {
        return subPageItemName;
    }

    public List<String> getSubPageItemLore() {
        return subPageItemLore;
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

    public int getTotalWealthSlot() {
        return mainButtonSlots.get("total-wealth");
    }

    public int getBlockWealthSlot() {
        return mainButtonSlots.get("block-wealth");
    }

    public int getSpawnerWealthSlot() {
        return mainButtonSlots.get("spawner-wealth");
    }

    public int getContainerWealthSlot() {
        return mainButtonSlots.get("container-wealth");
    }

    public int getInventoryWealthSlot() {
        return mainButtonSlots.get("inventory-wealth");
    }

    /**
     * Prepares the inventory views for block, spawner and container.
     *
     * @param materialList list of materials to show in gui
     * @param entityName name of entity to show stats for
     * @param viewType type of view (block, spawner or container)
     *
     * @return An array list representing pages of inventory for the view type
     */
    private ArrayList<Inventory> prepareStatsViews(HashMap<String, MutableInt> materialList,
            String entityName, String viewType) {
        ArrayList<Inventory> entityViews = new ArrayList<>();
        int pageNum = 1;
        Inventory entityView = initializeStatsSubPageTemplate(entityName, pageNum, viewType);

        // if no entity, return empty inventory
        if (materialList == null ) {
            entityViews.add(entityView);
            return entityViews;
        }

        int counter = 0;
        int endCount = subPageItemSlots.size() - 1;
        int slot = subPageItemSlots.get(counter);
        for (Map.Entry<String, MutableInt> map : materialList.entrySet()) {
            String name = map.getKey();
            int quantity = map.getValue().get();
            if (quantity == 0) {
                continue;
            }
            double worth;
            Material material;
            switch (viewType) {
            case "Block Stats":
                worth = main.getLandManager().getBlockWorth(name);
                material = Material.getMaterial(name);
                break;
            case "Spawner Stats":
                worth = main.getLandManager().getSpawnerWorth(name);
                material = Material.SPAWNER;
                break;
            case "Container Stats":
                worth = main.getLandManager().getContainerWorth(name);
                material = Material.getMaterial(name);
                break;
            default:
                worth = main.getInventoryManager().getInventoryItemWorth(name);
                material = Material.getMaterial(name);
                break;
            }

            List<String> lore = getSubPageItemLore();
            List<String> parsedLore = GuiUtils.parseLore(lore, "%amount%", quantity);
            parsedLore = GuiUtils.parseLore(parsedLore, "%worth%", worth);
            parsedLore = GuiUtils.parseLore(parsedLore, "%value%", worth * quantity);
            String itemName = getSubPageItemName();
            String parsedName = GuiUtils.parseName(itemName, "%name%", name);
            entityView.setItem(slot, GuiUtils.createGuiItem(material, parsedName, false,
                parsedLore.toArray(new String[0])));

            counter++;
            slot = subPageItemSlots.get(counter);

            // next page
            if (counter == endCount) {
                entityViews.add(entityView);
                pageNum++;
                slot = 10;
                entityView = initializeStatsSubPageTemplate(entityName, pageNum, viewType);
            }
        }
        entityViews.add(entityView);

        return entityViews;
    }

    /**
     * Creates template for subpage.
     *
     * @param pageNum page number to show
     * @param viewType type of view (block, spawner or container)
     *
     * @return an inventory gui template for subpage
     */
    public Inventory initializeStatsSubPageTemplate(String entityName, int pageNum, String viewType) {
        Inventory inv;
        String pageNumPrefix = "§" + pageNum + "§8";
        if (viewType.equals("Block Stats")) {
            inv = Bukkit.createInventory(null, getSubPageSize(), pageNumPrefix + GuiUtils.parseName(subPageBlockTitle, "%entity%", entityName));
        } else if (viewType.equals("Spawner Stats")) {
            inv = Bukkit.createInventory(null, getSubPageSize(), pageNumPrefix + GuiUtils.parseName(subPageSpawnerTitle, "%entity%", entityName));
        } else if (viewType.equals("Container Stats")) {
            inv = Bukkit.createInventory(null, getSubPageSize(), pageNumPrefix + GuiUtils.parseName(subPageContainerTitle, "%entity%", entityName));
        } else {
            inv = Bukkit.createInventory(null, getSubPageSize(), pageNumPrefix + GuiUtils.parseName(subPageInventoryTitle, "%entity%", entityName));
        }

        for (Map.Entry<Integer, ItemStack> map : getSubPageBackground().entrySet()) {
            inv.setItem(map.getKey(), map.getValue());
        }

        for (Map.Entry<Integer, ItemStack> map : getSubPageButtons().entrySet()) {
            inv.setItem(map.getKey(), map.getValue());
        }
        return inv;
    }

    public StatsGui createStatsGui(String name, HashMap<String, Double> wealthBreakdown,
            HashMap<String, MutableInt> blockList, HashMap<String, MutableInt> spawnerList,
            HashMap<String, MutableInt> containerList, HashMap<String, MutableInt> inventoryList) {

        // set up inventories
        Inventory mainPage = getStatsMainPage(name, wealthBreakdown);

        ArrayList<Inventory> blockViews = prepareStatsViews(blockList, name, "Block Stats");
        ArrayList<Inventory> spawnerViews = prepareStatsViews(spawnerList, name, "Spawner Stats");
        ArrayList<Inventory> containerViews = prepareStatsViews(containerList, name, "Container Stats");
        ArrayList<Inventory> inventoryViews = prepareStatsViews(inventoryList, name, "Inventory Stats");

        return new StatsGui(name, mainPage, blockViews, spawnerViews, containerViews, inventoryViews);
    }

    /**
     * Sets up the main page for stats.
     *
     * @param entityName name of entity whose stats is shown
     */
    public Inventory getStatsMainPage(String entityName, HashMap<String, Double> wealthBreakdown) {
        String title = getMainPageTitle();
        String parsedName = GuiUtils.parseName(title, "%entity%", entityName);
        Inventory inv = Bukkit.createInventory(null, getMainPageSize(),
            parsedName);
        for (Map.Entry<Integer, ItemStack> map : getMainPageBackground().entrySet()) {
            inv.setItem(map.getKey(), map.getValue());
        }

        for (Map.Entry<String, Integer> map : mainButtonSlots.entrySet()) {
            int slot = map.getValue();
            ItemStack itemStack = mainPageButtons.get(slot);
            ItemMeta meta = itemStack.getItemMeta();
            List<String> parsedLore;
            Double wealth = wealthBreakdown.get(map.getKey());
            if (wealth == null) {
                wealth = 0.0;
            }
            parsedLore = GuiUtils.parseLore(meta.getLore(), "%value%", wealth);
            meta.setLore(parsedLore);
            itemStack.setItemMeta(meta);
            inv.setItem(slot, itemStack);
        }
        return inv;
    }
}
