package tk.taverncraft.survivaltop.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import tk.taverncraft.survivaltop.Main;

/**
 * InfoGui handles all logic related to showing item info in a GUI.
 */
public class InfoGui extends GuiHelper {

    // list of worth values
    private static LinkedHashMap<String, Double> blockList;
    private static LinkedHashMap<String, Double> spawnerList;
    private static LinkedHashMap<String, Double> containerList;
    private static LinkedHashMap<String, Double> inventoryList;

    // list of inventories
    public static Inventory mainPage;
    public static ArrayList<Inventory> blockViews;
    public static ArrayList<Inventory> spawnerViews;
    public static ArrayList<Inventory> containerViews;
    public static ArrayList<Inventory> inventoryViews;

    private final int mainPageSize = 27;

    /**
     * Constructor for InfoGui.
     *
     * @param main plugin class
     */
    public InfoGui(Main main) {
        // todo: cleanup this entire mess during the ui update
        HashMap<Material, Double> blockList = main.getLandManager().getBlockWorth();
        HashMap<EntityType, Double> spawnerList = main.getLandManager().getSpawnerWorth();
        HashMap<Material, Double> containerList = main.getLandManager().getContainerWorth();
        HashMap<Material, Double> inventoryList = main.getInventoryManager().getInventoryItemWorth();
        InfoGui.blockList = changeMaterialDoubleToString(blockList);
        InfoGui.spawnerList = changeEntityTypeDoubleToString(spawnerList);
        InfoGui.containerList = changeMaterialDoubleToString(containerList);
        InfoGui.inventoryList = changeMaterialDoubleToString(inventoryList);
        initializeAllPages();
    }

    /**
     * Initialize all values to default.
     */
    public void initializeAllPages() {
        setUpMainPage();
        blockViews = prepareViews(blockList, "Block Info");
        spawnerViews = prepareViews(spawnerList, "Spawner Info");
        containerViews = prepareViews(containerList, "Container Info");
        inventoryViews = prepareViews(inventoryList, "Inventory Info");
    }

    /**
     * Sets up the main page for item info.
     */
    public void setUpMainPage() {
        Inventory inv = Bukkit.createInventory(null, this.mainPageSize,
                "Item Values Info" + identifier);
        for (int i = 0; i < mainPageSize; i++) {
            inv.setItem(i, createGuiItem(background, " ", false));
        }

        inv.setItem(11, createGuiItem(Material.EMERALD, "Balance Info",
            false,"Taken directly from your balance!"));
        inv.setItem(12, createGuiItem(Material.GRASS_BLOCK, "Block Info",
                false,"Click to learn more."));
        inv.setItem(13, createGuiItem(Material.SPAWNER, "Spawner Info",
                false,"Click to learn more."));
        inv.setItem(14, createGuiItem(Material.CHEST, "Container Info",
                false,"Click to learn more."));
        inv.setItem(15, createGuiItem(Material.PLAYER_HEAD, "Inventory Info",
            false,"Click to learn more."));

        mainPage = inv;
    }

    /**
     * Prepares the inventory views for block, spawner and container.
     *
     * @param materialList list of materials to show in gui
     * @param viewType type of view (block, spawner or container)
     *
     * @return An array list representing pages of inventory for the view type
     */
    private ArrayList<Inventory> prepareViews(LinkedHashMap<String, Double> materialList,
            String viewType) {
        ArrayList<Inventory> entityViews = new ArrayList<>();
        int pageNum = 1;
        Inventory entityView = initializeSubPageTemplate("", pageNum, viewType);

        // if no entity, return empty inventory
        if (materialList == null ) {
            entityViews.add(entityView);
            return entityViews;
        }

        int slot = 10;
        for (Map.Entry<String, Double> map : materialList.entrySet()) {
            String name = map.getKey();
            double value = map.getValue();
            if (viewType.equals("Spawner Info")) {
                entityView.setItem(slot, createGuiItem(Material.SPAWNER, name,
                        false,"Item Worth: " + value));
            } else {
                entityView.setItem(slot, createGuiItem(Material.getMaterial(name), name,
                        false, "Item Worth: " + value));
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
                entityView = initializeSubPageTemplate("", pageNum, viewType);
            }
        }
        entityViews.add(entityView);

        return entityViews;
    }

    /**
     * Gets the block info page.
     *
     * @param pageNum page number to show
     * @return inventory showing block page of info
     */
    public static Inventory getBlockInfoPage(int pageNum) {
        try {
            return blockViews.get(pageNum);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Gets the spawner info page.
     *
     * @param pageNum page number to show
     * @return inventory showing spawner page of info
     */
    public static Inventory getSpawnerInfoPage(int pageNum) {
        try {
            return spawnerViews.get(pageNum);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Gets the container info page.
     *
     * @param pageNum page number to show
     * @return inventory showing container page of info
     */
    public static Inventory getContainerInfoPage(int pageNum) {
        try {
            return containerViews.get(pageNum);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Gets the inventory info page.
     *
     * @param pageNum page number to show
     * @return inventory showing inventory page of info
     */
    public static Inventory getInventoryInfoPage(int pageNum) {
        try {
            return inventoryViews.get(pageNum);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
}
