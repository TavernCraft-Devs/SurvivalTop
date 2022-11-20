package tk.taverncraft.survivaltop.gui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.gui.types.InfoGui;
import tk.taverncraft.survivaltop.gui.types.StatsGui;
import tk.taverncraft.survivaltop.logs.LogManager;
import tk.taverncraft.survivaltop.stats.cache.EntityCache;
import tk.taverncraft.survivaltop.utils.types.MutableInt;

/**
 * GuiManager handles all logic related to showing information in a GUI.
 */
public class GuiManager {
    private final Main main;
    private StatsMenuOptions statsOptions;
    private InfoMenuOptions infoOptions;

    private InfoGui infoGui;

    private ConcurrentHashMap<UUID, StatsGui> senderGui = new ConcurrentHashMap<>();

    /**
     * Constructor for GuiManager.
     *
     * @param main plugin class
     */
    public GuiManager(Main main) {
        this.main = main;
        initializeMenuOptions();
    }

    public void initializeMenuOptions() {
        this.statsOptions = new StatsMenuOptions(main, this);
        this.infoOptions = new InfoMenuOptions(main, this);
        createInfoGui();
    }

    public void createInfoGui() {
        LinkedHashMap<String, Double> blockList = main.getLandManager().getBlockWorth();
        LinkedHashMap<String, Double> spawnerList = main.getLandManager().getSpawnerWorth();
        LinkedHashMap<String, Double> containerList = main.getLandManager().getContainerWorth();
        LinkedHashMap<String, Double> inventoryList = main.getInventoryManager().getInventoryItemWorth();
        Inventory mainPage = getInfoMainPage();
        ArrayList<Inventory> blockViews = prepareInfoViews(blockList, "Block Info");
        ArrayList<Inventory> spawnerViews = prepareInfoViews(spawnerList, "Spawner Info");
        ArrayList<Inventory> containerViews = prepareInfoViews(containerList, "Container Info");
        ArrayList<Inventory> inventoryViews = prepareInfoViews(inventoryList, "Inventory Info");
        this.infoGui = new InfoGui(mainPage, blockViews, spawnerViews, containerViews, inventoryViews);
    }

    public StatsGui createStatsGui(String name, double balWealth, double landWealth, double blockWealth,
            double spawnerWealth, double containerWealth, double inventoryWealth, double totalWealth,
            HashMap<String, MutableInt> blockList, HashMap<String, MutableInt> spawnerList,
            HashMap<String, MutableInt> containerList, HashMap<String, MutableInt> inventoryList) {

        // set up inventories
        name = name + " ";
        Inventory mainPage = getStatsMainPage(name, balWealth, landWealth, blockWealth, spawnerWealth,
                containerWealth, inventoryWealth, totalWealth);

        ArrayList<Inventory> blockViews = prepareStatsViews(blockList, name, "Block Stats");
        ArrayList<Inventory> spawnerViews = prepareStatsViews(spawnerList, name, "Spawner Stats");
        ArrayList<Inventory> containerViews = prepareStatsViews(containerList, name, "Container Stats");
        ArrayList<Inventory> inventoryViews = prepareStatsViews(inventoryList, name, "Inventory Stats");

        return new StatsGui(mainPage, blockViews, spawnerViews, containerViews, inventoryViews);
    }

    /**
     * Sets up the main page for stats.
     *
     * @param entityName name of entity whose stats is shown
     */
    public Inventory getStatsMainPage(String entityName, double balWealth, double landWealth, double blockWealth,
            double spawnerWealth, double containerWealth, double inventoryWealth, double totalWealth) {
        String title = statsOptions.getMainPageTitle();
        String parsedName = parseName(title, "%entity%", entityName);
        Inventory inv = Bukkit.createInventory(null, statsOptions.getMainPageSize(),
                parsedName);
        for (Map.Entry<Integer, ItemStack> map : statsOptions.getMainPageBackground().entrySet()) {
            inv.setItem(map.getKey(), map.getValue());
        }

        // formatting to 2dp
        balWealth = new BigDecimal(balWealth).setScale(2,
                RoundingMode.CEILING).doubleValue();
        totalWealth = new BigDecimal(totalWealth).setScale(2,
                RoundingMode.CEILING).doubleValue();

        for (Map.Entry<Integer, ItemStack> map : statsOptions.getMainPageButtons().entrySet()) {
            int slot = map.getKey();
            ItemStack itemStack = map.getValue();
            ItemMeta meta = itemStack.getItemMeta();
            List<String> parsedLore;
            if (slot == statsOptions.getTotalWealthSlot()) {
                parsedLore = parseLore(meta.getLore(), "%value%", totalWealth);
            } else if (slot == statsOptions.getBalWealthSlot() && main.getOptions().balIsIncluded()) {
                parsedLore = parseLore(meta.getLore(), "%value%", balWealth);
            } else if (slot == statsOptions.getLandWealthSlot() && main.getOptions().landIsIncluded()) {
                parsedLore = parseLore(meta.getLore(), "%value%", landWealth);
            } else if (slot == statsOptions.getBlockWealthSlot() && main.getOptions().landIsIncluded()) {
                parsedLore = parseLore(meta.getLore(), "%value%", blockWealth);
            } else if (slot == statsOptions.getSpawnerWealthSlot() && main.getOptions().spawnerIsIncluded()) {
                parsedLore = parseLore(meta.getLore(), "%value%", spawnerWealth);
            } else if (slot == statsOptions.getContainerWealthSlot() && main.getOptions().containerIsIncluded()) {
                parsedLore = parseLore(meta.getLore(), "%value%", containerWealth);
            } else if (slot == statsOptions.getInventoryWealthSlot() && main.getOptions().inventoryIsIncluded()) {
                parsedLore = parseLore(meta.getLore(), "%value%", inventoryWealth);
            } else {
                parsedLore = Arrays.asList(statsOptions.getDisabledButtonLore());
            }
            meta.setLore(parsedLore);
            itemStack.setItemMeta(meta);
            inv.setItem(slot, itemStack);
        }
        return inv;
    }

    private List<String> parseLore(List<String> lore, String placeholder, double value) {
        List<String> parsedLore = new ArrayList<>();
        if (lore == null) {
            return parsedLore;
        }
        for (String s : lore) {
            parsedLore.add(s.replaceAll(placeholder, String.valueOf(value)));
        }
        return parsedLore;
    }

    private String parseName(String lore, String placeholder, String name) {
        return lore.replaceAll(placeholder, name);
    }

    /**
     * Prepares the inventory views for block, spawner and container.
     *
     * @param materialList list of materials to show in gui
     * @param entityName name of entity whose stats is shown
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

        int slot = 10;
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

            List<String> lore = statsOptions.getSubPageItemLore();
            List<String> parsedLore = parseLore(lore, "%amount%", quantity);
            parsedLore = parseLore(parsedLore, "%worth%", worth);
            parsedLore = parseLore(parsedLore, "%value%", worth * quantity);
            String itemName = statsOptions.getSubPageItemName();
            String parsedName = parseName(itemName, "%name%", name);
            entityView.setItem(slot, createGuiItem(material, parsedName, false,
                    parsedLore.toArray(new String[0])));

            slot++;

            // next line
            if (slot == 17 || slot == 26) {
                slot += 2;
            }

            // next page
            if (slot == 35) {
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
     * @param entityName name of entity whose stats are shown
     * @param pageNum page number to show
     * @param viewType type of view (block, spawner or container)
     *
     * @return an inventory gui template for subpage
     */
    public Inventory initializeStatsSubPageTemplate(String entityName, int pageNum, String viewType) {
        Inventory inv;
        if (viewType.equals("Block Stats")) {
            inv = Bukkit.createInventory(null, statsOptions.getSubPageSize(),
                statsOptions.getSubPageBlockTitle());
        } else if (viewType.equals("Spawner Stats")) {
            inv = Bukkit.createInventory(null, statsOptions.getSubPageSize(),
                statsOptions.getSubPageSpawnerTitle());
        } else if (viewType.equals("Container Stats")) {
            inv = Bukkit.createInventory(null, statsOptions.getSubPageSize(),
                statsOptions.getSubPageContainerTitle());
        } else {
            inv = Bukkit.createInventory(null, statsOptions.getSubPageSize(),
                statsOptions.getSubPageInventoryTitle());
        }

        for (Map.Entry<Integer, ItemStack> map : statsOptions.getSubPageBackground().entrySet()) {
            inv.setItem(map.getKey(), map.getValue());
        }

        for (Map.Entry<Integer, ItemStack> map : statsOptions.getSubPageButtons().entrySet()) {
            inv.setItem(map.getKey(), map.getValue());
        }
        return inv;
    }

    /**
     * Creates template for subpage.
     *
     * @param entityName name of entity whose stats are shown
     * @param pageNum page number to show
     * @param viewType type of view (block, spawner or container)
     *
     * @return an inventory gui template for subpage
     */
    public Inventory initializeInfoSubPageTemplate(String entityName, int pageNum, String viewType) {
        Inventory inv;
        if (viewType.equals("Block Stats")) {
            inv = Bukkit.createInventory(null, infoOptions.getSubPageSize(),
                infoOptions.getSubPageBlockTitle());
        } else if (viewType.equals("Spawner Stats")) {
            inv = Bukkit.createInventory(null, infoOptions.getSubPageSize(),
                infoOptions.getSubPageSpawnerTitle());
        } else if (viewType.equals("Container Stats")) {
            inv = Bukkit.createInventory(null, infoOptions.getSubPageSize(),
                infoOptions.getSubPageContainerTitle());
        } else {
            inv = Bukkit.createInventory(null, infoOptions.getSubPageSize(),
                infoOptions.getSubPageInventoryTitle());
        }

        for (Map.Entry<Integer, ItemStack> map : infoOptions.getSubPageBackground().entrySet()) {
            inv.setItem(map.getKey(), map.getValue());
        }

        for (Map.Entry<Integer, ItemStack> map : infoOptions.getSubPageButtons().entrySet()) {
            inv.setItem(map.getKey(), map.getValue());
        }
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
    protected ItemStack createGuiItem(final Material material, final String name,
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

    private String parseWithColours(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private List<String> parseWithColours(String[] lore) {
        List<String> colouredLore = new ArrayList<>();
        for (String line : lore) {
            colouredLore.add(parseWithColours(line));
        }
        return colouredLore;
    }

    /**
     * Special handler to open player inventory gui stats main page.
     *
     * @param uuid uuid of sender
     */
    public void openMainStatsPage(UUID uuid) {
        try {
            Bukkit.getPlayer(uuid).openInventory(senderGui.get(uuid).getMainStatsPage());
        } catch (Exception e) {
            LogManager.error(e.getMessage());
        }
    }

    /**
     * Retrieves player inventory gui stats block page.
     *
     * @param uuid uuid of sender
     * @param pageNum page number to show
     *
     * @return inventory page containing block info for given page
     */
    public Inventory getBlockStatsPage(UUID uuid, int pageNum) {
        return senderGui.get(uuid).getBlockStatsPage(pageNum);
    }

    /**
     * Retrieves player inventory gui stats spawner page.
     *
     * @param uuid uuid of sender
     * @param pageNum page number to show
     *
     * @return inventory page containing spawner info for given page
     */
    public Inventory getSpawnerStatsPage(UUID uuid, int pageNum) {
        return senderGui.get(uuid).getSpawnerStatsPage(pageNum);
    }

    /**
     * Retrieves player inventory gui stats container page.
     *
     * @param uuid uuid of sender
     * @param pageNum page number to show
     *
     * @return inventory page containing container info for given page
     */
    public Inventory getContainerStatsPage(UUID uuid, int pageNum) {
        return senderGui.get(uuid).getContainerStatsPage(pageNum);
    }

    /**
     * Retrieves player inventory gui stats inventory page.
     *
     * @param uuid uuid of sender
     * @param pageNum page number to show
     *
     * @return inventory page containing inventory info for given page
     */
    public Inventory getInventoryStatsPage(UUID uuid, int pageNum) {
        return senderGui.get(uuid).getInventoryStatsPage(pageNum);
    }

    public void setSenderGui(UUID uuid, EntityCache eCache) {
        senderGui.put(uuid, eCache.getGui(main));
    }

    /**
     * Sets up the main page for item info.
     */
    public Inventory getInfoMainPage() {
        Inventory inv = Bukkit.createInventory(null, infoOptions.getMainPageSize(),
            infoOptions.getMainPageTitle());
        for (Map.Entry<Integer, ItemStack> map : infoOptions.getMainPageBackground().entrySet()) {
            inv.setItem(map.getKey(), map.getValue());
        }

        for (Map.Entry<Integer, ItemStack> map : infoOptions.getMainPageButtons().entrySet()) {
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
    private ArrayList<Inventory> prepareInfoViews(LinkedHashMap<String, Double> materialList,
                                                  String viewType) {
        ArrayList<Inventory> entityViews = new ArrayList<>();
        int pageNum = 1;
        Inventory entityView = initializeInfoSubPageTemplate("", pageNum, viewType);

        // if no entity, return empty inventory
        if (materialList == null ) {
            entityViews.add(entityView);
            return entityViews;
        }

        int slot = 10;
        for (Map.Entry<String, Double> map : materialList.entrySet()) {
            String name = map.getKey();
            double worth = map.getValue();
            Material material;
            if (viewType.equals("Spawner Info")) {
                material = Material.SPAWNER;
            } else {
                material = Material.getMaterial(name);
            }

            List<String> lore = infoOptions.getSubPageItemLore();
            List<String> parsedLore = parseLore(lore, "%worth%", worth);
            String itemName = infoOptions.getSubPageItemName();
            String parsedName = parseName(itemName, "%name%", name);
            entityView.setItem(slot, createGuiItem(material, parsedName, false,
                parsedLore.toArray(new String[0])));

            slot++;

            // next line
            if (slot == 17 || slot == 26) {
                slot += 2;
            }

            // next page
            if (slot == 35) {
                entityViews.add(entityView);
                pageNum++;
                slot = 10;
                entityView = initializeInfoSubPageTemplate("", pageNum, viewType);
            }
        }
        entityViews.add(entityView);

        return entityViews;
    }

    public Inventory getMainInfoPage() {
        return infoGui.getMainInfoPage();
    }

    /**
     * Gets the block info page.
     *
     * @param pageNum page number to show
     * @return inventory showing block page of info
     */
    public Inventory getBlockInfoPage(int pageNum) {
        return infoGui.getBlockInfoPage(pageNum);
    }

    /**
     * Gets the spawner info page.
     *
     * @param pageNum page number to show
     * @return inventory showing spawner page of info
     */
    public Inventory getSpawnerInfoPage(int pageNum) {
        return infoGui.getSpawnerInfoPage(pageNum);
    }

    /**
     * Gets the container info page.
     *
     * @param pageNum page number to show
     * @return inventory showing container page of info
     */
    public Inventory getContainerInfoPage(int pageNum) {
        return infoGui.getContainerInfoPage(pageNum);
    }

    /**
     * Gets the inventory info page.
     *
     * @param pageNum page number to show
     * @return inventory showing inventory page of info
     */
    public Inventory getInventoryInfoPage(int pageNum) {
        return infoGui.getInventoryInfoPage(pageNum);
    }

    public StatsMenuOptions getStatsOptions() {
        return statsOptions;
    }

    public InfoMenuOptions getInfoOptions() {
        return infoOptions;
    }
}
