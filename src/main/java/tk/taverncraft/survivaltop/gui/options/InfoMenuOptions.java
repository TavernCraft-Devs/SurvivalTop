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
    private final String mainIdentifier = "§m§i§s§t§o§p";
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

    public InfoMenuOptions(Main main) {
        this.main = main;
        FileConfiguration config = main.getInfoMenuConfig();
        mainPageSize = config.getInt("main-page-size", 27);
        subPageSize = config.getInt("sub-page-size", 54);
        mainPageTitle = config.getString("main-page-title", "Item Values Info") + mainIdentifier;
        subPageBlockTitle = config.getString("sub-page-block-title", "Block Info") + blockIdentifier ;
        subPageSpawnerTitle = config.getString("sub-page-spawner-title", "Spawner Info") + spawnerIdentifier;
        subPageContainerTitle = config.getString("sub-page-container-title", "Container Info") + containerIdentifier;
        subPageInventoryTitle = config.getString("sub-page-inventory-title", "Inventory Info") + inventoryIdentifier;

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

    public String getSubPageBlockTitle() {
        return subPageBlockTitle;
    }

    public String getSubPageSpawnerTitle() {
        return subPageSpawnerTitle;
    }

    public String getSubPageContainerTitle() {
        return subPageContainerTitle;
    }

    public String getSubPageInventoryTitle() {
        return subPageInventoryTitle;
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

    /**
     * Prepares the inventory views for block, spawner and container.
     *
     * @param materialList list of materials to show in gui
     * @param viewType type of view (block, spawner or container)
     *
     * @return An array list representing pages of inventory for the view type
     */
    private ArrayList<Inventory> prepareInfoViews(LinkedHashMap<String, Double> materialList,
            String viewType) {
        ArrayList<Inventory> entityViews = new ArrayList<>();
        int pageNum = 1;
        Inventory entityView = initializeInfoSubPageTemplate(pageNum, viewType);

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

            List<String> lore = subPageItemLore;
            List<String> parsedLore = GuiUtils.parseLore(lore, "%worth%", worth);
            String itemName = subPageItemName;
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
                entityView = initializeInfoSubPageTemplate(pageNum, viewType);
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
    public Inventory initializeInfoSubPageTemplate(int pageNum, String viewType) {
        Inventory inv;
        String pageNumPrefix = "§" + pageNum + "§8";
        if (viewType.equals("Block Info")) {
            inv = Bukkit.createInventory(null, getSubPageSize(), pageNumPrefix + getSubPageBlockTitle());
        } else if (viewType.equals("Spawner Info")) {
            inv = Bukkit.createInventory(null, getSubPageSize(), pageNumPrefix + getSubPageSpawnerTitle());
        } else if (viewType.equals("Container Info")) {
            inv = Bukkit.createInventory(null, getSubPageSize(), pageNumPrefix + getSubPageContainerTitle());
        } else {
            inv = Bukkit.createInventory(null, getSubPageSize(), pageNumPrefix + getSubPageInventoryTitle());
        }

        for (Map.Entry<Integer, ItemStack> map : getSubPageBackground().entrySet()) {
            inv.setItem(map.getKey(), map.getValue());
        }

        for (Map.Entry<Integer, ItemStack> map : getSubPageButtons().entrySet()) {
            inv.setItem(map.getKey(), map.getValue());
        }
        return inv;
    }

    public InfoGui createInfoGui() {
        LinkedHashMap<String, Double> blockList = main.getLandManager().getBlockWorth();
        LinkedHashMap<String, Double> spawnerList = main.getLandManager().getSpawnerWorth();
        LinkedHashMap<String, Double> containerList = main.getLandManager().getContainerWorth();
        LinkedHashMap<String, Double> inventoryList = main.getInventoryManager().getInventoryItemWorth();
        Inventory mainPage = getInfoMainPage();
        ArrayList<Inventory> blockViews = prepareInfoViews(blockList, "Block Info");
        ArrayList<Inventory> spawnerViews = prepareInfoViews(spawnerList, "Spawner Info");
        ArrayList<Inventory> containerViews = prepareInfoViews(containerList, "Container Info");
        ArrayList<Inventory> inventoryViews = prepareInfoViews(inventoryList, "Inventory Info");
        return new InfoGui(mainPage, blockViews, spawnerViews, containerViews, inventoryViews);
    }

    /**
     * Sets up the main page for item info.
     */
    public Inventory getInfoMainPage() {
        Inventory inv = Bukkit.createInventory(null, getMainPageSize(), getMainPageTitle());
        for (Map.Entry<Integer, ItemStack> map : getMainPageBackground().entrySet()) {
            inv.setItem(map.getKey(), map.getValue());
        }

        for (Map.Entry<Integer, ItemStack> map : getMainPageButtons().entrySet()) {
            inv.setItem(map.getKey(), map.getValue());
        }

        return inv;
    }
}
