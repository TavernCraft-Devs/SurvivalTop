package tk.taverncraft.survivaltop.ui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.logs.LogManager;
import tk.taverncraft.survivaltop.stats.cache.EntityCache;
import tk.taverncraft.survivaltop.utils.types.MutableInt;

/**
 * GuiManager handles all logic related to showing information in a GUI.
 */
public class GuiManager extends GuiHelper {
    private final Main main;
    private final int mainPageSize;
   // private InfoGui infoGui;

    private ConcurrentHashMap<UUID, StatsGui> senderGui = new ConcurrentHashMap<>();

    /**
     * Constructor for GuiManager.
     *
     * @param main plugin class
     */
    public GuiManager(Main main) {
        this.main = main;
        this.mainPageSize = main.getMenuConfig().getInt("menu-size", 27);
        //initializeInfoGui();
    }

//    public void initializeInfoGui() {
//        this.infoGui = new InfoGui(main);
//    }

    public StatsGui createStatsGui(String name, double balWealth, double landWealth, double blockWealth,
            double spawnerWealth, double containerWealth, double inventoryWealth, double totalWealth,
            HashMap<String, MutableInt> blockList, HashMap<String, MutableInt> spawnerList,
            HashMap<String, MutableInt> containerList, HashMap<String, MutableInt> inventoryList) {

        // set up inventories
        name = name + " ";
        Inventory mainPage = getMainPage(name, balWealth, landWealth, blockWealth, spawnerWealth,
                containerWealth, inventoryWealth, totalWealth);

        ArrayList<Inventory> blockViews = prepareViews(blockList, name, "Block Stats");
        ArrayList<Inventory> spawnerViews = prepareViews(spawnerList, name, "Spawner Stats");
        ArrayList<Inventory> containerViews = prepareViews(containerList, name, "Container Stats");
        ArrayList<Inventory> inventoryViews = prepareViews(inventoryList, name, "Inventory Stats");

        return new StatsGui(mainPage, blockViews, spawnerViews, containerViews, inventoryViews);
    }

    /**
     * Sets up the main page for stats.
     *
     * @param entityName name of entity whose stats is shown
     */
    public Inventory getMainPage(String entityName, double balWealth, double landWealth, double blockWealth,
            double spawnerWealth, double containerWealth, double inventoryWealth, double totalWealth) {
        Inventory inv = Bukkit.createInventory(null, this.mainPageSize,
                entityName + "Wealth Stats" + identifier);
        for (int i = 0; i < mainPageSize; i++) {
            inv.setItem(i, createGuiItem(background, "", false));
        }

        // formatting to 2dp
        balWealth = new BigDecimal(balWealth).setScale(2,
                RoundingMode.CEILING).doubleValue();
        totalWealth = new BigDecimal(totalWealth).setScale(2,
                RoundingMode.CEILING).doubleValue();


        inv.setItem(10, createGuiItem(Material.BEACON, "Total Combined Wealth",
                true,"Total Wealth: " + totalWealth));
        if (main.getOptions().balIsIncluded()) {
            inv.setItem(11, createGuiItem(Material.EMERALD, "Total Balance Wealth",
                    true, "Balance Wealth: " + balWealth));
        } else {
            inv.setItem(11, createGuiItem(Material.EMERALD, "Total Balance Wealth",
                    false, "Disabled"));
        }
        if (main.getOptions().landIsIncluded()) {
            inv.setItem(12, createGuiItem(Material.GOLDEN_SHOVEL, "Total Land Wealth",
                    true, "Land Wealth: " + landWealth));
            inv.setItem(13, createGuiItem(Material.GRASS_BLOCK, "Blocks Wealth",
                true, "Block Wealth: " + blockWealth, "Click to learn more."));
        } else {
            inv.setItem(12, createGuiItem(Material.GOLDEN_SHOVEL, "Total Land Wealth",
                    false, "Disabled"));
            inv.setItem(13, createGuiItem(Material.GRASS_BLOCK, "Blocks Wealth",
                    false, "Disabled"));
        }
        if (main.getOptions().spawnerIsIncluded() && main.getOptions().landIsIncluded()) {
            inv.setItem(14, createGuiItem(Material.SPAWNER, "Spawners Wealth",
                    true, "Spawner " +
                "Wealth: " + spawnerWealth, "Click to learn more."));
        } else {
            inv.setItem(14, createGuiItem(Material.SPAWNER, "Spawners Wealth",
                    false, "Disabled"));
        }
        if (main.getOptions().containerIsIncluded() && main.getOptions().landIsIncluded()) {
            inv.setItem(15, createGuiItem(Material.CHEST, "Container Wealth",
                    true, "Container Wealth: " + containerWealth,
                    "Click to learn more."));
        } else {
            inv.setItem(15, createGuiItem(Material.CHEST, "Container Wealth",
                    false, "Disabled"));
        }
        if (main.getOptions().inventoryIsIncluded()) {
            inv.setItem(16, createGuiItem(Material.PLAYER_HEAD, "Inventory Wealth",
                true, "Inventory Wealth: " + inventoryWealth,
                "Click to learn more."));
        } else {
            inv.setItem(16, createGuiItem(Material.PLAYER_HEAD, "Inventory Wealth",
                false, "Disabled"));
        }

        return inv;
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
    private ArrayList<Inventory> prepareViews(HashMap<String, MutableInt> materialList,
                                              String entityName, String viewType) {
        ArrayList<Inventory> entityViews = new ArrayList<>();
        int pageNum = 1;
        Inventory entityView = initializeSubPageTemplate(entityName, pageNum, viewType);

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
            double value;
            switch (viewType) {
            case "Block Stats":
                value = main.getLandManager().getBlockWorth(name);
                entityView.setItem(slot, createGuiItem(Material.getMaterial(name), name,
                    false, "Quantity: " + quantity,
                    "Item Worth: " + value, "Total Value: " + value * quantity));
                break;
            case "Spawner Stats":
                value = main.getLandManager().getSpawnerWorth(name);
                entityView.setItem(slot, createGuiItem(Material.SPAWNER, name,
                    false, "Quantity: " + quantity,
                    "Item Worth: " + value, "Total Value: " + value * quantity));
                break;
            case "Container Stats":
                value = main.getLandManager().getContainerWorth(name);
                entityView.setItem(slot, createGuiItem(Material.getMaterial(name), name,
                    false, "Quantity: " + quantity,
                    "Item Worth: " + value, "Total Value: " + value * quantity));
                break;
            default:
                value = main.getInventoryManager().getInventoryItemWorth(name);
                entityView.setItem(slot, createGuiItem(Material.getMaterial(name), name,
                    false, "Quantity: " + quantity,
                    "Item Worth: " + value, "Total Value: " + value * quantity));
                break;
            }

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
                entityView = initializeSubPageTemplate(entityName, pageNum, viewType);
            }
        }
        entityViews.add(entityView);

        return entityViews;
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
}
